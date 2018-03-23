package cn.panda;


import cn.panda.conf.NettyConf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * @author ZhuYunpeng
 * woscaizi@gmail.com
 * 2018-03-23
 */
public class MyWebSocketHandler extends SimpleChannelInboundHandler<Object> {

    Logger logger = LoggerFactory.getLogger(getClass());

    private WebSocketServerHandshaker handshaker;
    private static final String WEB_SOCKET_URL = "ws://localhost:7001/websocket";


    /**
     * 服务端处理客户端websocket请求的核心方法
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {


        //处理客户端向服务端发起http握手请求的业务
        if(msg instanceof FullHttpRequest){
            //msg强转
            handHttpRequest(ctx, (FullHttpRequest) msg);
        }else if(msg instanceof WebSocketFrame){    //处理websocket连接业务
            handWebSocketFrame(ctx,(WebSocketFrame) msg);
        }


    }


    /**
     * 工程出现异常的时候调用
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        cause.printStackTrace();
        ctx.close();
    }


    /**
     * 客户端与服务端创建链接时调用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

            //netty配置类保存channel
        NettyConf.group.add(ctx.channel());

        logger.info("客户端与服务端建立链接……");

//        super.channelActive(ctx);
    }


    /**
     * 客户端与服务端断开链接时调用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        NettyConf.group.remove(ctx.channel());

        logger.info("客户端与服务端断开链接……");
    }


    /**
     * 服务端接收客户端发送来数据之后调用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

        ctx.flush();
    }


    /**
     * 处理客户端向服务端发起http握手请求的业务
     * @param ctx
     * @param request
     */
    private void handHttpRequest(ChannelHandlerContext ctx,FullHttpRequest request){

        if(!request.getDecoderResult().isSuccess() || !("websocket").equals(request.headers().get("Upgrade"))){
            sendHttpRespponse(ctx,request,new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.BAD_REQUEST));
            return;
        }


        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(WEB_SOCKET_URL,null,false);

        //创建handShaker
        handshaker = wsFactory.newHandshaker(request);

        if(handshaker == null){
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        }else{
            handshaker.handshake(ctx.channel(),request);
        }

    }


    /**
     * 服务端向客户端响应消息
     * @param ctx
     * @param request
     * @param response
     */
    private void sendHttpRespponse(ChannelHandlerContext ctx, FullHttpRequest request, DefaultFullHttpResponse response){

        //如果请求失败
       if(response.getStatus().code() != 200){
           ByteBuf buf = Unpooled.copiedBuffer(response.getStatus().toString(),CharsetUtil.UTF_8);
            response.content().writeBytes(buf);
            buf.release();
       }

       //服务端向客户都按发送数据
        ChannelFuture channelFuture = ctx.channel().writeAndFlush(response);

       //请求失败
        if(response.getStatus().code() != 200){

            //关闭连接
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }


    /**
     * 处理客户端与服务端之间的websocket业务
     * @param ctx
     * @param frame
     */
    private void handWebSocketFrame(ChannelHandlerContext ctx,WebSocketFrame frame){

        //判断是否是关闭websocket的指令
        if(frame instanceof CloseWebSocketFrame){
            handshaker.close(ctx.channel(),((CloseWebSocketFrame) frame).retain());
        }

        //判断是否是ping消息

        if(frame instanceof PingWebSocketFrame){
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        // 判断是否是二进制消息，如果是，抛出异常


        if(!(frame instanceof TextWebSocketFrame)){

            logger.info("目前我们不支持二进制消息");
            throw new RuntimeException("【"+this.getClass().getName()+"】不支持消息");

        }


        //返回应答消息
        //获取客户端向服务端发送的消息
        String request = ((TextWebSocketFrame) frame).text();
        logger.info("服务端收到客户都按的消息======>>>"+request);

        TextWebSocketFrame tws = new TextWebSocketFrame(new Date().toString()+ctx.channel().id()+"====>>>"+request);

        //群发，服务端向每个连接的客户端群发消息

        NettyConf.group.writeAndFlush(tws);

    }


}
