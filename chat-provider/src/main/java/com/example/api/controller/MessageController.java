package com.example.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.stage.core.system.domain.AjaxResult;
import com.example.api.service.VimMessageService;
import com.example.api.entity.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;


/**
 * @author 乐天
 */
@RestController
@RequestMapping("/api/sys/messages")
public class MessageController {

    @Resource
    private VimMessageService vimMessageService;

    @GetMapping
    public AjaxResult list(String chatId, String fromId, String type, Long pageSize) {
        List<Message> messageVoList = vimMessageService.list(chatId, fromId, type, pageSize);
        for (Message message : messageVoList) {
            message.setMine(fromId.equals(String.valueOf(message.getFromId())));
        }
        return AjaxResult.success(messageVoList);
    }

    /**
     * 分页查询
     *
     * @param page 分页条件
     * @return page
     */
    @GetMapping(value = "page")
    public AjaxResult page(Page<Message> page, String searchText, String chatId, String fromId, String messageType, String chatType) {
        return AjaxResult.success(vimMessageService.page(chatId, fromId, messageType,chatType, searchText, page));
    }
}
