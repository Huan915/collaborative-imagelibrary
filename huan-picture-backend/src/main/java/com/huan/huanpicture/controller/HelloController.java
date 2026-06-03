package com.huan.huanpicture.controller;

import com.huan.huanpicture.common.BaseResponse;
import com.huan.huanpicture.common.ResultUtils;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public BaseResponse<String> sayHello() {
        return ResultUtils.success("hello");
    }
}
