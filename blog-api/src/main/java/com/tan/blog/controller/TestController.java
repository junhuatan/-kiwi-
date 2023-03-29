package com.tan.blog.controller;


import com.tan.blog.Utils.UserThreadLocal;
import com.tan.blog.dao.pojo.SysUser;
import com.tan.blog.vo.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestController {

    @RequestMapping
    public Result test(){
        SysUser sysUser = UserThreadLocal.get();
        System.out.println(sysUser);
        return Result.success(null);
    }
}
