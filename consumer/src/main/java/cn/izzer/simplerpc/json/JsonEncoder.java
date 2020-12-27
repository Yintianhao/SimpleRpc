package cn.izzer.simplerpc.json;

import cn.izzer.common.util.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author yintianhao
 * @createTime 2020/12/13 22:38
 * @description
 */
public class JsonEncoder extends MessageToMessageEncoder {

    private static final Logger logger = LoggerFactory.getLogger(JsonEncoder.class);
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, List list) throws Exception {
        ByteBuf buf = ByteBufAllocator.DEFAULT.ioBuffer();
        byte[] bytes = JsonUtil.parseToJsonBytes(o);
        logger.info("JsonEncoder 编码:{}",new String(bytes));
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
        list.add(buf);
    }
}
