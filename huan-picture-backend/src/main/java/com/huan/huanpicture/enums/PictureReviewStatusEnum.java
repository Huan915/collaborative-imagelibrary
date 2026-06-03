package com.huan.huanpicture.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 图片审核状态枚举
 */
@Getter
public enum PictureReviewStatusEnum {

    REVIEWING("待审核", 0),
    PASS("通过", 1),
    REJECT("拒绝", 2);

    private final String text;
    private final int reviewStatus;

    PictureReviewStatusEnum(String text, int reviewStatus) {
        this.text = text;
        this.reviewStatus = reviewStatus;
    }

    /**
     * 根据value 获取对应枚举
     *
     * @param value 枚举值的value
     * @return 返回对应枚举
     */
    public static PictureReviewStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (PictureReviewStatusEnum pictureReviewStatusEnum : PictureReviewStatusEnum.values()) {
            if (pictureReviewStatusEnum.reviewStatus == value) {
                return pictureReviewStatusEnum;
            }
        }
        return null;
    }
}
