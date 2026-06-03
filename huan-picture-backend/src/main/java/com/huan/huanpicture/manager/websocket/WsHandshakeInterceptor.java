package com.huan.huanpicture.manager.websocket;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.huan.huanpicture.enums.SpaceTypeEnum;
import com.huan.huanpicture.manager.auth.SpaceUserAuthManager;
import com.huan.huanpicture.manager.auth.model.SpaceUserPermissionConstant;
import com.huan.huanpicture.model.entity.Picture;
import com.huan.huanpicture.model.entity.Space;
import com.huan.huanpicture.model.entity.User;
import com.huan.huanpicture.service.PictureService;
import com.huan.huanpicture.service.SpaceService;
import com.huan.huanpicture.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * WebSocket 拦截器， 建立连接前要先校验
 */
@Slf4j
@Component
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        log.info("websocket正在连接");
        // 获取当前登录用户
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            // 从请求中获取参数
            String pictureId = servletRequest.getParameter("pictureId");
            if (StrUtil.isBlank(pictureId)) {
                log.error("缺少图片参数");
                return false;
            }
            User loginUser = userService.getLoginUser(servletRequest);
            if (ObjUtil.isEmpty(loginUser)) {
                log.error("用户未登录，拒绝握手");
                return false;
            }
            // 判断当前用户是否具有操作权限
            Picture picture = pictureService.getById(pictureId);
            if (ObjUtil.isEmpty(picture)) {
                log.error("图片不存在， 拒绝握手");
                return false;
            }
            Long spaceId = picture.getSpaceId();
            Space space;
            if (spaceId != null) {
                space = spaceService.getById(spaceId);
                if (ObjUtil.isEmpty(space)) {
                    log.error("图片所在空间不存在，拒绝握手");
                    return false;
                }
                if (space.getSpaceType() != SpaceTypeEnum.TEAM.getValue()) {
                    log.error("图片所在空间不是团队空间，拒绝握手");
                    return false;
                }
                List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
                if (!permissionList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)) {
                    log.error("用户没有编辑图片的权限，拒绝握手");
                    return false;
                }
                // 设置登录信息到WebSocket会话中
                attributes.put("userId", loginUser.getId());
                attributes.put("pictureId", Long.valueOf(pictureId));
                attributes.put("user", loginUser);
            }
            return true;
        }
        // 如果是团队空间，且具有编辑者权限，才能建立连接

        // 保存用户登录信息等属性到WebSocket会话中
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
