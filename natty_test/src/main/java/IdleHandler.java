import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;


public class IdleHandler {


        public static void main(String[] args) throws InterruptedException {
            EventLoopGroup bossGroup = new NioEventLoopGroup(), workerGroup = new NioEventLoopGroup();  //线程数先限制一下
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) {
                            channel.pipeline()
                                    .addLast(new StringDecoder())
                                    .addLast(new IdleStateHandler(10, 10, 0))  //IdleStateHandler能够侦测连接空闲状态
                                    //第一个参数表示连接多少秒没有读操作时触发事件，第二个是写操作，第三个是读写操作都算，0表示禁用
                                    //事件需要在ChannelInboundHandlerAdapter中进行监听处理
                                    .addLast(new ChannelInboundHandlerAdapter(){
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                            System.out.println("收到客户端数据："+msg);
                                            ctx.channel().writeAndFlush("已收到！");
                                        }

                                        @Override
                                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                            //没想到吧，这个方法原来是在这个时候用的
                                            if(evt instanceof IdleStateEvent) {
                                                IdleStateEvent event = (IdleStateEvent) evt;
                                                if(event.state() == IdleState.WRITER_IDLE) {
                                                    System.out.println("好久都没写了，看视频的你真的有认真在跟着敲吗");
                                                } else if(event.state() == IdleState.READER_IDLE) {
                                                    System.out.println("已经很久很久没有读事件发生了，好寂寞");
                                                }
                                            }
                                        }
                                    })
                                    .addLast(new StringEncoder());

                        }
                    });
            ChannelFuture channelFuture = bootstrap.bind(8080);
            channelFuture.sync();   //让当前线程同步等待任务完成

            //直接添加监听器，当任务完成时自动执行，但是注意执行也是异步的，不是在当前线程
            /* channelFuture.addListener(f -> System.out.println("我是服务端启动完成之后要做的事情！"));*/
            System.out.println("服务端启动状态："+channelFuture.isDone());
            System.out.println("我是服务端启动完成之后要做的事情！");
        }



}


