package field.initialize;

import field.entity.ResourceListValue;
import field.protocol.InformationProtocol;
import field.util.MyCommonState;
import field.entity.Message.ResourceMessage;
import field.entity.Resource;
import field.util.JsonUtil;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.transport.Transport;

import java.util.Map;

/**
 * 泛洪机制下的资源消息发生器
 * Created by Hong on 2017/8/9.
 */
public class FloodResourceMessageProducer implements Control {

    /** 信息协议 */
    private static final String PAR_IP_PROT = "information_protocol";
    /** 产生资源消息的时间跨度 */
    private static final String PAR_START_TIME_SPAN = "start_time_span";

    private final int ipPid;
    private final long startTimeSpan;

    public FloodResourceMessageProducer(String prefix){
        ipPid = Configuration.getPid(prefix + "." + PAR_IP_PROT);
        startTimeSpan = Configuration.getLong(prefix + "." + PAR_START_TIME_SPAN);
    }

    @Override
    public boolean execute() {
        // 遍历网络中的所有节点，根据节点上的资源情况产生相应的资源消息
        for (int i = 0; i < Network.size(); i++) {
            Node currentNode = Network.get(i);
            // 获取当前节点的信息协议
            InformationProtocol currentIp =
                    (InformationProtocol) currentNode.getProtocol(ipPid);
            // 把当前节点上的同类资源整合起来，产生对应的资源消息并发送
            Map<Long, ResourceListValue> resourceListValueMap = currentIp.resourceList.calTotalValueForType();

            for (Map.Entry<Long, ResourceListValue> entry: resourceListValueMap.entrySet()) {
                ResourceMessage resourceMessage = new ResourceMessage();
                resourceMessage.setResourceMessageID(MyCommonState.resourceMessageCounter);
                MyCommonState.resourceMessageCounter++;
                resourceMessage.setResourceSize(entry.getValue().getResourceListSize());
                resourceMessage.setResourceValue(entry.getValue().getResourceListValue());
                long resourceMessageStartTime = CommonState.r.nextLong(startTimeSpan);
                resourceMessage.setResourceMessageStartTime(resourceMessageStartTime);
                resourceMessage.setMessageSource(i);
                resourceMessage.setTypeID(entry.getKey());
                resourceMessage.setLifeTime(MyCommonState.maxMessageLifeTime);
                resourceMessage.setRelayCapacity(MyCommonState.maxMessageRelayCapacity);
                resourceMessage.setHop(0);

                // 得先将设置好的资源消息存放至当前节点的缓存中，因为很多资源消息还没到开始运作的时间
                ResourceMessage resourceMessageForCache = resourceMessage.clone();
                currentIp.resourceMessageCache.add(resourceMessageForCache);

                // 判断资源消息开始运作的时间是否小于当前时间，如果是，则调整开始时间，并开始传递资源消息
                if (resourceMessage.getResourceMessageStartTime() <= CommonState.getTime()){
                    resourceMessage.setResourceMessageStartTime(CommonState.getTime());
                    String json = JsonUtil.toJson(resourceMessage);
                    ((Transport) currentNode.getProtocol(FastConfig.getTransport(ipPid))).
                            send(currentNode, currentNode, json, ipPid);
                }
            }
        }
        return false;
    }
}
