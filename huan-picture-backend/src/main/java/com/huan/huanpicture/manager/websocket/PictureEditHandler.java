package com.huan.huanpicture.manager.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.huan.huanpicture.manager.websocket.disruptor.PictureEditEventProducer;
import com.huan.huanpicture.manager.websocket.model.PictureEditActionEnum;
import com.huan.huanpicture.manager.websocket.model.PictureEditMessageTypeEnum;
import com.huan.huanpicture.manager.websocket.model.PictureEditRequestMessage;
import com.huan.huanpicture.manager.websocket.model.PictureEditResponseMessage;
import com.huan.huanpicture.model.entity.User;
import com.huan.huanpicture.model.vo.UserVO;
import com.huan.huanpicture.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图片编辑 WebSocket 处理器
 */
@Component
@Slf4j
public class PictureEditHandler extends TextWebSocketHandler {

    @Resource
    private UserService userService;

    @Lazy
    @Resource
    private PictureEditEventProducer pictureEditEventProducer;

    // 保存每张图片的编辑状态
    private final Map<Long, Long> pictureEditUsers = new ConcurrentHashMap<>();

    // 保存所有会话的连接
    private final Map<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();

    /**
     * 连接建立成功
     *
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        // 保存会话到集合中
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        pictureSessions.computeIfAbsent(pictureId, k -> ConcurrentHashMap.newKeySet()).add(session);
        // 构造响应，发送加入编辑的消息通知
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("用户：%s, 加入编辑", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        UserVO userVO = userService.getUserVO(user);
        pictureEditResponseMessage.setUser(userVO);
        // 广播给所有用户
        broadcastToPicture(pictureId, pictureEditResponseMessage);

    }

    /**
     * 收到前端发送的消息，根据消息类别处理消息
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
      // 获取消息内容，将 JSON 转换为 PictureEditRequestMessage
        PictureEditRequestMessage pictureEditRequestMessage = JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);
        // 从 Session 属性中获取到公共参数
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        pictureEditEventProducer.publishEvent(pictureEditRequestMessage, session, user, pictureId);
    }

    /**
     * 进入编辑状态
     *
     * @param pictureEditRequestMassage
     * @param session
     * @param user
     * @param pictureId
     */
    public void handleEnterEditMessage(PictureEditRequestMessage pictureEditRequestMassage, WebSocketSession session, User user, Long pictureId) throws IOException {
        // 判断当前是否有用户正在编辑图片
        if (!pictureEditUsers.containsKey(pictureId)) {
            // 没有用户编辑该图片，设置当前用户正在编辑
            pictureEditUsers.put(pictureId, user.getId());
            // 构造响应，发送加入编辑的消息通知
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
            String message = String.format("用户：%s, 开始编辑图片", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            UserVO userVO = userService.getUserVO(user);
            pictureEditResponseMessage.setUser(userVO);
            // 广播给所有用户
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }

    /**
     * 退出编辑状态
     *
     * @param pictureEditRequestMassage
     * @param session
     * @param user
     * @param pictureId
     */
    public void handleExitEditMessage(PictureEditRequestMessage pictureEditRequestMassage, WebSocketSession session, User user, Long pictureId) throws IOException {
        // 正在编辑的用户
        Long editingUserId = pictureEditUsers.get(pictureId);

        // 确认是否为当前编辑者
        if (editingUserId != null && editingUserId.equals(user.getId())) {
            pictureEditUsers.remove(pictureId);
            // 构造相应， 发送退出编辑的消息通知
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
            String message = String.format("用户：%s, 退出编辑图片", user.getUserName());
            pictureEditResponseMessage.setMessage(message);
            UserVO userVO = userService.getUserVO(user);
            pictureEditResponseMessage.setUser(userVO);
            // 广播给所有用户
            broadcastToPicture(pictureId, pictureEditResponseMessage);
        }
    }

    /**
     * 执行编辑操作
     *
     * @param pictureEditRequestMassage
     * @param session
     * @param user
     * @param pictureId
     */
    public void handleEditActionMessage(PictureEditRequestMessage pictureEditRequestMassage, WebSocketSession session, User user, Long pictureId) throws IOException {
        Long editUserId = pictureEditUsers.get(pictureId);
        String editAction = pictureEditRequestMassage.getEditAction();
        PictureEditActionEnum actionEnum = PictureEditActionEnum.getEnumByValue(editAction);
        if (actionEnum == null) {
            log.error("无效的编辑操作");
            return;
        }
        // 确认是当前的编辑者
        if (editUserId != null && editUserId.equals(user.getId())) {
            // 构造响应，发送执行的具体编辑操作信息
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            String message = String.format("%s执行%S操作", user.getUserName(), actionEnum.getText());
            pictureEditResponseMessage.setEditAction(editAction);
            pictureEditResponseMessage.setMessage(message);
            UserVO userVO = userService.getUserVO(user);
            pictureEditResponseMessage.setUser(userVO);
            // 广播给除了当前客户端之外的其他用户，否则会造成重复编辑
            broadcastToPicture(pictureId, pictureEditResponseMessage, session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        // 移除当前用户的编辑状态
        // 从Session中获取公共属性
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        // 移除当前用户编辑状态
        handleExitEditMessage(null, session, user, pictureId);

        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
        if (sessionSet != null) {
            sessionSet.remove(session);
            if (sessionSet.isEmpty()) {
                pictureSessions.remove(pictureId);
            }
        }
        // 通知其他用户（如果有），该用户已经离开编辑
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        String message = String.format("%s离开编辑", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userService.getUserVO(user));
        // 广播给所有用户
        broadcastToPicture(pictureId, pictureEditResponseMessage);

    }

    /**
     * 广播给该图片的所有session (排除自身)
     *
     * @param pictureId
     * @param pictureEditResponseMessage
     * @param excludeSession
     * @throws IOException
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage, WebSocketSession excludeSession) throws IOException {
        Set<WebSocketSession> webSocketSessions = pictureSessions.get(pictureId);
        if (CollUtil.isNotEmpty(webSocketSessions)) {

            ObjectMapper objectMapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            objectMapper.registerModule(module);

            String str = objectMapper.writeValueAsString(pictureEditResponseMessage);
            TextMessage textMessage = new TextMessage(str);
            for (WebSocketSession session : webSocketSessions) {
                // 排除掉的 session 不发送
                if (session.equals(excludeSession)) {
                    continue;
                }
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            }
        }
    }

    /**
     * 广播给该图片的所有session (不排除自身)
     *
     * @param pictureId
     * @param pictureEditResponseMessage
     * @throws IOException
     */
    private void broadcastToPicture(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage) throws IOException {
        broadcastToPicture(pictureId, pictureEditResponseMessage, null);
    }
}
