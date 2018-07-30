package field.initialize;

import field.entity.Message.Query;
import field.entity.ResourceListValue;
import field.protocol.InformationProtocol;
import field.util.MyCommonState;
import field.entity.Message.ResourceMessage;
import field.entity.Resource;
import field.util.JsonUtil;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.*;
import peersim.transport.Transport;

import java.util.Map;

/**
 * 直接等待机制下的资源消息发生器
 * Created by Hong on 2017/8/9.
 */
public class WaitResourceMessageProducer implements Control {

    /** MyLinkable协议 */
    private static final String PAR_MY_LINKABLE_PROT = "linkable";
    /** 信息协议 */
    private static final String PAR_IP_PROT = "information_protocol";

    private final int linkPid;
    private final int ipPid;

    public WaitResourceMessageProducer(String prefix){
        linkPid = Configuration.getPid(prefix + "." + PAR_MY_LINKABLE_PROT);
        ipPid = Configuration.getPid(prefix + "." + PAR_IP_PROT);
    }

    @Override
    public boolean execute() {
        // 遍历网络中的所有节点，根据节点上的资源情况产生相应的资源消息
        for (int i = 0; i < Network.size(); i++) {
            Node currentNode = Network.get(i);
            // 获取当前节点的MyLinkable协议
            MyLinkable currentLp =
                    (MyLinkable) currentNode.getProtocol(linkPid);
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
                resourceMessage.setMessageSource(i);
                resourceMessage.setTypeID(entry.getKey());

                // 得先将设置好的资源消息存放至当前节点的缓存中
                currentIp.resourceMessageCache.add(resourceMessage);
                String json = JsonUtil.toJson(resourceMessage);
                ((Transport) currentNode.getProtocol(FastConfig.getTransport(ipPid))).
                        send(currentNode, currentNode, json, ipPid);

                // 而后向邻居节点进行判断，合适的就转发（需计算带宽）
                if (currentLp.degree() > 0){
                    for (int j = 0; j < currentLp.degree(); j++) {
                        Node neighborNode = currentLp.getNeighbor(j);
                        // 获取邻居节点的信息协议
                        InformationProtocol neighborIp =
                                (InformationProtocol) neighborNode.getProtocol(ipPid);
                        // 判断邻居节点上是否有能资源相匹配的query
                        for (Query query : neighborIp.queryCache.queries) {
                            if ((query.getTypeID() == resourceMessage.getTypeID())
                                    && (query.getQueryStartTime() <= CommonState.getTime())){
                                ((Transport) currentNode.getProtocol(FastConfig.getTransport(ipPid))).
                                        send(currentNode, neighborNode, json, ipPid);
                                MyCommonState.bandwidthConsume =
                                        MyCommonState.bandwidthConsume + resourceMessage.getResourceSize() + 1;
                                break;
                            }
                        }
                    }
                }

            }
        }
        return false;
    }
}
