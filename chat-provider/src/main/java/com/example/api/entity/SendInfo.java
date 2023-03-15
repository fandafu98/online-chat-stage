package com.example.api.entity;

import java.io.Serializable;
import java.util.Map;

/**
 * websocket 通讯的json 封装
 *
 * @author 乐天
 * @since 2018-10-07
 */
public class SendInfo implements Serializable {

    /**
     * 发送信息的代码
     */
    private String code;

    /**
     * 信息的json 根据不同的消息类型分别解析
     */
    private Map<String,Object> message;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, Object> getMessage() {
        return message;
    }

    public void setMessage(Map<String, Object> message) {
        this.message = message;
    }
}
