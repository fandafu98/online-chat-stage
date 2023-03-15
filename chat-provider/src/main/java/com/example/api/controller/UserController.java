package com.example.api.controller;

import com.example.stage.core.system.domain.AjaxResult;
import com.example.stage.core.system.domain.entity.SysUser;
import com.example.stage.core.utils.SecurityUtils;
import com.example.api.service.VimUserApiService;
import com.example.api.vo.User;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/sys/users")
public class UserController {

    @Resource
    private VimUserApiService vimUserApiService;

    @GetMapping("{userId}")
    public AjaxResult get(@PathVariable String userId) {
        return AjaxResult.success(vimUserApiService.get(userId));
    }

    @GetMapping("my")
    public AjaxResult my() {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        return AjaxResult.success(vimUserApiService.get(String.valueOf(user.getUserId())));
    }

    @GetMapping("search")
    public AjaxResult search(String mobile) {
        return AjaxResult.success(vimUserApiService.search(mobile));
    }

    @PutMapping("{userId}")
    public AjaxResult update(@PathVariable String userId, @RequestBody User user) {
        user.setId(userId);
        return AjaxResult.success(vimUserApiService.update(user));
    }


}
