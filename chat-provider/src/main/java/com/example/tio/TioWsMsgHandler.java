package com.example.tio;


import cn.hutool.core.bean.BeanUtil;
import com.example.stage.core.system.domain.model.LoginUser;
import com.example.stage.core.web.service.TokenService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.api.entity.Message;
import com.example.api.entity.Receipt;
import com.example.api.entity.SendInfo;
import com.example.api.service.VimGroupApiService;
import com.example.api.service.VimMessageService;
import com.example.api.vo.Group;
import com.example.vim.utils.ChatUtils;
import com.example.vim.utils.SendCodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.TioConfig;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;
import org.tio.http.common.HttpResponseStatus;
import org.tio.utils.hutool.Snowflake;
import org.tio.utils.lock.SetWithLock;
import org.tio.websocket.common.WsRequest;
import org.tio.websocket.common.WsResponse;
import org.tio.websocket.server.handler.IWsMsgHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * websocket 处理函数
 *
 * @author 乐天
 * @since 2018-10-08
 */
@Component
public class TioWsMsgHandler implements IWsMsgHandler {

    private static final Logger log = LoggerFactory.getLogger(TioWsMsgHandler.class);

    public static TioConfig tioConfig;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private VimGroupApiService vimGroupApiService;

    @Autowired
    private VimMessageService vimMessageService;


    /**
     * 握手时走这个方法，业务可以在这里获取cookie，request参数等
     *
     * @param request        request
     * @param httpResponse   httpResponse
     * @param channelContext channelContext
     * @return HttpResponse
     */
    @Override
    public HttpResponse handshake(HttpRequest request, HttpResponse httpResponse, ChannelContext channelContext) {
        try {
            tioConfig = channelContext.tioConfig;
            String token = request.getParam("token");
            LoginUser loginUser = tokenService.getLoginUser(token);
            Long userId = loginUser.getUserId();
            //先关闭原先的连接
            Tio.closeUser(tioConfig, String.valueOf(userId), null);
            //绑定用户
            Tio.bindUser(channelContext, String.valueOf(userId));
            // 在线用户绑定到上下文 用于发送在线消息
            WsOnlineContext.bindUser(String.valueOf(userId), channelContext);
            // 绑定群组
            List<Group> groups = vimGroupApiService.getGroups(String.valueOf(userId));
            for (Group group : groups) {
                Tio.bindGroup(channelContext, String.valueOf(group.getId()));
            }
        } catch (Exception e) {
            log.error("websocket 连接失败：{}", e.getMessage());
            httpResponse.setStatus(HttpResponseStatus.getHttpStatus(401));
        }
        return httpResponse;
    }

    /**
     * @param httpRequest    httpRequest
     * @param httpResponse   httpResponse
     * @param channelContext channelContext
     * @author tanyaowu tanyaowu
     */
    @Override
    public void onAfterHandshaked(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext){

    }

    /**
     * 字节消息（binaryType = arraybuffer）过来后会走这个方法
     */
    @Override
    public Object onBytes(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext)  {
        return null;
    }

    /**
     * 当客户端发close flag时，会走这个方法
     */
    @Override
    public Object onClose(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext)  {
        Tio.remove(channelContext, "receive close flag");
        return null;
    }

    /**
     * 字符消息（binaryType = blob）过来后会走这个方法
     *
     * @param wsRequest      wsRequest
     * @param text           text
     * @param channelContext channelContext
     * @return obj
     */
    @Override
    public Object onText(WsRequest wsRequest, String text, ChannelContext channelContext) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SendInfo sendInfo = objectMapper.readValue(text, SendInfo.class);

            //心跳检测包
            if (SendCodeUtils.PING.equals(sendInfo.getCode())) {
                WsResponse wsResponse = WsResponse.fromText(text, TioServerConfig.CHARSET);
                Tio.send(channelContext, wsResponse);
            }
            //真正的消息
            else if (SendCodeUtils.MESSAGE.equals(sendInfo.getCode())) {
                acceptMessage(channelContext, objectMapper, sendInfo);
            }
            //准备就绪，需要发送离线消息
            else if (SendCodeUtils.READY.equals(sendInfo.getCode())) {
                //未读消息
                sendOffLineMessage(channelContext, objectMapper);
            }
            //读消息
            else if (SendCodeUtils.READ.equals(sendInfo.getCode())) {
                read(sendInfo);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        //返回值是要发送给客户端的内容，一般都是返回null
        return null;
    }

