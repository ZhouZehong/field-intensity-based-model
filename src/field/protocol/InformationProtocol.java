package field.protocol;

import field.entity.FieldList;
import field.entity.Message.QueryCache;
import field.entity.Message.ResourceMessageCache;
import field.entity.ResourceList;
import field.entity.UserInterestList;
import field.handler.Handler;
import field.handler.HandlerFactor;
import field.util.JsonUtil;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.vector.SingleValueHolder;

import java.util.Map;

/**
 * 节点信息的容器
 * Created by Hong on 2017/7/12.
 */
public class InformationProtocol extends SingleValueHolder implements EDProtocol {

    /** 机会路由的方法 */
    private static final String PAR_METHOD = "method";

    public static String method;

    /** 信息场集合 */
    public FieldList fieldList;
    /** 资源集合 */
    public ResourceList resourceList;
    /** 用户兴趣集合 */
    public UserInterestList userInterestList;
    /** Query缓存 */
    public QueryCache queryCache;
    /** 资源消息缓存 */
    public ResourceMessageCache resourceMessageCache;
    /** 相遇节点缓存《相遇节点，相遇次数》 */
//    public Map<Node, Long> nodeCache;
    /** 邻居节点的相遇时长《邻居节点，相遇时长》，此变量针对真实轨迹数据*/
    public Map<Node, Long> meetingTime;

    public InformationProtocol(String prefix){
        super(prefix);
        method = Configuration.getString(prefix + "." + PAR_METHOD);
        if (method.equals("default"))
            method = "";
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        try {
            // 获取Json文件中的类名信息
            String className = JsonUtil.getClassName((String)event);
            // 根据类名创建相关类
            Class clazz = Class.forName(className);
            // 将Json转换成Java对象
            Object message = JsonUtil.toObject((String)event, clazz);

            // 利用Factory创建具体的Handler对象
            Handler handler = HandlerFactor.createHandler(className);
            if (handler != null){
                handler.handleMessage(node, pid, message);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
