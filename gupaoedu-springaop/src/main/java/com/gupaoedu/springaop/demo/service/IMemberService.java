package com.gupaoedu.springaop.demo.service;


import com.gupaoedu.springaop.demo.model.Member;

/**
 * 注解版业务操作类
 * @author Tom
 */

public interface IMemberService {
	
	public Member get(String id);

	public Member get();
	
	public void save(Member member) throws Exception;
	
	public Boolean delete(String id) throws Exception;
	
}
