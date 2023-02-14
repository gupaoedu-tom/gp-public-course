package com.gupaoedu.springaop.proxy;

import com.gupaoedu.springaop.demo.model.Member;
import com.gupaoedu.springaop.demo.service.IMemberService;
import com.gupaoedu.springaop.demo.service.impl.MemberService;
import sun.misc.ProxyGenerator;

import java.io.FileOutputStream;

public class ProxyTest {

    public static void main(String[] args) {

        try {

            IMemberService memberService = (IMemberService)new MemberServiceProxy().getProxy(new MemberService());
            Member member = memberService.get("Tom");
            System.out.println(member);

            byte[] bytes = ProxyGenerator.generateProxyClass("$Proxy0", new Class[]{IMemberService.class});
            FileOutputStream os = new FileOutputStream("D://$Proxy0.class");
            os.write(bytes);
            os.close();

        }catch (Exception e) {

            e.printStackTrace();

        }

    }

}
