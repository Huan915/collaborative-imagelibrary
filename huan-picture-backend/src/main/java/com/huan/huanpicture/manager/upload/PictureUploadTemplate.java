package com.huan.huanpicture.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.huan.huanpicture.config.CosClientConfig;
import com.huan.huanpicture.exception.BusinessException;
import com.huan.huanpicture.exception.ErrorCode;
import com.huan.huanpicture.manager.CosManager;
import com.huan.huanpicture.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public abstract class PictureUploadTemplate {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;


    /**
     *  上传图片
     * @param inputSource
     * @param uploadPathPrefix
     * @return
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 1、校验图片
        validPicture(inputSource);
        // 2、图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = getOriginalFilename(inputSource);
        // 自己拼接文件上传路径，而不是使用原始文件名称，可以增加安全性
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originalFilename));
        String upLoadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);

        File tempFile = null;
        try {
            // 3、创建临时文件，获取文件到服务器
            tempFile = File.createTempFile(upLoadPath, null);
            // 处理文件来源
            processFile(inputSource, tempFile);
            // 4、上传图片到对象存储
            PutObjectResult putObjectResult = cosManager.putPictureObject(upLoadPath, tempFile);
            // 5、获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (CollUtil.isNotEmpty(objectList)) {
                // 获取压缩后得到的文件信息
                CIObject compressedCiObject = objectList.get(0);
                // 缩略图默认等于压缩图
                CIObject thunbnailedCiObject = compressedCiObject;
                if (objectList.size() > 1) {
                    //封装压缩图的返回结果
                    thunbnailedCiObject = objectList.get(1);
                }
                return buildResult(originalFilename, compressedCiObject, thunbnailedCiObject);
            }

            return buildResult(imageInfo, upLoadPath, originalFilename, tempFile);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + upLoadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 6、临时文件清理
            deleteTemplate(tempFile);
        }
    }

    private UploadPictureResult buildResult(String originalFilename, CIObject compressedCiObject, CIObject thumbnailCiObject) {
// 计算宽高
        int picWidth = compressedCiObject.getWidth();
        int picHeight = compressedCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        // 封装返回结果
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        // 设置原图地址
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressedCiObject.getKey());
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(compressedCiObject.getSize().longValue());
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressedCiObject.getFormat());
        // 设置缩略图地址
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailCiObject.getKey());
        //返回可访问的地址
        return uploadPictureResult;
    }

    /**
     *  构造返回对象
     * @param imageInfo
     * @param upLoadPath
     * @param originalFilename
     * @param file
     * @return
     */
    private UploadPictureResult buildResult(ImageInfo imageInfo, String upLoadPath, String originalFilename, File file) {
        // 计算宽高
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        // 封装返回结果
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + upLoadPath);
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        //返回可访问的地址
        return uploadPictureResult;
    }

    /**
     *  校验输入源 （本地文件或URL）
     * @param inputSource
     */
    protected abstract void validPicture(Object inputSource);

    /**
     *  获取原始文件名
     * @param inputSource
     * @return
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     *  处理输入源生成本地临时文件
     * @param tempFile
     */
    protected abstract void processFile(Object tempFile, File file) throws Exception;

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
