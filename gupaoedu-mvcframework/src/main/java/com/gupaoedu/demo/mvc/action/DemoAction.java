package com.gupaoedu.demo.mvc.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gupaoedu.demo.service.IDemoService;
import com.gupaoedu.mvcframework.annotation.GPAutowired;
import com.gupaoedu.mvcframework.annotation.GPController;
import com.gupaoedu.mvcframework.annotation.GPRequestMapping;
import com.gupaoedu.mvcframework.annotation.GPRequestParam;

@GPController
@GPRequestMapping("/demo")
public class DemoAction {

	//private在反射面前只能哄小孩，遵循OOP
	//反射一般是框架技术，从某种程度上破坏了OOP的一些特性
  	@GPAutowired private IDemoService demoService;

	private IDemoService domoService2;

	@GPRequestMapping("/query")
	public void query(HttpServletRequest req, HttpServletResponse resp,
					  @GPRequestParam("name") String name){
		String result = demoService.get(name);
//		String result = "My name is " + name + " =====";
		try {
			resp.getWriter().write(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@GPRequestMapping("/add")
	public void add(HttpServletRequest req, HttpServletResponse resp,
					@GPRequestParam("a") Integer a, @GPRequestParam("b") Integer b){
		try {
			resp.getWriter().write(a + "+" + b + "=" + (a + b));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@GPRequestMapping("/remove")
	public void remove(HttpServletRequest req,HttpServletResponse resp,
					   @GPRequestParam("id") Integer id){
	}

}
