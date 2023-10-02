import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.nio.charset.StandardCharsets;


public class Server {




    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(), workerGroup = new NioEventLoopGroup(1);  //线程数先限制一下
        EventLoopGroup handlerGroup = new DefaultEventLoopGroup();  //使用DefaultEventLoop来处理其他任务
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) {
                        channel.pipeline()
                                .addLast(new ChannelInboundHandlerAdapter(){
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        ByteBuf buf = (ByteBuf) msg;
                                        System.out.println("接收到客户端发送的数据："+buf.toString(StandardCharsets.UTF_8));
                                        ctx.fireChannelRead(msg);
                                    }
                                }).addLast(handlerGroup, new ChannelInboundHandlerAdapter(){  //在添加时，可以直接指定使用哪个EventLoopGroup
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        try {
                                            Thread.sleep(10000);
                                        } catch (InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                        ctx.writeAndFlush(Unpooled.wrappedBuffer("已收到！".getBytes()));
                                    }
                                });
                    }
                });
        bootstrap.bind(8080);
    }




    /*@Override
    protected void initChannel(SocketChannel channel) {
        channel.pipeline()   //直接获取pipeline，然后添加两个Handler
                .addLast(new ChannelInboundHandlerAdapter(){
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        ByteBuf buf = (ByteBuf) msg;
                        System.out.println("1接收到客户端发送的数据："+buf.toString(StandardCharsets.UTF_8));
                        ctx.fireChannelRead(msg);
                    }
                })
                .addLast(new ChannelInboundHandlerAdapter(){
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        ByteBuf buf = (ByteBuf) msg;
                        System.out.println("2接收到客户端发送的数据："+buf.toString(StandardCharsets.UTF_8));
                        ctx.channel().writeAndFlush("伞兵一号卢本伟");  //这里我们使用channel的write
                    }
                })
                .addLast(new ChannelOutboundHandlerAdapter(){
                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                        System.out.println("1号出站："+msg);
                    }
                })
                .addLast(new ChannelOutboundHandlerAdapter(){
                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                        System.out.println("2号出站："+msg);
                        ctx.write(msg);  //继续write给其他的出站Handler，不然到这里就断了
                    }
                });
    }*/
}
