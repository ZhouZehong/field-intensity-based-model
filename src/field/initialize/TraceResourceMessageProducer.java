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
 * 真实实验下资源消息发生器（和query发生流程基本相似）（先产生query再产生资源消息）
 * 依据节点上的资源情况一次性生成所有的资源消息，不过资源消息的开始（传递）时间是不一样的
 * Created by Hong on 2017/7/29.
 */
public class TraceResourceMessageProducer implements Control {

    /** 信息协议 */
    private static final String PAR_IP_PROT = "information_protocol";
    /** 产生资源消息的时间跨度 */
    private static final String PAR_START_TIME_SPAN = "start_time_span";

    private final int ipPid;
    private final long startTimeSpan;

    public TraceResourceMessageProducer(String prefix){
        ipPid = Configuration.getPid(prefix + "." + PAR_IP_PROT);
        startTimeSpan = Configuration.getLong(prefix + "." + PAR_START_TIME_SPAN);
    }

    @Override
    public boolean execute() {
        // 遍历网络中的移动节点，根据节点上的资源情况产生相应的资源消息
        for (int i = 20; i < 97; i++) {
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

                // 存活时间和复制能力上限需要根据资源大小来进行计算
                double alpha; // 权重
                if (entry.getValue().getResourceListSize() > MyCommonState.maxResourceSize)
                    alpha = 1;
                else
                    alpha = entry.getValue().getResourceListSize() / MyCommonState.maxResourceSize;
                // 资源越大，存活时间越长，复制能力越弱
                long lifeTime = (long)(alpha * MyCommonState.maxMessageLifeTime);
                double relayCapacity = (1 - alpha) * MyCommonState.maxMessageRelayCapacity;
                resourceMessage.setLifeTime(lifeTime);
                resourceMessage.setRelayCapacity(relayCapacity);
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
