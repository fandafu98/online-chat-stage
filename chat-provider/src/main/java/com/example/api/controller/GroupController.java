package com.example.api.controller;

import com.example.stage.core.system.controller.BaseController;
import com.example.stage.core.system.domain.AjaxResult;
import com.example.stage.core.system.domain.entity.SysUser;
import com.example.stage.core.utils.SecurityUtils;
import com.example.api.service.VimGroupApiService;
import com.example.api.vo.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/sys/groups")
public class GroupController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(GroupController.class);

    @Resource
    private VimGroupApiService vimGroupApiService;

    /**
     * 新建群
     *
     * @param group 群
     * @return boolean
     */
    @PostMapping("")
    public AjaxResult save(@RequestBody Group group) {
        return AjaxResult.success(vimGroupApiService.save(group));
    }

    /**
     * 更新群
     *
     * @param group 群
     * @return boolean
     */
    @PatchMapping(value = "{id}")
    public AjaxResult update(@PathVariable String id, @RequestBody Group group) {
        group.setId(id);
        return toAjax(vimGroupApiService.update(group));
    }

    /**
     * 获取群
     *
     * @param id 群id
     * @return boolean
     */
    @GetMapping(value = "{id}")
    public AjaxResult get(@PathVariable("id") String id) {
        return AjaxResult.success(vimGroupApiService.get(id));
    }

    /**
     * 查询当前用户所在的群
     *
     * @return List
     */
    @GetMapping
    public AjaxResult list() {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        return AjaxResult.success(vimGroupApiService.getGroups(String.valueOf(user.getUserId())));
    }


    /**
     * 查询当前群的所有的用户
     *
     * @return List
     */
    @GetMapping("{id}/users")
    public AjaxResult users(@PathVariable("id") String id) {
        return AjaxResult.success(vimGroupApiService.getUsers(id));
    }

    /**
     * 添加群成员
     *
     * @return List
     */
    @PostMapping("{id}/users")
    public AjaxResult addUsers(@PathVariable("id") String id, @RequestBody String[] userId) {
        try {
            vimGroupApiService.addUsers(id, userId);
            return AjaxResult.success();
        } catch (Exception e) {
            return AjaxResult.error();
        }
    }

    /**
     * 删除群成员
     *
     * @return List
     */
    @DeleteMapping("{id}/users/{userId}")
    public AjaxResult deleteUser(@PathVariable("id") String id, @PathVariable("userId") String userId) {
        String[] ids = {userId};
        return toAjax(vimGroupApiService.delUsers(id, ids));
    }


    /**
     * 主动退群
     *
     * @return List
     */
    @DeleteMapping("{id}/exit")
    public AjaxResult exit(@PathVariable("id") String id) {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        String[] ids = {String.valueOf(user.getUserId())};
        return toAjax(vimGroupApiService.delUsers(id, ids));
    }


    /**
     * 删除群成员
     *
     * @return List
     */
    @DeleteMapping("{id}/users")
    public AjaxResult deleteUsers(@PathVariable("id") String id, @RequestBody String[] userId) {
        return toAjax(vimGroupApiService.delUsers(id, userId));
    }


    /**
     * 删一个群,只有群主能删除
     *
     * @param id 群id
     * @return boolean
     */
    @DeleteMapping(value = "{id}")
    public AjaxResult delete(@PathVariable("id") String id) {
        checkMaster(id);
        return toAjax(vimGroupApiService.del(id));
    }


    /**
     * 判断是否群主
     *
     * @param groupId 群id
     * @return boolean
     */
    private void checkMaster(String groupId) {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        Group group = vimGroupApiService.get(groupId);
        if (!String.valueOf(user.getUserId()).equals(group.getMaster())) {
            throw new RuntimeException("非群主操作失败！");
        }
        ;
    }

}
