package com.gupaoedu.mvcframework.v2.servlet;

import com.gupaoedu.mvcframework.annotation.GPAutowired;
import com.gupaoedu.mvcframework.annotation.GPController;
import com.gupaoedu.mvcframework.annotation.GPRequestMapping;
import com.gupaoedu.mvcframework.annotation.GPService;
import sun.security.x509.EDIPartyName;

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

public class GPDispatherServlet extends HttpServlet {

    //初始化IoC容器
    private Map<String,Object> ioc = new HashMap<String,Object>();


    //读取配置文件
    private Properties contextConfig = new Properties();


    //缓存所有被扫描到的fullClasName(全类名)
    private List<String> classNames = new ArrayList<String>();

    //缓存所有的url和method的对应关系
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
            resp.getWriter().write("500 Exception Detail ：" + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");


        if (!this.handlerMapping.containsKey(url)){
             resp.getWriter().write("404");
             return;
        }

        Method method = this.handlerMapping.get(url);

        //http://locahost?name=Tom&name=Tomcat
        //name = [Tom,Tomcat]
        Map<String,String[]> params = req.getParameterMap();

        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        method.invoke(ioc.get(beanName),new Object[]{req,resp,Arrays.toString(params.get("name"))});
    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        //==========  IoC  =========

        //1、加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //2、扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));

        //3、初始化所有的相关类的实例，并且缓存到IoC容器中
        doInstance();

        //==========  DI  =========

        //4、完成依赖注入
        doAutowired();

        //==========  MVC  =========

        //5、初始化HandlerMapping
        doInitHandlerMapping();


        System.out.println("GP Spring framework is init.");

    }

    private void doInitHandlerMapping() {

        if(ioc.isEmpty()){ return;}

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {

            Class<?> clazz = entry.getValue().getClass();

            String baseUrl = "";
            if(clazz.isAnnotationPresent(GPRequestMapping.class)){
                GPRequestMapping requestMapping = clazz.getAnnotation(GPRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            for (Method method : clazz.getMethods()) {

                if(!method.isAnnotationPresent(GPRequestMapping.class)){continue;}

                GPRequestMapping requestMapping = method.getAnnotation(GPRequestMapping.class);

                //demoquery
                //      /demo//query
                String url = ("/" + baseUrl + "/" + requestMapping.value())
                            .replaceAll("/+","/");

                handlerMapping.put(url,method);

                System.out.println("Mapped : " + url + " --> " + method);

            }


        }
    }

    private void doAutowired() {
        if(ioc.isEmpty()){return;}

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {

            for (Field field : entry.getValue().getClass().getDeclaredFields()) {

                if(!field.isAnnotationPresent(GPAutowired.class)) { continue; }

                GPAutowired autowired = field.getAnnotation(GPAutowired.class);
                String beanName = autowired.value().trim();
                if("".equals(beanName)){
                    beanName = field.getType().getName();
                }

                //强制授权，暴力访问
                field.setAccessible(true);

                try {

                    // DemoAction     IDemoService demoService;
                    //    entry.getValue() 相当于 DemoAction的实例
                    //beanName相当于com.gupaoedu.demo.service.IDemoService这个字符串
                    //field.set 相当于人工写了
                    // demoAction.demoService = ioc.get("com.gupaoedu.demo.service.IDemoService")
                    field.set(entry.getValue(),ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }

    }

    private void doInstance() {

        if(classNames.isEmpty()){return;}

        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);

                if(clazz.isAnnotationPresent(GPController.class)){      
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    Object instance = clazz.newInstance();
                    ioc.put(beanName,instance);
                }else if(clazz.isAnnotationPresent(GPService.class)){
                    //1、默认类名首字母小写
                    String beanName = toLowerFirstCase(clazz.getSimpleName());

                    //2、自定义命名
                    GPService service = clazz.getAnnotation(GPService.class);
                    if(!"".equals(service.value())){
                        beanName = service.value();
                    }
                    Object instance = clazz.newInstance();                    
                    ioc.put(beanName,instance);

                    //3、要根据类型注入实现类，投机取巧
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

            if (file.isDirectory()){
                doScanner(scanPackage + "." + file.getName());
            }else {

                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                //fullClassName
                //Class.forName("包名.类名");
                String className = (scanPackage + "." + file.getName()).replace(".class","");
                classNames.add(className);

            }
        }

    }

    private void doLoadConfig(String contextConfigLocation) {

        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);

        try {
            //读取配置文件
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
}