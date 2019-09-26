package test;

import kryo.KryoUtil;
import message.RpcRequest;

public class KryoTest {

    public static void main(String[] args) {
        RpcRequest request = new RpcRequest();
        byte[] bytes = KryoUtil.writeToByteArray(request);

        RpcRequest o = KryoUtil.readFromByteArray(bytes);
    }
}