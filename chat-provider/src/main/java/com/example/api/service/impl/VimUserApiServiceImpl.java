package com.example.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.stage.core.system.domain.entity.SysUser;
import com.example.stage.core.system.service.ISysUserService;
import com.example.stage.core.utils.SecurityUtils;
import com.example.api.exception.VimBaseException;
import com.example.api.service.VimUserApiService;
import com.example.api.utils.SysUtils;
import com.example.api.vo.User;
import com.example.vim.domain.ImFriend;
import com.example.vim.mapper.ImFriendMapper;
import com.example.vim.service.IImFriendService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * vim 用户操作类，如果需要对接其他的系统，重新下面的方法即可
 *
 * @author 乐天
 */
@Service
public class VimUserApiServiceImpl implements VimUserApiService {

    private static final String CACHE_KEY = "user";

    @Resource
    private ISysUserService iSysUserService;

    @Resource
    private ImFriendMapper imFriendMapper;

    @Resource
    private IImFriendService iImFriendService;

    /**
     * 获取用户的好友 同时缓存
     *
     * @param userId 用户id
     * @return List<User>
     */
    @Override
    @Cacheable(value = CACHE_KEY + ":friend", key = "#userId")
    public List<User> getFriends(String userId) {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        return imFriendMapper.getUserFriends(String.valueOf(user.getUserId()), "0");
    }

    /**
     * 获取部门下的用户
     *
     * @param deptId 部门id
     * @return List<User>
     */
    @Override
    @Cacheable(value = CACHE_KEY + ":dept:user", key = "#deptId")
    public List<User> getByDept(String deptId) {
        SysUser sysUser = new SysUser();
        sysUser.setDeptId(Long.parseLong(deptId));
        return iSysUserService.selectUserList(sysUser).stream().map(this::transform).collect(Collectors.toList());
    }

    /**
     * 根据用户mobile获取用户
     *
     * @param mobile mobile
     * @return List<User>
     */
    @Override
    public List<User> search(String mobile) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile", mobile);
        return iSysUserService.list(queryWrapper).stream().map(this::transform).collect(Collectors.toList());
    }

    /**
     * 根据用户id获取用户,同时缓存用户
     *
     * @param userId 用户ID
     * @return User
     */
    @Override
    @Cacheable(value = CACHE_KEY + ":one", key = "#userId")
    public User get(String userId) {
        SysUser sysUser = iSysUserService.selectUserById(Long.parseLong(userId));
        return new User(String.valueOf(sysUser.getUserId()), sysUser.getNickName(), sysUser.getAvatar(), sysUser.getPhonenumber(), sysUser.getSex(), String.valueOf(sysUser.getDeptId()), sysUser.getEmail());
    }

    /**
     * 更新用户 同时清空用户相关的缓存
     *
     * @param user 用户
     * @return 更新数
     */
    @Override
    @CacheEvict(value = CACHE_KEY + ":one", key = "#user.id")
    public int update(User user) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(Long.parseLong(user.getId()));
        sysUser.setAvatar(user.getAvatar());
        sysUser.setNickName(user.getName());
        sysUser.setEmail(user.getEmail());
        sysUser.setSex(user.getSex());
        return iSysUserService.updateUser(sysUser);
    }


    /**
     * 添加好友同时清除双方的好友缓存关系
     *
     * @param friendId 好友id
     * @param userId   用户id
     * @return boolean
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY + ":friend", key = "#userId"),
            @CacheEvict(value = CACHE_KEY + ":friend", key = "#friendId")
    })
    public boolean addFriends(String friendId, String userId) {
        List<String> ids = getFriends(userId).stream().map(User::getId).collect(Collectors.toList());
        if (ids.contains(friendId)) {
            throw new VimBaseException("friend.also.added", null);
        }
        ImFriend imFriend = new ImFriend();
        imFriend.setFriendId(Long.parseLong(friendId));
        imFriend.setUserId(Long.parseLong(userId));
        imFriend.setState(SysUtils.FRIEND_STATUS_COMMON);
        iImFriendService.save(imFriend);
        return true;
    }

    /**
     * 删除好友同时清除双方的好友缓存关系
     *
     * @param friendId 好友id
     * @param userId   用户id
     * @return boolean
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = CACHE_KEY + ":friend", key = "#userId"),
            @CacheEvict(value = CACHE_KEY + ":friend", key = "#friendId")
    })
    public boolean delFriends(String friendId, String userId) {
        QueryWrapper<ImFriend> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("friend_id", friendId);
        boolean f1 = iImFriendService.remove(wrapper);

        QueryWrapper<ImFriend> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("user_id", friendId);
        wrapper1.eq("friend_id", userId);
        boolean f2 = iImFriendService.remove(wrapper1);
        return f1 || f2;
    }

    private User transform(SysUser sysUser) {
        return new User(String.valueOf(sysUser.getUserId()), sysUser.getNickName(), sysUser.getAvatar(), sysUser.getPhonenumber(), sysUser.getSex(), String.valueOf(sysUser.getDeptId()), sysUser.getEmail());
    }

    @Override
    public boolean isFriends(String friendId, String userId) {
        QueryWrapper<ImFriend> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("friend_id", friendId);
        boolean f0 = iImFriendService.count(wrapper)==1;

        QueryWrapper<ImFriend> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("user_id", friendId);
        wrapper1.eq("friend_id", userId);
        boolean f1 = iImFriendService.count(wrapper1)==1;

        return f0 || f1;
    }

    @Override
    public int save(User user) {
        SysUser sysUser = new SysUser();
        sysUser.setAvatar(user.getAvatar());
        sysUser.setNickName(user.getName());
        sysUser.setEmail(user.getEmail());
        sysUser.setSex(user.getSex());
        return iSysUserService.updateUser(sysUser);
    }
}
