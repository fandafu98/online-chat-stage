package com.example.api.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.api.entity.Message;
import com.example.api.service.VimMessageService;
import com.example.vim.domain.ImMessage;
import com.example.vim.service.IImMessageService;
import com.example.vim.utils.ChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 消息处理：群聊消息在一个
 *
 * @author 乐天
 */
@Service
public class VimMessageServiceImpl implements VimMessageService {

    private static final Logger log = LoggerFactory.getLogger(VimMessageServiceImpl.class);

    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private IImMessageService iImMessageService;


    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 添加消息到redis 队列，有可能受到的是离线消息，所以要去删除下离线消息里面的记录
     *
     * @param message 消息
     * @param isRead  是否
     * @return boolean
     * @throws JsonProcessingException 抛出异常
     */
    @Override
    public boolean save(Message message, boolean isRead) throws Exception {
        //先保存消息
        String key = isRead ? ChatUtils.getChatKey(message.getFromId(), message.getChatId(), message.getType()) : StrUtil.format(ChatUtils.UNREAD_TEMPLATE, message.getChatId());
        boolean res = Boolean.TRUE.equals(redisTemplate.opsForZSet().add(key, objectMapper.writeValueAsString(message), message.getTimestamp()));
        //如果是离线消息，去离线队列里处理
        if (message.isOffline()) {
            String unreadKey = StrUtil.format(ChatUtils.UNREAD_TEMPLATE, message.getChatId());
            redisTemplate.opsForZSet().removeRangeByScore(unreadKey, message.getTimestamp(), message.getTimestamp());
        }
        if (isRead) {
            //异步保存到数据库
            new SaveChatMessageThread(message).run();
        }
        return res;
    }

    /**
     * 内部类 保存到数据库
     */
    class SaveChatMessageThread implements Runnable {

        private final Message message;

        public SaveChatMessageThread(Message message) {
            this.message = message;
        }

        @Override
        public void run() {
            try {
                ImMessage imMessage = new ImMessage();
                imMessage.setId(message.getId());
                if (message.getExtend() != null) {
                    imMessage.setExtend(objectMapper.writeValueAsString(message.getExtend()));
                }
                imMessage.setMessageType(message.getMessageType());
                imMessage.setContent(message.getContent());
                imMessage.setFromId(message.getFromId());
                imMessage.setToId(message.getChatId());
                imMessage.setSendTime(message.getTimestamp());
                //聊天的唯一
                imMessage.setChatKey(ChatUtils.getChatKey(message.getFromId(), message.getChatId(), message.getType()));
                iImMessageService.saveOrUpdate(imMessage);
            } catch (JsonProcessingException e) {
                log.error("保存消息进入数据库失败，JSON格式化问题");
            }
        }
    }


    /**
     * 查询消息
     *
     * @param chatId   聊天室id
     * @param fromId   发送人
     * @param chatType     聊天类型  私聊 群聊
     * @param pageSize 每页多少条
     * @return List
     */
    @Override
    public List<Message> list(String chatId, String fromId, String chatType, Long pageSize) {
        String key = ChatUtils.getChatKey(fromId, chatId, chatType);
        Set<String> set = redisTemplate.opsForZSet().reverseRange(key, 0, pageSize - 1);
        if (set != null) {
            List<Message> list = set.stream().map(this::toMessage).collect(Collectors.toList());
            Collections.reverse(list);
            //加上未读消息，分页第一页的数据是不一定和pageSize一样多，
            list.addAll(unreadList(chatId, fromId));
            return list;
        }
        return new ArrayList<>();
    }