    /**
     * 读取消息
     * @param sendInfo 消息
     */
    private void read(SendInfo sendInfo) {
        Map<String, Object> map = sendInfo.getMessage();
        Receipt receipt = BeanUtil.fillBeanWithMap(map, new Receipt(), false);
        vimMessageService.read(receipt.getChatId(), receipt.getUserId(), receipt.getType(), System.currentTimeMillis());
    }

    /**
     * 发送未读消息
     *
     * @param channelContext channelContext
     * @param objectMapper   objectMapper
     * @throws IOException 抛出异常
     */
    private void sendOffLineMessage(ChannelContext channelContext, ObjectMapper objectMapper) throws Exception {
        String userId = channelContext.userid;
        TioConfig tioConfig = channelContext.tioConfig;
        sendMessage(tioConfig, objectMapper, vimMessageService.unreadList(userId,null), userId);
        //获取用户所有的群
        List<Group> groups = vimGroupApiService.getGroups(userId);
        for (Group group : groups) {
            //发送群离线消息
            sendMessage(tioConfig, objectMapper, vimMessageService.unreadGroupList(userId, group.getId()), userId);
        }
    }

    /**
     * 发送消息
     *
     * @param tioConfig    tioConfig
     * @param objectMapper 格式化
     * @param messageList  消息列表
     * @throws JsonProcessingException throws
     */
    private void sendMessage(TioConfig tioConfig, ObjectMapper objectMapper, List<Message> messageList, String userId) throws Exception {
        if (messageList != null) {
            for (Message message : messageList) {
                vimMessageService.save(message, true);
                SendInfo sendInfo = new SendInfo();
                sendInfo.setCode(SendCodeUtils.MESSAGE);
                sendInfo.setMessage(BeanUtil.beanToMap(message));
                WsResponse wsResponse = WsResponse.fromText(objectMapper.writeValueAsString(sendInfo), TioServerConfig.CHARSET);
                Tio.sendToUser(tioConfig, userId, wsResponse);
            }
        }
    }

    /**
     * 接受消息
     *
     * @param channelContext channelContext
     * @param objectMapper   objectMapper
     * @param sendInfo       sendInfo
     * @throws JsonProcessingException 异常
     */
    private void acceptMessage(ChannelContext channelContext, ObjectMapper objectMapper, SendInfo sendInfo) throws Exception {
        Map<String, Object> map = sendInfo.getMessage();
        map.put("mine", false);
        map.put("timestamp", System.currentTimeMillis());
        map.put("id", String.valueOf(new Snowflake(1L, 1L).nextId()));
        Message message = BeanUtil.fillBeanWithMap(map, new Message(), false);
        WsResponse wsResponse = WsResponse.fromText(objectMapper.writeValueAsString(sendInfo), TioServerConfig.CHARSET);

        String chatId = String.valueOf(message.getChatId());
        //单聊
        if (ChatUtils.MESSAGE_TYPE_FRIEND.equals(message.getType())) {
            SetWithLock<ChannelContext> channelContextSetWithLock = Tio.getByUserid(channelContext.tioConfig, chatId);
            //用户没有登录，存储到离线文件
            if (channelContextSetWithLock == null || channelContextSetWithLock.size() == 0) {
                vimMessageService.save(message, false);
            } else {
                //入库操作
                vimMessageService.save(message, true);
                Tio.sendToUser(channelContext.tioConfig, chatId, wsResponse);
            }
            Tio.sendToUser(channelContext.tioConfig, message.getFromId(), wsResponse);
        } else {
            Tio.sendToGroup(channelContext.tioConfig, chatId, wsResponse);
            //入库操作
            vimMessageService.save(message, true);
        }
    }

}
