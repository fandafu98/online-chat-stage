package com.example.vim.service.impl;


import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.vim.mapper.ImGroupMapper;
import com.example.vim.domain.ImGroup;
import com.example.vim.service.IImGroupService;

/**
 * 群管理Service业务层处理
 * 
 * @author 乐天
 * @since 2022-01-26
 */
@Service
public class ImGroupServiceImpl extends ServiceImpl<ImGroupMapper, ImGroup>  implements IImGroupService 
{

    @Override
    public boolean save(ImGroup entity) {
        return super.save(entity);
    }
}
