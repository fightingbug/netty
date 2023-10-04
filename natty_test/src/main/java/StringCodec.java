import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.nio.charset.StandardCharsets;
import java.util.List;
//编码解码器
//需要指定两个泛型，第一个是入站的消息类型，还有一个是出站的消息类型，出站是String类型，我们要转成ByteBuf
public class StringCodec extends MessageToMessageCodec<ByteBuf, String> {


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, String buf, List<Object> list) throws Exception {
        System.out.println("正在处理出站数据...");
        list.add(Unpooled.wrappedBuffer(buf.getBytes()));   //同样的，添加的数量就是出站的消息数量
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf buf, List<Object> list) throws Exception {
        System.out.println("正在处理入站数据...");
        list.add(buf.toString(StandardCharsets.UTF_8));  //和之前一样，直接一行解决
    }
}