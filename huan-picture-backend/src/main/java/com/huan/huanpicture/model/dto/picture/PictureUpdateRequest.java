package com.huan.huanpicture.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片更新请求
 */
@Data
public class PictureUpdateRequest implements Serializable {

    /**
     * 图片 id(用户修改)
     */
    private Long id;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签（JSON 数组）
     */
    private String tags;

    private static final long serialVersionUID = 2280060552168145941L;
}
