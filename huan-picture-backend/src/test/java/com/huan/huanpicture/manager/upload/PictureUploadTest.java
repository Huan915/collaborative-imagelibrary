package com.huan.huanpicture.manager.upload;

import com.huan.huanpicture.model.dto.file.UploadPictureResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PictureUploadTest {

    @Resource
    private PictureUpload pictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Test
    void test01() {
        UploadPictureResult uploadPictureResult = urlPictureUpload.uploadPicture("https://pic.pngsucai.com/00/99/50/5ad1fc7c54645f87.webp", "templatetest");
        Assertions.assertNotNull(uploadPictureResult);
    }

}