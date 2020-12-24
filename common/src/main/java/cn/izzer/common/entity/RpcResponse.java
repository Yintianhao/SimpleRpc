package cn.izzer.common.entity;

import cn.izzer.common.enums.RespCodeEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * @author yintianhao
 * @createTime 2020/12/13 14:33
 * @description
 */
public class RpcResponse {

    /**
     * 请求的ID
     * */
    @Getter
    @Setter
    private Long reqId;

    /**
     * 返回码
     * */
    @Getter
    @Setter
    private RespCodeEnum code;

    /**
     * 发生错误时的错误信息
     * */
    @Getter
    @Setter
    private String errorMsg;

    /**
     * 正常返回下返回信息
     * */
    @Getter
    @Setter
    private Object data;

}
