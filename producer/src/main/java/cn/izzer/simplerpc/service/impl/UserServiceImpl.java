package cn.izzer.simplerpc.service.impl;


import cn.izzer.common.entity.business.User;
import cn.izzer.simplerpc.annonation.SimpleRpc;
import cn.izzer.simplerpc.service.UserService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yintianhao
 * @createTime 2020/12/13 12:49
 * @description
 */
@Slf4j
@SimpleRpc
public class UserServiceImpl implements UserService {

    private Map<Integer,User> userMap = new HashMap<>();

    @PostConstruct
    public void init(){
        User u1 = new User();
        u1.setUserId(1);
        u1.setUserName("yintianhao");
        userMap.put(1,u1);
    }

    @Override
    public User getUserById(Integer userId) {
        return userMap.get(userId);
    }

    @Override
    public void saveUser(User user) {
        userMap.put(user.getUserId(),user);
    }
}
