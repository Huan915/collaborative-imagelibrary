package com.huan.huanpicture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huan.huanpicture.api.aliyun.model.CreateOutPaintingTaskResponse;
import com.huan.huanpicture.model.dto.picture.*;
import com.huan.huanpicture.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.huan.huanpicture.model.entity.User;
import com.huan.huanpicture.model.vo.PictureVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Huan
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-11-20 10:28:24
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片 （pictureId为空表示新增图片，不为空表示修改）
     *
     * @param pictureUploadRequest （pictureId为空表示新增图片，不为空表示修改）
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);


    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片包装类（单条）
     */
    PictureVO getPictureVO(Picture picture);

    /**
     * 获取图片包装类（分页）
     */
    Page<PictureVO> getPicturePage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 校验图片参数
     */
    void validPicture(Picture picture);

    /**
     * 图片审核
     *
     * @param pictureReviewRequest 图片审核请求
     * @param loginUser            当前登录用户
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取图片
     *
     * @param loginUser
     * @return 抓取成功的图片数量
     */
    Integer uploadPictureByBatch(PictureByBatchRequest pictureByBatchRequest, User loginUser);

    void clearPictureFile(Picture oldPicture);

    void checkPictureAuth(User loginUser, Picture picture);

    void deletePicture(Long id, User loginUser);

    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    /**
     * 图像扩展
     *
     * @param createPictureOutPaintingTaskRequest
     * @param loginUser
     * @return
     */
    CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);
}