    @Override
    public Page<Message> page(String chatId, String fromId, String messageType, String chatType, String searchText, Page<Message> page) {
        QueryWrapper<ImMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("chat_key", ChatUtils.getChatKey(fromId, chatId, chatType));
        if (StrUtil.isNotBlank(messageType)) {
            wrapper.eq("message_type", messageType);
        }
        if (StrUtil.isNotBlank(searchText)) {
            wrapper.like("content", StrUtil.format("%{}%", searchText));
        }
        wrapper.orderByDesc("id");
        Page<ImMessage> page1 = iImMessageService.page(new Page<>(page.getCurrent(), page.getSize()), wrapper);

        List<Message> messages = page1.getRecords().stream().map(item -> {
            Message message = new Message();
            message.setId(item.getId());
            message.setTimestamp(item.getSendTime());
            message.setFromId(item.getFromId());
            message.setChatId(item.getToId());
            message.setContent(item.getContent());
            message.setMessageType(item.getMessageType());
            try {
                if (StrUtil.isNotBlank(item.getExtend())) {
                    message.setExtend(objectMapper.readValue(item.getExtend(), HashMap.class));
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return message;
        }).collect(Collectors.toList());
        //第一页要加上未读消息
        if (page.getCurrent() == 1L) {
            messages = messages.stream().sorted(Comparator.comparing(Message::getId)).collect(Collectors.toList());
            Collections.reverse(messages);
        }
        page.setRecords(messages);
        page.setTotal(page1.getTotal());
        return page;
    }

    /**
     * 读取未读消息
     * 未读消息只存私聊消息，群聊消息还在群列表里
     *
     * @param chatId 聊天室id = toUserId
     * @param fromId 发送人id
     * @return List
     */
    @Override
    public List<Message> unreadList(String chatId, String fromId) {
        String key = StrUtil.format(ChatUtils.UNREAD_TEMPLATE, chatId);
        Set<String> set = redisTemplate.opsForZSet().range(key, 0, -1);
        if (set != null) {
            return set.stream().filter(str -> {
                try {
                    Message message = new ObjectMapper().readValue(str, Message.class);
                    //如果发送人为空，取出所有的未读消息
                    return StrUtil.isBlank(fromId) || message.getFromId().equals(fromId);
                } catch (JsonProcessingException e) {
                    log.error(e.getMessage());
                    return false;
                }

            }).map(this::toMessage).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }


    /**
     * 未读群消息，服务器时间是个很重要的数据，时间不对可能造成离线消息不能全部发送
     *
     * @param userId userId
     * @param chatId 群id
     * @return List
     */
    @Override
    public List<Message> unreadGroupList(String userId, String chatId) {
        String key = ChatUtils.getReadKey(userId, chatId);
        String value = redisTemplate.opsForValue().get(key);
        long score = -1;
        if (value != null && StrUtil.isNotBlank(value)) {
            score = Long.parseLong(value);
        }
        Set<String> set = redisTemplate.opsForZSet().rangeByScore(StrUtil.format(ChatUtils.GROUP_TEMPLATE, chatId), score, System.currentTimeMillis());
        if (set != null) {
            return set.stream().map(this::toMessage).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }


    /**
     * 已读消息的条数
     *
     * @param chatId 聊天室id
     * @param formId 发送人
     * @param type   聊天室类型
     * @return 数量
     */
    @Override
    public Long count(String chatId, String formId, String type) {
        String key = ChatUtils.getChatKey(formId, chatId, type);
        return redisTemplate.opsForZSet().size(key);
    }

    /**
     * 读消息，并持久化到redis
     *
     * @param chatId    消息id
     * @param userId    消息读取人
     * @param type      类型
     * @param timestamp 系统时间
     */
    @Override
    public void read(String chatId, String userId, String type, long timestamp) {
        String key = ChatUtils.getReadKey(userId, chatId);
        redisTemplate.opsForValue().set(key, String.valueOf(timestamp));
    }

    /**
     * json 转 message
     *
     * @param str str
     * @return Message;
     */
    private Message toMessage(String str) {
        try {
            Message message = new ObjectMapper().readValue(str, Message.class);
            message.setOffline(true);
            return message;
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return null;
        }
    }


}
