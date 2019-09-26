import example.Hello;
import rpc.RpcProxy;

public class Main {

    public static void main(String[] args) {
        Hello hello = RpcProxy.synchronize(Hello.class);
        System.out.println("sync" + hello.say("RPC"));
        System.out.println("sync" + hello.say("rpc"));

        Hello hello2 = RpcProxy.asynchronous(Hello.class);

        System.out.println("asyn" + hello2.say("RPC"));
        System.out.println("asyn" + hello2.say("rpc"));
    }
}