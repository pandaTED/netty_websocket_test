package cn.panda;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author ZhuYunpeng
 * woscaizi@gmail.com
 * 2018-03-23
 */
public class MyWebSocketChannnelHandler extends ChannelInitializer<SocketChannel> {


    /**
     * 初始化连接时的各个组件
     * @param socketChannel
     * @throws Exception
     */
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {

        socketChannel.pipeline().addLast("http-codec",new HttpServerCodec());
        socketChannel.pipeline().addLast("aggregator",new HttpObjectAggregator(65536));
        socketChannel.pipeline().addLast("http-chuncked",new ChunkedWriteHandler());
        socketChannel.pipeline().addLast("handler",new MyWebSocketHandler());

    }


}
