package network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import kryo.KryoUtil;
import message.RpcRequest;
import message.RpcResponse;

public class RpcEncoder extends MessageToByteEncoder {
    @Override
    protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) {
        if (in instanceof RpcResponse || in instanceof RpcRequest) {
            byte[] bytes = KryoUtil.writeToByteArray(in);
            out.writeBytes(bytes);
        }
    }
}