package com.gupaoedu.springaop.framework.proxy;

import java.lang.reflect.Method;

/**
 * Created by Tom.
 */
public interface GPInvocationHandler {
    Object invoke(Object proxy, Method method, Object[] args) throws Throwable;
}
