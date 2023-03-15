package com.example.vim.service.impl;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vim.mapper.ImFriendMapper;
import com.example.vim.domain.ImFriend;
import com.example.vim.service.IImFriendService;

/**
 * 好友Service业务层处理
 * 
 * @author ruoyi
 * @since 2022-02-03
 */
@Service
public class ImFriendServiceImpl extends ServiceImpl<ImFriendMapper, ImFriend>  implements IImFriendService 
{


}
