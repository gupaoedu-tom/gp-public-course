package com.gupaoedu.mvcframework.v2.servlet;

import com.gupaoedu.mvcframework.annotation.GPAutowired;
import com.gupaoedu.mvcframework.annotation.GPController;
import com.gupaoedu.mvcframework.annotation.GPRequestMapping;
import com.gupaoedu.mvcframework.annotation.GPService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class GPDispatherServletBak extends HttpServlet {

    //初始化IoC容器
    private Map<String,Object> ioc = new HashMap<String,Object>();

    private Properties contextConfig = new Properties();

    private List<String> classNames = new ArrayList<String>();

    private Map<String,Method> handlerMapping = new HashMap<String, Method>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();

            resp.getWriter().write("500 Excetion Detail: " + Arrays.toString(e.getStackTrace()));
        }

    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");

        if(!this.handlerMapping.containsKey(url)){
            resp.getWriter().write("404 Not Found!!");
            return;
        }

        Method method = this.handlerMapping.get(url);
        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());

        //http://localhost:8080/demo/query?name=Tom&name=Tomcat
        Map<String,String[]> params = req.getParameterMap();

        method.invoke(ioc.get(beanName),new Object[]{req,resp,Arrays.toString(params.get("name"))});

    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        //1、加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //2、扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));

        //3、初始化相关的类，并且缓存到IoC容器中
        doInstance();

        //4、完成依赖注入
        doAutowired();

        //5、初始化HandlerMapping，将URL和Method建立一对一的关系
        doInitHandlerMapping();

        System.out.println("GP Spring framework is init.");

    }

    private void doInitHandlerMapping() {
        
        if(ioc.isEmpty()){ return; }

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {

            Class<?> clazz = entry.getValue().getClass();

            if(!clazz.isAnnotationPresent(GPController.class)){ continue; }

            String baseUrl = "";
            if(clazz.isAnnotationPresent(GPRequestMapping.class)){
                GPRequestMapping requestMapping = clazz.getAnnotation(GPRequestMapping.class);
                baseUrl = requestMapping.value();
            }


            for (Method method : clazz.getMethods()) {

                if(!method.isAnnotationPresent(GPRequestMapping.class)){ continue; }

                GPRequestMapping requestMapping = method.getAnnotation(GPRequestMapping.class);


                //  demo query
                //  demoquery
                //   //demo//query
                String url = ("/" + baseUrl + "/" + requestMapping.value())
                        .replaceAll("/+","/");

                handlerMapping.put(url,method);

                System.out.println("Mapped : " + url + " --> " + method);

            }
            
        }
        
    }

    private void doAutowired() {

        if(ioc.isEmpty()){ return; }

        for (Map.Entry<String,Object> entry : ioc.entrySet()) {

            for (Field field : entry.getValue().getClass().getDeclaredFields()) {

                if(!field.isAnnotationPresent(GPAutowired.class)){ continue;}

                GPAutowired autowired = field.getAnnotation(GPAutowired.class);
                String beanName = autowired.value().trim();
                if("".equals(beanName)){
                    beanName = field.getType().getName();
                }
                
                field.setAccessible(true);  //强制暴力访问

                try {
                    //执行赋值的方法

                    //filed 相当于 IDemoService demoService;

                    //entry.getValue() 相当于 DemoAction 的实例

                    //beanName  相当于 com.gupaoedu.demo.service.IDemoService

                    //field.set 相当于  demoAction.demoService = ioc.get(beanName);

                    field.set(entry.getValue(),ioc.get(beanName));

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }

            }
            
        }

    }

    private void doInstance() {
        
        if(classNames.isEmpty()){ return; }

        try {
            for (String className : classNames) {

                Class<?> clazz = Class.forName(className);


                if(clazz.isAnnotationPresent(GPController.class)){
                    //默认是类名的首字母小写
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    Object instance = clazz.newInstance();
                    ioc.put(beanName,instance);
                }else if(clazz.isAnnotationPresent(GPService.class)){

                    //1、默认是类名的首字母小写
                    String beanName = toLowerFirstCase(clazz.getSimpleName());


                    //2、在不同包下存在相同类名，自定义类名
                    GPService service = clazz.getAnnotation(GPService.class);
                    if(!"".equals(service.value())){
                        beanName = service.value();
                    }

                    Object instance = clazz.newInstance();
                    ioc.put(beanName,instance);

                    //3、根据接口注入的话，拿实现类来赋值
                    for (Class<?> i : clazz.getInterfaces()) {
                        if(ioc.containsKey(i.getName())){
                            throw new Exception("The beanName is exists!!");
                        }
                        ioc.put(i.getName(),instance);
                    }
                }else {
                    continue;
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        
    }

    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doScanner(String scanPackage) {

        URL url = this.getClass().getClassLoader()
                .getResource("/" + scanPackage.replaceAll("\\.","/"));

        File classPath = new File(url.getFile());


        for (File file : classPath.listFiles()) {

            if(file.isDirectory()) {

                doScanner(scanPackage + "." + file.getName());

            }else {

                if (!file.getName().endsWith(".class")) {  continue; }
                //Class.forName();
                String className = (scanPackage + "." + file.getName().replace(".class", ""));
                classNames.add(className);
            }

        }

    }

    private void doLoadConfig(String contextConfigLocation) {

        InputStream is = null;

        try {
            is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
            //读取到properties
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null != is){
                try {
                    is.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }
}