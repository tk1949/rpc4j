package message;

public class PingMessage implements RpcMessage {
    public static final PingMessage ping = new PingMessage();
}