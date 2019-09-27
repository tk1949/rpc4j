package rpc;

import io.netty.channel.nio.NioEventLoopGroup;
import network.RpcServer;
import site.RpcInstal;

public class RpcBoot {

    public static void run(Class<?> clazz) {
        try {
            RpcServer server =
                    new RpcServer(new NioEventLoopGroup(), new NioEventLoopGroup(), RpcInstal.instal.getRpcServerPort());
            server.start();

            //todo 扫描类的@RpcFace 注解

        } catch (InterruptedException e) {
            throw new RpcException(e);
        }
    }
}