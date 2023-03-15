package com.example.api.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.api.entity.Message;

import java.util.List;

/**
 * 消息处理接口
 * @author 乐天
 */
public interface VimMessageService {

   /**
    * 添加消息到redis 队列
    *
    * @param message 消息
    * @param isRead  是否
    * @return boolean
    * @throws Exception 抛出异常
    */
   boolean save(Message message, boolean isRead) throws Exception;

   /**
    * 查询消息
    *
    * @param chatId   聊天室id
    * @param fromId   userId
    * @param type     聊天类型  私聊 群聊
    * @param pageSize 每页多少条
    * @return List
    */
   List<Message> list(String chatId, String fromId, String type, Long pageSize);


   Page<Message> page(String chatId, String fromId, String messageType, String chatType, String searchText, Page<Message> page);

   /**
    * 读取未读消息，并清空(这里可能是会出现丢数据，未读消息清空后，消息没有发送成功，造成未读列表和已读列表都没有消息)
    * 未读消息只存私聊消息，群聊消息还在群列表里
    * @param chatId 聊天室id
    * @return List
    */
   List<Message> unreadList(String chatId,String fromId);

   /**
    * 查询一个群的未读消息
    * @param chatId 群id
    * @return List
    */
   List<Message> unreadGroupList(String userId, String chatId);

   /**
    * 已读消息的条数
    *
    * @param chatId 聊天室id
    * @param type   聊天室类型
    * @return 数量
    */
   Long count(String chatId, String userId, String type);

   /**
    * 读消息，并持久化到redis
    *
    * @param chatId    消息id
    * @param userId    消息读取人
    * @param type      类型
    * @param timestamp 系统时间
    */
   void read(String chatId, String userId, String type, long timestamp) ;
}
