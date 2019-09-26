package rpc;

public class RpcException extends Exception {

    private int code;
    private String message;

    public RpcException() {}

    public RpcException(String message) {
        this.message = message;
    }

    public RpcException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}