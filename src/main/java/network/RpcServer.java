package network;

import com.esotericsoftware.reflectasm.MethodAccess;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import message.RpcRequest;
import message.RpcResponse;
import network.codec.RpcDecoder;
import network.codec.RpcEncoder;

import java.util.HashMap;

public class RpcServer {

    private static final HashMap<Class<?>, MethodAccess> mas = new HashMap<>();
    private static final HashMap<Class<?>, Object> beans = new HashMap<>();

    private EventLoopGroup boos;
    private EventLoopGroup worker;
    private int port;

    public RpcServer(EventLoopGroup boss, EventLoopGroup worker, int port) {
        this.boos = boss;
        this.worker = worker;
        this.port = port;
    }

    public void start() throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();
        b.group(boos, worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024 * 2)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(
                                new ReadTimeoutHandler(60),
                                new WriteTimeoutHandler(60),
                                new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 8, 0, 8),
                                new LengthFieldPrepender(8),
                                new RpcDecoder(),
                                new RpcEncoder(),
                                new SocketFrameHandler());
                    }
                }).bind(port).sync().channel();
    }

    public void stop() {
        boos.shutdownGracefully();
        worker.shutdownGracefully();
    }

    private class SocketFrameHandler extends SimpleChannelInboundHandler<RpcRequest> {

        @Override
        public void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) {
            try {
                Class<?> className = msg.getClassName();
                MethodAccess ma = mas.get(className);
                if (ma == null) {
                    ma = MethodAccess.get(className);
                    mas.put(className, ma);
                    Object o = className.getDeclaredConstructor().newInstance();
                    beans.put(className, o);
                }

                Object invoke = ma.invoke(beans.get(className), msg.getMethodName(), msg.getParameterTypes(), msg.getParameters());

                RpcResponse response = new RpcResponse(msg.getMessageId(), msg.isSync(), invoke);
                System.out.println(response.isSync());
                ctx.channel().writeAndFlush(response);
            } catch (Exception e) {
                RpcResponse response = new RpcResponse(msg.getMessageId(), msg.isSync(), e);
                ctx.channel().writeAndFlush(response);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.close();
        }
    }
}