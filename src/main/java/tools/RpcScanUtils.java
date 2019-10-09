package tools;

import com.google.common.collect.Sets;
import org.reflections.Reflections;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 扫描工具类，使用org.reflections.reflections0.9.11框架
 *
 * @author pangqr
 * @date 2019-9-30
 */
public class RpcScanUtils {

    private RpcScanUtils() {
    }

    private static Set<Class<?>> beanContainer = Sets.newHashSet();

    /**
     * 扫描全部注解（包括接口和实现类）
     *
     * @param packageName 扫描包路径
     * @param T           注解class
     * @return
     */
    private static Set<Class<?>> getAll(String packageName, Class T) {
        beanContainer.clear();
        beanContainer = new Reflections(packageName).getTypesAnnotatedWith(T);
        return beanContainer;
    }

    /**
     * 扫描带有注解的接口
     *
     * @param packageName 扫描包路径
     * @param T           注解class
     * @return
     */
    public static Set<Class<?>> getInterfaces(String packageName, Class T) {
        return getAll(packageName, T).stream()
                .filter(clazz -> clazz.isInterface())
                .collect(Collectors.toSet());
    }

    /**
     * 扫描带有指定注解的bean
     *
     * @param packageName 扫描包路径
     * @param T           注解class
     * @return
     */
    public static Set<Class<?>> getBeans(String packageName, Class T) {
        return getAll(packageName, T).stream()
                .filter(clazz -> !clazz.isInterface())
                .collect(Collectors.toSet());
    }

}
