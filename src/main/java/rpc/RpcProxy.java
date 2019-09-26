package rpc;

import message.RpcRequest;
import message.RpcResponse;
import network.RpcClient;
import tools.Snowflake;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RpcProxy {

    private static final HashMap<String, CountDownLatch> locks = new HashMap<>();
    private static final HashMap<String, RpcResponse> response = new HashMap<>();

    public static void putResponse(RpcResponse rpc) {
        response.put(rpc.getMessageId(), rpc);
        CountDownLatch latch = locks.remove(rpc.getMessageId());
        if (latch != null) {
            latch.countDown();
        }
    }

    public static  <T> T asynchronous(Class<?> clazz) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        RpcFace annotation = method.getDeclaringClass().getAnnotation(RpcFace.class);
                        if (annotation == null) {
                            throw new RpcException("Interface not implemented @RpcFace annotation");
                        }

                        String messageId = Snowflake.instance().nextIdS();
                        Class<?> className = annotation.value();
                        String methodName = method.getName();
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        RpcRequest request = new RpcRequest(messageId, false, className, methodName, parameterTypes, args);
                        RpcClient.client.submit(request);

                        return null;
                    }
                }
        );
    }

    public static  <T> T synchronize(Class<?> clazz) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        RpcFace annotation = method.getDeclaringClass().getAnnotation(RpcFace.class);
                        if (annotation == null) {
                            throw new RpcException("Interface not implemented @RpcFace annotation");
                        }

                        String messageId = Snowflake.instance().nextIdS();
                        Class<?> className = annotation.value();
                        String methodName = method.getName();
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        RpcRequest request = new RpcRequest(messageId, true, className, methodName, parameterTypes, args);

                        CountDownLatch latch = new CountDownLatch(1);
                        locks.put(messageId, latch);

                        RpcClient.client.submit(request);

                        latch.await(5000, TimeUnit.MILLISECONDS);

                        RpcResponse res = response.remove(messageId);

                        if (res != null) {
                            if (res.hasError()) {
                                throw res.getError();
                            }
                            return res.getResult();
                        }

                        throw new RpcException("RPC server response time out");
                    }
                }
        );
    }
}
