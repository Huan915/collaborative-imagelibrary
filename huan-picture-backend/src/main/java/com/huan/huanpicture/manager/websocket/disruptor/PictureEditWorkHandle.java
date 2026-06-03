package com.huan.huanpicture.manager.websocket.disruptor;

import cn.hutool.json.JSONUtil;
import com.huan.huanpicture.manager.websocket.PictureEditHandler;
import com.huan.huanpicture.manager.websocket.model.PictureEditMessageTypeEnum;
import com.huan.huanpicture.manager.websocket.model.PictureEditRequestMessage;
import com.huan.huanpicture.manager.websocket.model.PictureEditResponseMessage;
import com.huan.huanpicture.model.entity.User;
import com.huan.huanpicture.service.UserService;
import com.lmax.disruptor.WorkHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;

/**
 * 图片编辑事件处理器 （消费者）
 */
@Component
@Slf4j
public class PictureEditWorkHandle implements WorkHandler<PictureEditEvent> {

    @Resource
    private PictureEditHandler pictureEditHandler;

    @Resource
    private UserService userService;

    @Override
    public void onEvent(PictureEditEvent pictureEditEvent) throws Exception {
        Long pictureId = pictureEditEvent.getPictureId();
        User user = pictureEditEvent.getUser();
        WebSocketSession session = pictureEditEvent.getSession();
        PictureEditRequestMessage PictureEditRequestMessage = pictureEditEvent.getPictureEditRequestMessage();
        String editType = PictureEditRequestMessage.getType();
        PictureEditMessageTypeEnum pictureEditMessageTypeEnum = PictureEditMessageTypeEnum.getEnumByValue(editType);

        //根据消息类型处理消息
        switch (pictureEditMessageTypeEnum) {
            case ENTER_EDIT:
                pictureEditHandler.handleEnterEditMessage(PictureEditRequestMessage, session, user, pictureId);
                break;
            case EXIT_EDIT:
                pictureEditHandler.handleExitEditMessage(PictureEditRequestMessage, session, user, pictureId);
                break;
            case EDIT_ACTION:
                pictureEditHandler.handleEditActionMessage(PictureEditRequestMessage, session, user, pictureId);
                break;
            default:
                // 其他类型消息，返回错误提示
                PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
                pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ERROR.getValue());
                pictureEditResponseMessage.setUser(userService.getUserVO(user));
                pictureEditResponseMessage.setMessage("消息类型错误");
                session.sendMessage(new TextMessage(JSONUtil.toJsonStr(pictureEditResponseMessage)));
                break;
        }
    }
}
