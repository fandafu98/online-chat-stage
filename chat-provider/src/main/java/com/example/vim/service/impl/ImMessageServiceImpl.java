package com.example.vim.service.impl;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vim.mapper.ImMessageMapper;
import com.example.vim.domain.ImMessage;
import com.example.vim.service.IImMessageService;

/**
 * 聊天信息Service业务层处理
 * 
 * @author 乐天
 * @since 2022-01-25
 */
@Service
public class ImMessageServiceImpl extends ServiceImpl<ImMessageMapper, ImMessage>  implements IImMessageService 
{

}
