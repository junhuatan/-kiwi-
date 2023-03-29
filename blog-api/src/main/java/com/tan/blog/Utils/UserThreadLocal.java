package com.tan.blog.Utils;

import com.tan.blog.dao.pojo.SysUser;

public class UserThreadLocal {

    private UserThreadLocal() {}

    //线程变量隔离
    private static final ThreadLocal<SysUser> LOCAL = new ThreadLocal<SysUser>();

    public static void put(SysUser sysuser) {
        LOCAL.set(sysuser);
    }

    public static SysUser get() {
        return LOCAL.get();
    }

    public static void remove() {
        LOCAL.remove();
    }
}
