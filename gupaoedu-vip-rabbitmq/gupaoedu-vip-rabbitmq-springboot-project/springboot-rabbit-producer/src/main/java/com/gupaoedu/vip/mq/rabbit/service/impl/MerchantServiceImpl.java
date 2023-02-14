package com.gupaoedu.vip.mq.rabbit.service.impl;

import com.gupaoedu.vip.mq.rabbit.entity.Merchant;
import com.gupaoedu.vip.mq.rabbit.mapper.MerchantMapper;
import com.gupaoedu.vip.mq.rabbit.service.MerchantService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class MerchantServiceImpl implements MerchantService {


    @Value("${com.gupaoedu.topicexchange}")
    private String topicExchange;

    @Value("${com.gupaoedu.topicroutingkey1}")
    private String topicRoutingKey;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    AmqpTemplate gupaoTemplate;


    @Override
    public List<Merchant> getMerchantList(String name, int page, int limit) {
        return  merchantMapper.getMerchantList(name,page,limit);
    }

    @Override
    public Merchant getMerchantById(Integer id) {
        return merchantMapper.getMerchantById(id);
    }

    @Override
    public int add(Merchant merchant) {
        int k = merchantMapper.add(merchant);
        System.out.println("aaa : "+merchant.getId());
        JSONObject title = new JSONObject();
        String jsonBody = JSONObject.toJSONString(merchant);
        title.put("type","add");
        title.put("desc","新增商户");
        title.put("content",jsonBody);
        gupaoTemplate.convertAndSend(topicExchange,topicRoutingKey, title.toJSONString());

        return k;
    }

    @Override
    public int updateState(Merchant merchant) {

        //操作本地数据库（本地事务）
        int k = merchantMapper.updateState(merchant);

        //往消息中间件推送消息
        JSONObject title = new JSONObject();
        String jsonBody = JSONObject.toJSONString(merchant);
        title.put("type","state");
        title.put("desc","更新商户状态");
        title.put("content",jsonBody);
        gupaoTemplate.convertAndSend(topicExchange,topicRoutingKey, title.toJSONString());

        return k;

    }

    @Override
    public int update(Merchant merchant) {

        //  关注Tom抖音 或者 B站账号，
        //   Tom弹架构

        //本地事务
        int k = merchantMapper.update(merchant);

        //消息同步
        JSONObject title = new JSONObject();
        String jsonBody = JSONObject.toJSONString(merchant);
        title.put("type","update");
        title.put("desc","更新商户信息");
        title.put("content",jsonBody);
        gupaoTemplate.convertAndSend(topicExchange,topicRoutingKey, title.toJSONString());


        return k;
    }

    @Override
    public int delete(Integer id) {

        int k = merchantMapper.delete(id);

        JSONObject title = new JSONObject();
        Merchant merchant = new Merchant();
        merchant.setId(id);
        String jsonBody = JSONObject.toJSONString(merchant);
        title.put("type","delete");
        title.put("desc","删除商户");
        title.put("content",jsonBody);

        gupaoTemplate.convertAndSend(topicExchange,topicRoutingKey, title.toJSONString());

        return k;
    }

    @Override
    public int getMerchantCount() {

        return merchantMapper.getMerchantCount();
    }
}
