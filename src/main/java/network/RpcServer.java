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
import message.RpcMessage;
import message.RequestMessage;
import message.ResponseMessage;
import network.codec.RpcDecoder;
import network.codec.RpcEncoder;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

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
                                new ReadTimeoutHandler(20000, TimeUnit.MILLISECONDS),
                                new WriteTimeoutHandler(20000, TimeUnit.MILLISECONDS),
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

    private class SocketFrameHandler extends SimpleChannelInboundHandler<RpcMessage> {

        @Override
        public void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) {
            if (msg instanceof RequestMessage) {
                RequestMessage req = (RequestMessage) msg;
                try {
                    Class<?> className = req.getClassName();
                    MethodAccess ma = mas.get(className);
                    if (ma == null) {
                        ma = MethodAccess.get(className);
                        mas.put(className, ma);
                        Object o = className.getDeclaredConstructor().newInstance();
                        beans.put(className, o);
                    }

                    Object invoke = ma.invoke(beans.get(className), req.getMethodName(), req.getParameterTypes(), req.getParameters());

                    ResponseMessage response = new ResponseMessage(req.getMessageId(), req.isSync(), invoke);
                    ctx.channel().writeAndFlush(response);
                } catch (Exception e) {
                    ResponseMessage response = new ResponseMessage(req.getMessageId(), req.isSync(), e);
                    ctx.channel().writeAndFlush(response);
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.close();
        }
    }
}