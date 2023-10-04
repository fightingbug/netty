import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import io.netty.handler.codec.http.*;
import io.netty.handler.ipfilter.IpFilterRule;
import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.handler.ipfilter.RuleBasedIpFilter;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;


import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class HttpServer {



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
                                    .addLast(new HttpRequestDecoder())   //Http请求解码器
                                    .addLast(new HttpObjectAggregator(Integer.MAX_VALUE))  //搞一个聚合器，将内容聚合为一个FullHttpRequest，参数是最大内容长度
                                    .addLast(new LoggingHandler(LogLevel.INFO))   //添加一个日志Handler，在请求到来时会自动打印相关日志
                                    .addLast(new RuleBasedIpFilter(new IpFilterRule() { //ip过滤handler
                                        @Override
                                        public boolean matches(InetSocketAddress inetSocketAddress) {
                                            return inetSocketAddress.getHostName().equals("127.0.0.1");
                                            //进行匹配，返回false表示匹配失败
                                            //如果匹配失败，那么会根据下面的类型决定该干什么，比如我们这里判断是不是本地访问的，如果是那就拒绝
                                        }

                                        @Override
                                        public IpFilterRuleType ruleType() {
                                            return IpFilterRuleType.REJECT;   //类型，REJECT表示拒绝连接，ACCEPT表示允许连接
                                        }
                                    }))
                                    .addLast(new ChannelInboundHandlerAdapter(){
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                            FullHttpRequest request = (FullHttpRequest) msg;
                                            //请求进来了直接走解析
                                            PageResolver resolver = PageResolver.getInstance();
                                            ctx.channel().writeAndFlush(resolver.resolveResource(request.uri()));
                                            ctx.channel().close();
                                        }
                                    })
                                    .addLast(new HttpResponseEncoder());

                            /*channel.pipeline()
                                    .addLast(new HttpRequestDecoder())   //Http请求解码器
                                    .addLast(new HttpObjectAggregator(Integer.MAX_VALUE))  //搞一个聚合器，将内容聚合为一个FullHttpRequest，参数是最大内容长度
                                    .addLast(new ChannelInboundHandlerAdapter(){
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                            FullHttpRequest request = (FullHttpRequest) msg;
                                            System.out.println("浏览器请求路径："+request.uri());  //直接获取请求相关信息
                                            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                                            response.content().writeCharSequence("Hello World!", StandardCharsets.UTF_8);
                                            ctx.channel().writeAndFlush(response);
                                            ctx.channel().close();
                                        }
                                    })
                                    .addLast(new HttpResponseEncoder());*/
                            /*channel.pipeline()
                                    .addLast(new HttpRequestDecoder())   //Http请求解码器
                                    .addLast(new ChannelInboundHandlerAdapter(){
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                            System.out.println("收到客户端的数据："+msg.getClass());  //看看是个啥类型
                                            //收到浏览器请求后，我们需要给一个响应回去
                                            FullHttpResponse response =
                                                    new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);  //HTTP版本为1.1，状态码就OK（200）即可
                                            //直接向响应内容中写入数据
                                            response.content().writeCharSequence("Hello World!", StandardCharsets.UTF_8);
                                            ctx.channel().writeAndFlush(response);   //发送响应
                                            ctx.channel().close();   //HTTP请求是一次性的，所以记得关闭
                                        }
                                    })
                                    .addLast(new HttpResponseEncoder());   //响应记得也要编码后发送哦*/
                        }
                    });
            ChannelFuture channelFuture = bootstrap.bind(8080);
            channelFuture.sync();   //让当前线程同步等待任务完成
            System.out.println("服务端启动状态："+channelFuture.isDone());
            System.out.println("我是服务端启动完成之后要做的事情！");
        }


}


