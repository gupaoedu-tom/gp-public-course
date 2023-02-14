package com.gupaoedu.springaop.framework;

import com.gupaoedu.springaop.demo.service.impl.MemberService;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GPApplicationContext {

    private Properties contextConfig = new Properties();

    private Map<String,Object> ioc = new HashMap<String,Object>();

    //用来保存配置文件中对应的Method和Advice的对应关系
    private Map<Method,Map<String, Method>> methodAdvices = new HashMap<Method, Map<String, Method>>();

    public GPApplicationContext(){

        ioc.put("memberService", new MemberService());

        doLoadConfig("application.properties");

        doInitAopConfig();

    }

    public Object getBean(String name){
        return createProxy(ioc.get(name));
    }

    private Object createProxy(Object instance){
        return new GPJdkDynamicAopProxy(instance).getProxy();
    }

    //加载配置文件
    private void doLoadConfig(String contextConfigLocation) {

        //直接从类路径下找到Spring主配置文件所在的路径
        //并且将其读取出来放到Properties对象中
        //相对于scanPackage=com.gupaoedu.demo 从文件中保存到了内存中
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);

        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null != is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void doInitAopConfig() {

        //初始化 目标方法和通知列表 的对应关系
        try {

            Class aspectClass = Class.forName(contextConfig.getProperty("aspectClass"));
            Map<String,Method> aspectMethods = new HashMap<String, Method>();

            for (Method method : aspectClass.getMethods()) {
                aspectMethods.put(method.getName(),method);
            }


//            public .* com.gupaoedu.springaop.demo.service..*Service..*(.*)
            //Map<Method,Map<String, Method>> methodAdvices
            //PonintCut  表达式解析为正则表达式
            String pointCut = contextConfig.getProperty("pointCut")
                    .replaceAll("\\.","\\\\.")
                    .replaceAll("\\\\.\\*",".*")
                    .replaceAll("\\(","\\\\(")
                    .replaceAll("\\)","\\\\)");
            Pattern pointCutPattern = Pattern.compile(pointCut);

            for (Map.Entry<String, Object> entry : ioc.entrySet()) {
                Class<?> clazz = entry.getValue().getClass();

                for (Method method : clazz.getMethods()) {

                    //public com.gupaoedu.springaop.demo.model.Member com.gupaoedu.springaop.demo.service.impl.MemberService.get()
                    //public com.gupaoedu.springaop.demo.model.Member com.gupaoedu.springaop.demo.service.impl.MemberService.get(java.lang.String)
                    //public java.lang.Boolean com.gupaoedu.springaop.demo.service.impl.MemberService.delete(java.lang.String) throws java.lang.Exception
                    //public void com.gupaoedu.springaop.demo.service.impl.MemberService.save(com.gupaoedu.springaop.demo.model.Member) throws java.lang.Exception


                    //public .* com.gupaoedu.springaop.demo.service..*Service..*(.*)
//                    System.out.println(method.toString());

                    String methodString = method.toString();

//                    System.out.println(methodString);

                    if(methodString.contains("throws")){
                        methodString = methodString.substring(0,methodString.lastIndexOf("throws")).trim();
                    }


                    //public com.gupaoedu.springaop.demo.model.Member com.gupaoedu.springaop.demo.service.impl.MemberService.get()
                    //public com.gupaoedu.springaop.demo.model.Member com.gupaoedu.springaop.demo.service.impl.MemberService.get(java.lang.String)
                    //public java.lang.Boolean com.gupaoedu.springaop.demo.service.impl.MemberService.delete(java.lang.String)
                    //public void com.gupaoedu.springaop.demo.service.impl.MemberService.save(com.gupaoedu.springaop.demo.model.Member)
//                    System.out.println(methodString);

                    //Java正则API中的一个对象，匹配器
                    Matcher matcher = pointCutPattern.matcher(methodString);

                    if(matcher.matches()){

                        Map<String,Method> advices = new HashMap<String, Method>();

                        if(!(null == contextConfig.getProperty("aspectBefore") ||
                                "".equals(contextConfig.getProperty("aspectBefore")))){
                            advices.put("before",aspectMethods.get(contextConfig.getProperty("aspectBefore")));
                        }

                        if(!(null == contextConfig.getProperty("aspectAfter") ||
                                "".equals(contextConfig.getProperty("aspectAfter")))){
                            advices.put("after",aspectMethods.get(contextConfig.getProperty("aspectAfter")));
                        }

                        if(!(null == contextConfig.getProperty("aspectAfterThrow") ||
                                "".equals(contextConfig.getProperty("aspectAfterThrow")))){
                            advices.put("afterThrow",aspectMethods.get(contextConfig.getProperty("aspectAfterThrow")));
                        }

                        //目标类方法和切面通知方法，绑定一对多的对应关系
                        methodAdvices.put(method,advices);
                    }



                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }


    }

    class GPJdkDynamicAopProxy implements InvocationHandler {

        private Object instance;
        public GPJdkDynamicAopProxy(Object instance) {
            this.instance = instance;
        }

        public Object getProxy() {

            return Proxy.newProxyInstance(instance.getClass().getClassLoader(),instance.getClass().getInterfaces(),this);

        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            //com.gupaoedu.springaop.demo.aspect.LogAspect
            Object aspectObject = Class.forName(contextConfig.getProperty("aspectClass")).newInstance();
            //根据目标类的一个方法，对应多个回调通知方法
            // Map<Method,List<Method>> methodAdvices;
            // Map<Method,Map<String,Method>> methodAdvices;
            Map<String,Method> advices = methodAdvices.get(instance.getClass().getMethod(method.getName(),method.getParameterTypes()));
            Object returnValue = null;
            if(null != advices.get("before")) {
                advices.get("before").invoke(aspectObject);
            }
            try {
                //放着调用目标类的方法
                returnValue = method.invoke(instance, args);

            }catch (Exception e){
                if(null != advices.get("afterThrow")) {
                    advices.get("afterThrow").invoke(aspectObject);
                }
                e.printStackTrace();
                throw e;
            }
            if(null != advices.get("after")) {
                advices.get("after").invoke(aspectObject);
            }
            return returnValue;

        }

    }

}
