package com.example.vim.service.impl;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vim.mapper.ImGroupUserMapper;
import com.example.vim.domain.ImGroupUser;
import com.example.vim.service.IImGroupUserService;

/**
 * 群关系Service业务层处理
 * 
 * @author 乐天
 * @since 2022-01-25
 */
@Service
public class ImGroupUserServiceImpl extends ServiceImpl<ImGroupUserMapper, ImGroupUser>  implements IImGroupUserService 
{


}
