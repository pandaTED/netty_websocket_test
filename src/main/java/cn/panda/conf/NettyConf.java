package cn.panda.conf;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author ZhuYunpeng
 * woscaizi@gmail.com
 * 2018-03-23
 * 整个工程的全局配置
 */
public class NettyConf {

    /**
     * 存储每一个客户端接入进来的channel对象
     */
    public static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

}
