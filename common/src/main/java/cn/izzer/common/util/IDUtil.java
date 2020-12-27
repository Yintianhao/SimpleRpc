package cn.izzer.common.util;

/**
 * @author yintianhao
 * @createTime 2020/12/20 1:40
 * @description ID生成器
 */
public class IDUtil {

    private final static SnowflakeIdWorker idUtil = new SnowflakeIdWorker(0,0);

    /**
     * rpc请求id
     * */
    public static long getRpcRequestId(){
        return idUtil.nextId();
    }
}
