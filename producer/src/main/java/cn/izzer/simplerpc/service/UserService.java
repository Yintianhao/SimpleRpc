package cn.izzer.simplerpc.service;


import cn.izzer.common.entity.business.User;

/**
 * @author yintianhao
 * @createTime 2020/12/13 12:25
 * @description
 */
public interface UserService {

    User getUserById(Integer userId);

    void saveUser(User user);
}
