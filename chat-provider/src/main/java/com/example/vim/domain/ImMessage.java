package com.example.vim.domain;

import com.example.stage.core.annotation.Excel;

import java.io.Serializable;

/**
 * 聊天信息对象 im_message
 *
 * @author 乐天
 * @since 2022-01-25
 */
public class ImMessage implements Serializable {
    private static final Long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    private String id;

    /**
     * 接收人
     */
    @Excel(name = "接收人")
    private String toId;

    /**
     * 发送人
     */
    @Excel(name = "发送人")
    private String fromId;

    /**
     * 发送时间
     */
    @Excel(name = "发送时间")
    private Long sendTime;

    /**
     * 内容
     */
    @Excel(name = "内容")
    private String content;

    /**
     * 类型
     */
    @Excel(name = "类型")
    private String messageType;

    /**
     * 扩展 JSON
     */
    @Excel(name = "扩展")
    private String extend;

    /**
     * 聊天室id
     */
    private String chatKey;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public String getToId() {
        return toId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getFromId() {
        return fromId;
    }

    public void setSendTime(Long sendTime) {
        this.sendTime = sendTime;
    }

    public Long getSendTime() {
        return sendTime;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }

    public String getChatKey() {
        return chatKey;
    }

    public void setChatKey(String chatKey) {
        this.chatKey = chatKey;
    }
}
