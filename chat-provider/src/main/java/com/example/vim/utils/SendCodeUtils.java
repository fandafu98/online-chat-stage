package com.example.vim.utils;

/**
 * 发送
 * @author zkp
 */
public interface SendCodeUtils {

    /**
     * 心跳
     */
    String PING = "0";

    /**
     * 链接就绪
     */
    String READY = "1";

    /**
     * 文本消息
     */
    String MESSAGE = "2";


    /**
     * 消息已读回执
     */
    String READ = "3";
}
