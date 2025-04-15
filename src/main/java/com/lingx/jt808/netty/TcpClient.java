package com.lingx.jt808.netty;

import com.lingx.jt808.JT808Tools;
import com.lingx.jtools.ui.JT808ClientPanel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

public class TcpClient {
	public static Channel channel=null;

	 public static Channel doRequest(String ip,int port){
		 Channel channel=null;
	      /**处理请求和服务端响应数据的线程组*/
		 NioEventLoopGroup workerGroup = new NioEventLoopGroup();
	      try {
	         /**客户端相关配置信息*/
	         Bootstrap bootstrap = new Bootstrap();
	         //绑定线程组
	         bootstrap.group(workerGroup);
	         bootstrap.channel(NioSocketChannel.class);
	         bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
	         /*
	          * 客户端必须绑定处理器，也就是必须调用handler方法
	          */
	         bootstrap.handler(new ChannelInitializer<SocketChannel>() {
	            @Override
	            protected void initChannel(SocketChannel ch) throws Exception {

	               ch.pipeline().addLast("decoder", new ProtocolDecoder());
	        	   ch.pipeline().addLast("encoder", new ByteArrayEncoder());
	               ch.pipeline().addLast(new TcpClientHandler());
	            }
	         });

	         ChannelFuture future = bootstrap.connect(ip, port).sync();
	         channel=future.channel();
	         future.channel().closeFuture().sync();
	      } catch (Exception e) {
	        // e.printStackTrace();

	 		JT808ClientPanel.setButtunZt1();
	 		JT808Tools.callAardio("服务器连接失败"+ip+":"+port);
	      }finally{
	         workerGroup.shutdownGracefully();
	      } 
	      return channel;
	   }
}
