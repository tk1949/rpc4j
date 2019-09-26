import io.netty.channel.nio.NioEventLoopGroup;
import network.RpcServer;

public class ServerTest {

    public static void main(String[] args) throws InterruptedException {
        RpcServer server = new RpcServer(new NioEventLoopGroup(), new NioEventLoopGroup(), 8080);
        server.start();
    }
}