package field.handler;

import field.protocol.InformationProtocol;
import field.util.CommonUtil;

/**
 * 辨别消息的类型，根据类型创建Handler实例
 * Created by Hong on 2017/7/12.
 */
public class HandlerFactor {

    public static Handler createHandler(String handlerType){
        try {
            // ?代表泛型
            Class<?> handler = Class.forName("field.handler." + InformationProtocol.method
                    + CommonUtil.convert2SimpleName(handlerType) + "Handler");
            Handler instanceHandler = (Handler) handler.newInstance();
            return instanceHandler;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
