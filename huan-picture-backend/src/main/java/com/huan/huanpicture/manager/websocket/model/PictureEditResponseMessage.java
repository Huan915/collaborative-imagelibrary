package com.huan.huanpicture.manager.websocket.model;

import com.huan.huanpicture.model.vo.UserVO;
import lombok.Data;

import java.io.Serializable;

@Data
public class PictureEditResponseMessage implements Serializable {
    /**
     * 消息类型，例如“INFO”， “ERROR”， “ENTER_INFO” "EXIT_EDIT", "EDIT_ACTION"
     */
    private String type;

    /**
     * 信息
     */
    private String message;

    /**
     * 执行的编辑动作
     */
    private String editAction;

    /**
     * 用户信息
     */
    private UserVO user;

    private static final long serialVersionUID = -1408330187390694867L;
}
