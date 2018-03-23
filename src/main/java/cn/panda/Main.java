package cn.panda;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ZhuYunpeng
 * woscaizi@gmail.com
 * 2018-03-23
 * 程序的入口，负责程序启动
 */
public class Main {


    static Logger logger = LoggerFactory.getLogger("Main");

    public static void main(String[] args) throws InterruptedException {


        EventLoopGroup boosGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();


        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(boosGroup,workGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childHandler(new MyWebSocketChannnelHandler());

            logger.info("服务端开启等待客户端连接……");

            Channel channel =  serverBootstrap.bind(7001).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {

            boosGroup.shutdownGracefully();
            workGroup.shutdownGracefully();

        }

    }


}
