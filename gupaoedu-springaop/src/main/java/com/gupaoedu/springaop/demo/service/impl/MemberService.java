package com.gupaoedu.springaop.demo.service.impl;

import com.gupaoedu.springaop.demo.aspect.LogAspect;
import com.gupaoedu.springaop.demo.model.Member;
import com.gupaoedu.springaop.demo.service.IMemberService;
import lombok.extern.slf4j.Slf4j;

/**
 * 注解版业务操作类
 * @author Tom
 */
@Slf4j
public class MemberService implements IMemberService {

	private LogAspect logAspect = new LogAspect();

	//public com.gupaoedu.springaop.demo.model.Member com.gupaoedu.springaop.demo.service.impl.MemberService.get(java.lang.String id);
	public Member get(String id){
		log.info("getMemberById method . . .");
		return new Member();
	}

	public Member get(){
		log.info("getMember method . . .");
		return new Member();
	}
	
	public void save(Member member) throws Exception{
		log.info("save member method . . .");
		throw new Exception("Tom故意抛的异常");
	}
	
	public Boolean delete(String id) throws Exception{
		log.info("delete method . . .");
		throw new Exception("spring aop ThrowAdvice演示");
	}
	
}
