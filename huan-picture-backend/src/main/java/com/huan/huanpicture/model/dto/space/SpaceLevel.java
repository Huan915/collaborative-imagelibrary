package com.huan.huanpicture.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class SpaceLevel implements Serializable {
    /**
     * 值
     */
    private int value;

    /**
     * 说明
     */
    private String text;

    /**
     * 最大数量
     */
    private long maxCount;

    /**
     * 最大容量
     */
    private long maxSize;

    private static final long serialVersionUID = 8861283138685725103L;
}
