package cn.izzer.simplerpc.controller;

import cn.izzer.common.entity.business.User;
import cn.izzer.common.util.JsonUtil;
import cn.izzer.simplerpc.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author yintianhao
 * @createTime 2020/12/13 23:29
 * @description
 */
@Controller
public class RpcController {

    private static final Logger logger = LoggerFactory.getLogger(RpcController.class);

    @Autowired
    private UserService userService;

    @RequestMapping("/getUserById")
    @ResponseBody
    public String getUserById(@RequestParam("id")Integer id){
        User user = userService.getUserById(id);
        String jsonStr = JsonUtil.parseToJsonStr(user);
        logger.info("getUserById {}",jsonStr);
        return jsonStr;
    }

}
