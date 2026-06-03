package com.huan.huanpicture.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.huan.huanpicture.exception.BusinessException;
import com.huan.huanpicture.exception.ErrorCode;
import com.huan.huanpicture.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 2. URL格式校验
        ThrowUtils.throwIf(!StrUtil.startWithAny(fileUrl.toLowerCase(), "http://", "https://"),
                ErrorCode.PARAMS_ERROR, "文件URL格式不正确，必须以http://或https://开头");

        try (HttpResponse response = HttpUtil.createGet(fileUrl)
                .setFollowRedirects(true)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .setMaxRedirectCount(5)
                .timeout(5000)
                .execute()){

            // 4. 检查HTTP状态码
            int statusCode = response.getStatus();
            ThrowUtils.throwIf(statusCode != HttpStatus.HTTP_OK, ErrorCode.PARAMS_ERROR, "无法访问该图片URL，状态码：" + statusCode);

            // 5. 检查Content-Type是否为图片类型
            String contentType = response.header("Content-Type");
            if (contentType != null) {
                List<String> validImageTypes = Arrays.asList("image/jpeg","image/jpg", "image/png", "image/webp");
                boolean isImageType = validImageTypes.stream()
                        .anyMatch(type -> contentType.toLowerCase().startsWith(type));
                ThrowUtils.throwIf(!isImageType, ErrorCode.PARAMS_ERROR, "文件格式不正确" + contentType);
            }
            // 6. 检查文件大小（可选，限制不超过2MB）
            String contentLengthStr = response.header("Content-Length");
            if (contentLengthStr != null) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 图片最大为10MB
                    ThrowUtils.throwIf(contentLength > MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, "图片文件大小不能超过2MB");
                } catch (NumberFormatException e) {
                    // 如果无法解析Content-Length，则跳过大小检查
                    log.warn("无法解析Content-Length头部: {}", contentLengthStr);
                }
            }
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw e; // 重新抛出已知业务异常
            }
            log.error("校验图片URL时发生错误: ", e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片URL校验失败：" + e.getMessage());
        }
    }

    /**
     *   获取图片名称
     * @param inputSource
     * @return
     */
    @Override
    protected String getOriginalFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        return FileUtil.mainName(fileUrl);
    }

    @Override
    protected void processFile(Object inputSource, File file) {
        String fileUrl = (String) inputSource;
        // 下载文件到临时目录
        HttpUtil.downloadFile(fileUrl, file);
    }
}
