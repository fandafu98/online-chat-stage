package com.example.vim.utils;

import cn.hutool.core.util.StrUtil;

/**
 * 封装的信息类型 UTILS
 *
 * @author 乐天
 * @since 2018-10-07
 */
public class ChatUtils {

    /**
     * 存放私聊的已读消息
     * message-{minUserId}-{maxUserId}
     */
    public static final String FRIEND_TEMPLATE = "message-{}-{}";

    /**
     * 存放私聊的已读消息
     * message-{minUserId}-{maxUserId}
     */
    public static final String GROUP_TEMPLATE = "message-{}";

    /**
     * 存放群聊的最后一次查看消息时间
     * message-{userId：查看人}-{chatId：群id}
     */
    public static final String READ_TEMPLATE = "read-{}-{}";

    /**
     * 存放"私聊"的未读消息
     * message-{toUserId} 也是 chatId
     */
    public static final String UNREAD_TEMPLATE = "unread-message-{}";
    /**
     * 单聊
     */
    public static final String MESSAGE_TYPE_FRIEND = "0";

    /**
     * 群聊
     */
    public static final String MESSAGE_TYPE_GROUP = "1";

    /**
     * 获取一个固定的key来存储聊天记录到redis
     *
     * @param fromId 来源
     * @param chatId 聊天id
     * @param type   类型
     * @return chatId
     */
    public static String getChatKey(String fromId, String chatId, String type) {
        if (ChatUtils.MESSAGE_TYPE_FRIEND.equals(type)) {
            if (Long.parseLong(fromId) < Long.parseLong(chatId)) {
                return StrUtil.format(FRIEND_TEMPLATE, fromId, chatId);
            } else {
                return StrUtil.format(FRIEND_TEMPLATE, chatId, fromId);
            }
        } else {
            return StrUtil.format(GROUP_TEMPLATE, chatId);
        }
    }

    /**
     * 获取一个固定的key来存储聊天记录读取的最后时间
     *
     * @param userId 来源
     * @param chatId 聊天id
     * @return chatId
     */
    public static String getReadKey(String userId, String chatId) {
        return StrUtil.format(READ_TEMPLATE, userId, chatId);
    }

}
