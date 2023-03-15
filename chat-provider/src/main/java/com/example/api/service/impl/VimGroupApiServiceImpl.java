package com.example.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.stage.core.system.domain.entity.SysUser;
import com.example.stage.core.utils.SecurityUtils;
import com.example.api.service.VimGroupApiService;
import com.example.api.utils.SysUtils;
import com.example.api.vo.Group;
import com.example.api.vo.User;
import com.example.tio.StartTioRunner;
import com.example.vim.domain.ImGroup;
import com.example.vim.domain.ImGroupUser;
import com.example.vim.mapper.ImGroupMapper;
import com.example.vim.mapper.ImGroupUserMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tio.core.Tio;
import org.tio.server.ServerTioConfig;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * vim 群操作类，如果需要对接其他的系统，重新下面的方法即可
 *
 * @author 乐天
 */
@Service
public class VimGroupApiServiceImpl implements VimGroupApiService {

    private static final String CACHE_KEY = "group";

    @Resource
    private ImGroupMapper imGroupMapper;

    @Resource
    private ImGroupUserMapper imGroupUserMapper;

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 获取用户所在的群
     *
     * @param userId 用户id
     * @return List<Group>
     */
    @Override
    @Cacheable(value = CACHE_KEY + ":list", key = "#userId")
    public List<Group> getGroups(String userId) {
        return imGroupMapper.getUserGroups(userId).stream().map(this::transform).collect(Collectors.toList());
    }

    /**
     * 获取群里所有的用户
     *
     * @param groupId 群id
     * @return List<User>
     */
    @Override
    @Cacheable(value = CACHE_KEY + ":user:list", key = "#groupId")
    public List<User> getUsers(String groupId) {
        return imGroupMapper.getGroupUsers(groupId).stream().map(this::transform).collect(Collectors.toList());
    }

    /**
     * 获取群
     *
     * @param groupId 群id
     * @return Group
     */
    @Override
    @Cacheable(value = CACHE_KEY + ":one", key = "#groupId")
    public Group get(String groupId) {
        ImGroup group = imGroupMapper.selectById(groupId);
        if (group == null) {
            throw new RuntimeException("不能找到对应的群，可能群已经被删除");
        }
        return transform(group);
    }

    /**
     * 给群新增用户
     *
     * @param groupId 群id
     * @param userIds 用户id，多个
     * @return 是否添加成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CACHE_KEY + ":user:list", key = "#groupId")
    public boolean addUsers(String groupId, String[] userIds) {
        StartTioRunner startTioRunner = applicationContext.getBean(StartTioRunner.class);
        ServerTioConfig serverTioConfig = startTioRunner.getAppStarter().getWsServerStarter().getServerTioConfig();
        for (String userId : userIds) {
            ImGroupUser imGroupUser = new ImGroupUser();
            imGroupUser.setUserId(userId);
            imGroupUser.setGroupId(groupId);
            imGroupUser.setState("0");
            imGroupUserMapper.insert(imGroupUser);
            Tio.bindGroup(serverTioConfig, userId, groupId);
        }
        return true;
    }

    /**
     * 删除群用户
     *
     * @param groupId 群id
     * @param userIds 用户id，多个
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {CACHE_KEY + ":one", CACHE_KEY + ":list", CACHE_KEY + ":user:list"}, allEntries = true)
    public boolean delUsers(String groupId, String[] userIds) {
        StartTioRunner startTioRunner = applicationContext.getBean(StartTioRunner.class);
        ServerTioConfig serverTioConfig = startTioRunner.getAppStarter().getWsServerStarter().getServerTioConfig();
        for (String userId : userIds) {
            QueryWrapper<ImGroupUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            queryWrapper.eq("group_id", groupId);
            imGroupUserMapper.delete(queryWrapper);
            Tio.unbindGroup(serverTioConfig, userId, groupId);
        }
        return true;
    }

    /**
     * 删除一个群
     *
     * @param groupId 群Id
     * @return 删除数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {CACHE_KEY + ":one", CACHE_KEY + ":list", CACHE_KEY + ":user:list"}, allEntries = true)
    public int del(String groupId) {
        this.delUsers(groupId, getUsers(groupId).stream().map(User::getId).toArray(String[]::new));
        return imGroupMapper.deleteById(groupId);
    }



    /**
     * 群类型转换
     *
     * @param imGroup 群
     * @return Group
     */
    private Group transform(ImGroup imGroup) {
        return new Group(imGroup.getId(), imGroup.getName(), imGroup.getAvatar(), String.valueOf(imGroup.getMaster()), imGroup.getNeedCheck());
    }

    /**
     * 用户类型转换
     *
     * @param sysUser 用户
     * @return 群
     */
    private User transform(SysUser sysUser) {
        return new User(String.valueOf(sysUser.getUserId()), sysUser.getNickName(), sysUser.getAvatar(), sysUser.getPhonenumber(), sysUser.getSex(), String.valueOf(sysUser.getDeptId()), sysUser.getEmail());
    }

    /**
     * 新建一个群
     *
     * @param group 群
     * @return Group
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {CACHE_KEY + ":one", CACHE_KEY + ":list", CACHE_KEY + ":user:list"}, allEntries = true)
    public Group save(Group group) {
        SysUser sysUser = SecurityUtils.getLoginUser().getUser();
        ImGroup imGroup = new ImGroup();
        imGroup.setMaster(sysUser.getUserId());
        imGroup.setName(group.getName());
        imGroup.setAvatar(group.getAvatar());
        imGroup.setDelFlag(SysUtils.DEL_NO);
        imGroup.setNeedCheck(group.getNeedCheck());
        //imGroup.preInsert();
        imGroupMapper.insert(imGroup);
        String[] userIds = {String.valueOf(sysUser.getUserId())};
        this.addUsers(imGroup.getId(), userIds);
        group.setId(imGroup.getId());
        return transform(imGroup);
    }

    /**
     * 更新一个群
     *
     * @param group 群
     * @return Group
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {CACHE_KEY + ":one", CACHE_KEY + ":list", CACHE_KEY + ":user:list"}, allEntries = true)
    public int update(Group group) {
        ImGroup imGroup = imGroupMapper.selectById(group.getId());
        imGroup.setName(group.getName());
        imGroup.setAvatar(group.getAvatar());
        imGroup.setNeedCheck(group.getNeedCheck());
        imGroup.setDelFlag(SysUtils.DEL_NO);
        //imGroup.preUpdate();
        return imGroupMapper.updateById(imGroup);
    }
}
