package com.tan.blog.controller;

import com.tan.blog.service.SysUserService;
import com.tan.blog.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users")
public class UsersController {

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("currentUser")
    public Result currentUser(@RequestHeader("Authorization")String token){
        return sysUserService.findUserByToken(token);

    }
}
