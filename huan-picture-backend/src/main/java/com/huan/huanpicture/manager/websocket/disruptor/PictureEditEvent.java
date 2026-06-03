package com.huan.huanpicture.manager.websocket.disruptor;

import com.huan.huanpicture.manager.websocket.model.PictureEditRequestMessage;
import com.huan.huanpicture.model.entity.User;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

/**
 * 图片编辑时间
 */
@Data
public class PictureEditEvent {

    /**
     * 消息
     */
    PictureEditRequestMessage pictureEditRequestMessage;

    /**
     * 当前客户端连接
     */
    WebSocketSession session;

    /**
     * 当前用户信息
     */
    User user;

    /**
     * 当前操作的图片ID
     */
    Long pictureId;

}
