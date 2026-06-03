package com.huan.huanpicture.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import com.huan.huanpicture.config.CosClientConfig;
import com.huan.huanpicture.exception.BusinessException;
import com.huan.huanpicture.exception.ErrorCode;
import com.huan.huanpicture.exception.ThrowUtils;
import com.huan.huanpicture.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 文件服务（已废弃）
 * 改为使用upload中的方法
 */
@Slf4j
@Service
@Deprecated
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     *  上传图片
     * @param multipartFile
     * @param uploadPathPrefix
     * @return
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        // 校验图片
        validPicture(multipartFile);
        // 图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        // 自己拼接文件上传路径，而不是使用原始文件名称，可以增加安全性
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originalFilename));

        String upLoadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);

        File tempFile = null;
        try {
            // 上传文件
            tempFile = File.createTempFile(upLoadPath, null);
            multipartFile.transferTo(tempFile);
            PutObjectResult putObjectResult = cosManager.putPictureObject(upLoadPath, tempFile);
            // 获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 计算宽高
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
            // 封装返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + upLoadPath);
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicSize(FileUtil.size(tempFile));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            //返回可访问的地址
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("file upload error, filepath = " + upLoadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            deleteTemplate(tempFile);
        }
    }

    public UploadPictureResult uploadPictureByUrl(String fileUrl, String uploadPathPrefix) {
        // 校验图片
        validPicture(fileUrl);
        // 获取图片名称
        String originalFilename = FileUtil.mainName(fileUrl);
        // 图片上传地址
        String uuid = RandomUtil.randomString(16);
        // 自己拼接文件上传路径，而不是使用原始文件名称，可以增加安全性
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originalFilename));

        String upLoadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);

        File tempFile = null;
        try {
            // 上传文件
            tempFile = File.createTempFile(upLoadPath, null);
            HttpUtil.downloadFile(fileUrl, tempFile);

            PutObjectResult putObjectResult = cosManager.putPictureObject(upLoadPath, tempFile);
            // 获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 计算宽高
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
            // 封装返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + upLoadPath);
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicSize(FileUtil.size(tempFile));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            //返回可访问的地址
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("file upload error, filepath = " + upLoadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            deleteTemplate(tempFile);
        }
    }

    /**
     * 校验文件是否符合规范
     *
     * @param multipartFile
     */
    private void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        long size = multipartFile.getSize();
        final long ONE_M = 1024 * 1024;
        ThrowUtils.throwIf(size > ONE_M * 2, ErrorCode.PARAMS_ERROR, "文件大小不能超过2MB");

        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());

        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "png", "webp", "jpeg", "gif");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件格式不支持");
    }

    /**
     * 校验文件是否符合规范
     *
     * @param fileUrl 文件URL
     */
    private void validPicture(String fileUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 2. URL格式校验
        ThrowUtils.throwIf(!StrUtil.startWithAny(fileUrl.toLowerCase(), "http://", "https://"),
                ErrorCode.PARAMS_ERROR, "文件URL格式不正确，必须以http://或https://开头");

        try (HttpResponse response = HttpUtil.createRequest(Method.HEAD, fileUrl)
                .setFollowRedirects(true)
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
                    final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
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
     * 清理文件
     *
     * @param tempFile
     */
    public void deleteTemplate(File tempFile) {
        if (tempFile != null) {
            // 删除临时文件
            boolean delete = tempFile.delete();
            if (!delete) {
                log.error("file delete error, filepath = {}", tempFile.getAbsolutePath());
            }
        }
    }
}
