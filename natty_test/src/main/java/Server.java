import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.StandardCharsets;


public class Server {




    public static void main(String[] args) throws InterruptedException {
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
                                //解码器本质上也算是一种ChannelInboundHandlerAdapter，用于处理入站请求
                                //.addLast(new StringDecoder())
                                /*.addLast(new FixedLengthFrameDecoder(10))   解决粘包拆包问题
                                //第一种解决方案，使用定长数据包，每个数据包都要是指定长度*/
                                /*.addLast(new DelimiterBasedFrameDecoder(1024, Unpooled.wrappedBuffer("!".getBytes())))
                                //第二种，就是指定一个特定的分隔符，比如我们这里以感叹号为分隔符
                                //在收到分隔符之前的所有数据，都作为同一个数据包的内容*/
                                .addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4))
                                //第三种方案，就是在头部添加长度信息，来确定当前发送的数据包具体长度是多少
                                //offset是从哪里开始，length是长度信息占多少字节，这里是从0开始读4个字节表示数据包长度
                                .addLast(new ChannelInboundHandlerAdapter(){
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        ByteBuf buf = (ByteBuf) msg;
                                        String text = buf.toString(StandardCharsets.UTF_8);
                                        System.out.println("接收到客户端发送的数据："+text);
                                        ChannelPromise promise = new DefaultChannelPromise(channel);
                                        System.out.println(promise.isSuccess());
                                        ctx.writeAndFlush(Unpooled.wrappedBuffer("已收到！".getBytes()), promise);
                                        promise.sync();  //同步等待一下
                                        System.out.println(promise.isSuccess());
                                    }
                                })
                                /*.addLast(new ChannelInboundHandlerAdapter(){
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        ByteBuf buf = (ByteBuf) msg;
                                        System.out.println("接收到客户端发送的数据："+buf.toString(StandardCharsets.UTF_8));
                                        ctx.fireChannelRead(msg);
                                    }
                                })*/.addLast(handlerGroup, new ChannelInboundHandlerAdapter(){  //在添加时，可以直接指定使用哪个EventLoopGroup
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        try {
                                            Thread.sleep(10000);
                                        } catch (InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                        ctx.writeAndFlush(Unpooled.wrappedBuffer("已收到！".getBytes()));
                                    }
                                })
                                //.addLast(new StringEncoder());  //使用内置的StringEncoder可以直接将出站的字符串数据编码成ByteBuf
                        ;
                    }
                });
        ChannelFuture channelFuture = bootstrap.bind(8080);
        channelFuture.sync();   //让当前线程同步等待任务完成

        //直接添加监听器，当任务完成时自动执行，但是注意执行也是异步的，不是在当前线程
       /* channelFuture.addListener(f -> System.out.println("我是服务端启动完成之后要做的事情！"));*/
        System.out.println("服务端启动状态："+channelFuture.isDone());
        System.out.println("我是服务端启动完成之后要做的事情！");
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
