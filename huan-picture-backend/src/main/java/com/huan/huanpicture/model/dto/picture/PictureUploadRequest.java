package com.huan.huanpicture.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {

    /**
     * 图片 id(用户修改)
     */
    private Long id;

    /**
     * 要上传的图片地址
     */
    private String url;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 空间ID
     */
    private Long spaceId;

    private static final long serialVersionUID = 7632042109413247258L;
}
