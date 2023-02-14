package com.gupaoedu.springaop.service;

import com.gupaoedu.springaop.demo.model.Member;
import com.gupaoedu.springaop.demo.service.IMemberService;
import com.gupaoedu.springaop.framework.GPApplicationContext;

public class MemberServiceTest {

    public static void main(String[] args) {

        GPApplicationContext applicationContext = new GPApplicationContext();
        IMemberService memberService = (IMemberService)applicationContext.getBean("memberService");

        try {

//            memberService.get("1");
            memberService.save(new Member());

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

}
