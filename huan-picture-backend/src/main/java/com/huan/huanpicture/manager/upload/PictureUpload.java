package com.huan.huanpicture.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.huan.huanpicture.exception.ErrorCode;
import com.huan.huanpicture.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class PictureUpload extends PictureUploadTemplate {
    /**
     * 校验文件是否符合规范
     *
     * @param inputSource 文件URL
     */
    @Override
    protected void validPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 校验文件大小
        long size = multipartFile.getSize();
        final long ONE_M = 1024 * 1024;
        ThrowUtils.throwIf(size > ONE_M * 2, ErrorCode.PARAMS_ERROR, "文件大小不能超过2MB");
        // 校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        // 允许上传的文件类型（后缀）
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpg", "png", "webp", "jpeg", "gif");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件格式不支持");
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        return ((MultipartFile) inputSource).getOriginalFilename();
    }

    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }
}
