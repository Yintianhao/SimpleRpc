package cn.izzer.simplerpc.json;

import cn.izzer.common.util.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * @author yintianhao
 * @createTime 2020/12/13 22:38
 * @description
 */
public class JsonEncoder extends MessageToMessageEncoder {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, List list) throws Exception {
        ByteBuf buf = ByteBufAllocator.DEFAULT.ioBuffer();
        byte[] bytes = JsonUtil.parseToJsonBytes(o);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
        list.add(buf);
    }
}
