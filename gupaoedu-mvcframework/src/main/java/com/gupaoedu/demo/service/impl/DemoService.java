package com.gupaoedu.demo.service.impl;

import com.gupaoedu.demo.service.IDemoService;
import com.gupaoedu.mvcframework.annotation.GPService;

/**
 * 核心业务逻辑
 */
@GPService
public class DemoService implements IDemoService{

	public String get(String name) {
		System.out.println("Invoker DemoService get method!!");
		return "My name is " + name + ",from service ....";
	}

}
