package field.handler;

import peersim.core.Node;

/**
 * 处理消息的抽象类
 * Created by Hong on 2017/7/12.
 */
public abstract class Handler {
    public abstract void handleMessage(Node node,
                                       int protocolID, Object message);
}
