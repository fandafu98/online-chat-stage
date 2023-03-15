package com.example.api.service;

import com.example.api.vo.User;

import java.util.List;

public interface VimUserApiService {

    List<User> getFriends(String userId);

    List<User> getByDept(String deptId);

    List<User> search(String mobile);

    User get(String userId);

    int update(User user);

    boolean addFriends(String friendId,String userId);

    boolean delFriends(String friendId,String userId);

    boolean isFriends(String friendId, String userId);

    int save(User user);
}
