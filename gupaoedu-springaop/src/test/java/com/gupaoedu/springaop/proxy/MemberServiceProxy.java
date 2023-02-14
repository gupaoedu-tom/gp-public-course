package com.gupaoedu.springaop.proxy;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by Tom.
 */
public class MemberServiceProxy implements InvocationHandler {

    private Object target;

    public Object getProxy(Object obj){

        this.target = obj;
        return Proxy.newProxyInstance(obj.getClass().getClassLoader(),
                                    obj.getClass().getInterfaces(),
                                    this);

    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        System.out.println("在前面搞点事情");

        Object returnValue = method.invoke(this.target,args);

        System.out.println("在后面高点事情");

        return returnValue;

    }

}
