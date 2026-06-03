package com.huan.huanpicture;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {ShardingSphereAutoConfiguration.class})
@MapperScan("com.huan.huanpicture.mapper")
//@EnableAspectJAutoProxy(exposeProxy = true)
public class HuanPictureApplication {
    public static void main(String[] args) {
        SpringApplication.run(HuanPictureApplication.class, args);
    }

}
