package cn.izzer.simplerpc.json;

import cn.izzer.common.entity.RpcResponse;
import cn.izzer.common.util.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yintianhao
 * @createTime 2020/12/13 22:45
 * @description
 */
public class JsonDecoder extends LengthFieldBasedFrameDecoder {

    private static final Logger logger = LoggerFactory.getLogger(JsonDecoder.class);

    public JsonDecoder(){
        super(65535,0,4,0,4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf decode = (ByteBuf) super.decode(ctx, in);
        if (decode==null){
            return null;
        }
        int data_len = decode.readableBytes();
        byte[] bytes = new byte[data_len];
        decode.readBytes(bytes);
        logger.info("JsonDecoder 解码:{}",new String(bytes));
        Object ret = JsonUtil.parseToObject(bytes,RpcResponse.class);
        return ret;
    }
}
