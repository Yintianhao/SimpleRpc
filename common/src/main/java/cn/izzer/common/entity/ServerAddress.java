package cn.izzer.common.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * @author yintianhao
 * @createTime 2020/12/16 22:11
 * @description 服务实例的信息
 */
public class ServerAddress {

    @Getter
    @Setter
    private String ip;

    @Getter
    @Setter
    private Integer port;

}
