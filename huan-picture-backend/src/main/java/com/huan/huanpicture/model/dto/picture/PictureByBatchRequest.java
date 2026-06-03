package com.huan.huanpicture.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureByBatchRequest implements Serializable {
    /**
     * 抓取关键字
     */
    private String searchText;

    /**
     * 抓取条数，默认为10， 不超过30条
     */
    private int count = 10;

    /**
     * 图片名称前缀
     */
    private String namePrefix;

    private static final long serialVersionUID = 9025694504474321360L;

}
