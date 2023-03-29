package com.tan.blog.handler;

import com.tan.blog.vo.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 对加了@Controller注解的进行拦截处理AOp的实现
 */
@ControllerAdvice
public class AllExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody//返回json数据
    public Result doEeception(Exception ex){
        ex.printStackTrace();
        return Result.fail(-999,"系统异常");
    }
}
