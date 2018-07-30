package field.handler;

import field.entity.Resource;
import field.protocol.InformationProtocol;
import field.util.MyCommonState;
import field.entity.Field;
import field.entity.Message.Query;
import field.entity.Message.ResourceMessage;
import field.entity.QueryResult;
import field.util.JsonUtil;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.MyLinkable;
import peersim.core.Node;
import peersim.transport.Transport;

import java.util.List;

/**
 * 直接等待模式下处理资源消息的Handler
 * 只有遇到了目标节点才进行资源转发，且无生命周期限制，从不进行进一步扩散
 * Created by Hong on 2017/8/9.
 */
public class WaitResourceMessageHandler extends Handler {

    @Override
    public void handleMessage(Node node, int protocolID, Object message) {
        // 由Json转换成的Java对象
        ResourceMessage resourceMessage = (ResourceMessage) message;
        // 获取当前节点的信息协议
        InformationProtocol currentIp =
                (InformationProtocol) node.getProtocol(protocolID);

        // 检查当前节点上的原生query是否能与Resource匹配，若匹配，则检索成功
        for (Query query : currentIp.queryCache.queries) {
            // 先判断是否为该节点自己产生的query
            if ((query.getMessageSource() == node.getID())
                    && (query.getTypeID() == resourceMessage.getTypeID())
                    && (query.getQueryStartTime() <= CommonState.getTime())) {
                // 再判断query的类型与资源是否匹配
                // 匹配成功，产生query result
                QueryResult queryResult = new QueryResult();
                queryResult.setQueryID(query.getQueryID());
                queryResult.setResourceMessageID(resourceMessage.getResourceMessageID());
                queryResult.setHitTime(CommonState.getTime() - query.getQueryStartTime());
                queryResult.setResourceValue(resourceMessage.getResourceValue());
                // 进行结果统计，由该query对应的listener接受该result进行统计
                MyCommonState.receiveQueryResult(query.getQueryID(), queryResult);
            }
        }

        // 将这个message（副本）放入当前节点的缓存中
        ResourceMessage resourceMessageForCache = resourceMessage.clone();
        // 先判断当前message是否已经超过存活时间而失效
        long resourceMessageLife = resourceMessage.getResourceMessageStartTime() + resourceMessage.getLifeTime();
        if (resourceMessageLife > CommonState.getTime()){

            // 判断是否为本节点产生的资源消息，是则可继续传递，不是则添加进缓存，不再继续传递
//            if (resourceMessage.getMessageSource() != node.getID()){
//                resourceMessageForCache.setRelayCapacity(0);
//                currentIp.resourceMessageCache.add(resourceMessageForCache);
//            }
//            else {
            currentIp.resourceMessageCache.add(resourceMessageForCache);
            int linkableID = FastConfig.getLinkable(protocolID);
            MyLinkable currentLp =
                    (MyLinkable) node.getProtocol(linkableID);
            if (currentLp.degree() > 0) {
                resourceMessage.setHop(resourceMessage.getHop() + 1);
                String json = JsonUtil.toJson(resourceMessage);
                for (int i = 0; i < currentLp.degree(); i++) {
                    Node neighborNode = currentLp.getNeighbor(i);
                    // 邻居节点的信息协议
                    InformationProtocol neighborIp =
                            (InformationProtocol) neighborNode.getProtocol(protocolID);
                    // 邻居节点上的相关信息场情况
                    Field neighborField = neighborIp.fieldList.findFieldForType
                            (0, resourceMessage.getTypeID());
                    // 邻居节点的缓存中是否已有当前资源消息
                    ResourceMessage neighborMessage =
                            neighborIp.resourceMessageCache.containsResourceMessage(resourceMessage);

                    // 判断邻居节点上是否有能够匹配的query，如果有才将资源消息传递过去
                    if (neighborMessage == null){
                        for (Query query : neighborIp.queryCache.queries) {
                            if ((query.getMessageSource() == neighborNode.getID())
                                    && (query.getTypeID() == resourceMessage.getTypeID())){
                                ((Transport) node.getProtocol(FastConfig.getTransport(protocolID)))
                                        .send(node, neighborNode, json, protocolID);
                                MyCommonState.bandwidthConsume += resourceMessage.getResourceSize();
                                break;
                            }
                        }
                    }
                }
            }
//            }
        }
    }
}
