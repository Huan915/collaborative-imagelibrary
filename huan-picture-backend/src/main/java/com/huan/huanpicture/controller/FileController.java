package com.huan.huanpicture.controller;

import com.huan.huanpicture.common.BaseResponse;
import com.huan.huanpicture.common.ResultUtils;
import com.huan.huanpicture.exception.BusinessException;
import com.huan.huanpicture.exception.ErrorCode;
import com.huan.huanpicture.manager.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {
    @Resource
    private CosManager cosManager;

    @PostMapping("/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String filePath = String.format("/test/%s", fileName);
        File tempFile = null;
        try {
            tempFile = File.createTempFile(filePath, null);
            file.transferTo(tempFile);

            cosManager.putPictureObject(filePath, tempFile);
            return ResultUtils.success(filePath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (tempFile != null) {
                // 删除临时文件
                boolean delete = tempFile.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", tempFile);
                }
            }
        }
    }

    @GetMapping("/download")
    public void testDownloadFile(String filePath, HttpServletResponse response) {
        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObject(filePath);
            // cos文件对象转成字节数组
            cosObjectInput = cosObject.getObjectContent();
            byte[] byteArray = IOUtils.toByteArray(cosObjectInput);
            // 设置相应头信息
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filePath);
            // 写入相应
            response.getOutputStream().write(byteArray);
            response.getOutputStream().flush();
        } catch (Exception exception) {
            log.error("file down error, filepath = {}", filePath, exception);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInput != null) {
                try {
                    cosObjectInput.close();
                } catch (IOException e) {
                    log.error("Failed to close COSObjectInputStream");
                }
            }
        }
    }
}
