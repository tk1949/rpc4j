package site;

public class RpcInstal {

    public static final RpcInstal instal = new RpcInstal();

    private int rpcServerPort = 8080;

    private RpcInstal() {}

    public static void builder() {

    }

    public int getRpcServerPort() {
        return rpcServerPort;
    }
}