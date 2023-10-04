import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.IOException;
import java.io.InputStream;

public class PageResolver {
    //直接单例模式
    private static final PageResolver INSTANCE = new PageResolver();
    private PageResolver(){}
    public static PageResolver getInstance(){
        return INSTANCE;
    }

    //请求路径给进来，接着我们需要将页面拿到，然后转换成响应数据包发回去
    public FullHttpResponse resolveResource(String path){
        if(path.startsWith("/"))  {  //判断一下是不是正常的路径请求
            path = path.equals("/") ? "index.html" : path.substring(1);    //如果是直接请求根路径，那就默认返回index页面，否则就该返回什么路径的文件就返回什么
            try(InputStream stream = this.getClass().getClassLoader().getResourceAsStream(path)) {
                if(stream != null) {   //拿到文件输入流之后，才可以返回页面
                    byte[] bytes = new byte[stream.available()];
                    stream.read(bytes);
                    return this.packet(HttpResponseStatus.OK, bytes);  //数据先读出来，然后交给下面的方法打包
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        //其他情况一律返回404
        return this.packet(HttpResponseStatus.NOT_FOUND, "404 Not Found!".getBytes());
    }

    //包装成FullHttpResponse，把状态码和数据写进去
    private FullHttpResponse packet(HttpResponseStatus status, byte[] data){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        response.content().writeBytes(data);
        return response;
    }
}