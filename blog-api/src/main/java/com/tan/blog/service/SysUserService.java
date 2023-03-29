package com.tan.blog.service;

import com.tan.blog.dao.pojo.SysUser;
import com.tan.blog.vo.Result;
import com.tan.blog.vo.UserVo;

public interface SysUserService {

    UserVo findUserVoById(Long id);

    SysUser findUserById(Long id);

    SysUser findUser(String account, String password);

    /**
     *根据token获取user
     * @param token
     * @return
     */
    Result findUserByToken(String token);

    SysUser findUserByAccount(String account);

    void save(SysUser sysUser);
}
