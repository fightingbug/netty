import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;

import java.util.concurrent.ExecutionException;

public class Promise_test {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Promise<String> promise = new DefaultPromise<>(new DefaultEventLoop());
        promise.addListener(f -> System.out.println(promise.get()));   //注意是在上面的DefaultEventLoop执行的
        System.out.println(promise.isSuccess());    //在一开始肯定不是成功的
        promise.setSuccess("lbwnb");    //设定成功
        System.out.println(promise.isSuccess());   //再次获取，可以发现确实成功了
        System.out.println(promise.get());    //获取结果，就是我们刚刚给进去的



    }
}
