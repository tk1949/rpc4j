package message;

/**
 * 心跳
 *
 * @author TK
 */
public class PingMessage implements RpcMessage {
    public static final PingMessage ping = new PingMessage();
}