package field.handler;

import field.entity.Field;
import field.entity.Message.Query;
import field.entity.Message.ResourceMessage;
import field.entity.Resource;
import field.protocol.InformationProtocol;
import field.util.JsonUtil;
import field.util.MyCommonState;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.MyLinkable;
import peersim.core.Node;
import peersim.transport.Transport;

import java.util.List;


/**
 * 用于直接等待方法下的Query
 * Created by Hong on 2017/8/9
 */
public class WaitQueryHandler extends Handler {

    @Override
    public void handleMessage(Node node, int protocolID, Object message) {
        // 由Json转成的Java对象
        Query query = (Query) message;
        // 获取当前节点的信息协议
        InformationProtocol informationProtocol =
                (InformationProtocol) node.getProtocol(protocolID);

        // 先检查自身节点上是否有query所需要的资源类型
        List<Resource> resourceListForQuery =
                informationProtocol.resourceList.findResourceForQuery(query);
        // 如果找到了相匹配的资源，则根据资源生成资源消息，并将资源消息传递出去
        if (resourceListForQuery.size() > 0) {
            ResourceMessage resourceMessage = new ResourceMessage();
            resourceMessage.setResourceMessageID(MyCommonState.resourceMessageCounter);
            MyCommonState.resourceMessageCounter++;
            // 计算resourceSize & resourceValue
            double resourceSize = 0;
            double resourceValue = 0;
            for (Resource resource : resourceListForQuery) {
                resourceSize += resource.getResourceSize();
                resourceValue += resource.getResourceValue();
            }
            resourceMessage.setResourceSize(resourceSize);
            resourceMessage.setResourceValue(resourceValue);
            resourceMessage.setResourceMessageStartTime(CommonState.getTime());
            // resource message的源节点是无所谓的，所以记成前一跳节点既可
            resourceMessage.setMessageSource(node.getID());
            resourceMessage.setTypeID(query.getTypeID());
            resourceMessage.setLifeTime(MyCommonState.maxMessageLifeTime);
            resourceMessage.setRelayCapacity(MyCommonState.maxMessageRelayCapacity);
            resourceMessage.setHop(0);

            // 将资源消息发送出去，还是通过自己给自己发送消息来触发resource message的传递
            String json = JsonUtil.toJson(resourceMessage);
            ((Transport) node.getProtocol(FastConfig.getTransport(protocolID))).send
                    (node, node, json, protocolID);
            // 所以还要写一个ResourceMessage对应的Handler
        }

        // 将这个query（副本）放入节点的query缓存中（更新缓存）
        Query queryForCache = null;
        try {
            queryForCache = query.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        // 先判断当前query是否已经超过存活时间而失效
        long queryLife = query.getQueryStartTime() + query.getLifeTime();
        if (queryLife > CommonState.getTime()){

            // 判断是否为本节点产生的query，是则可传递，不是则添加进缓存，不再继续传递
            if (query.getMessageSource() != node.getID()){
                informationProtocol.queryCache.add(queryForCache);
            }
            else {
                informationProtocol.queryCache.remove(query);
                informationProtocol.queryCache.add(queryForCache);
            }
            int linkableID = FastConfig.getLinkable(protocolID);
            MyLinkable currentLp =
                    (MyLinkable) node.getProtocol(linkableID);
            if (currentLp.degree() > 0){
                query.setHop(query.getHop() + 1);
                String json = JsonUtil.toJson(query);
                for (int i = 0; i < currentLp.degree(); i++) {
                    Node neighborNode = currentLp.getNeighbor(i);
                    InformationProtocol neighborIp =
                            (InformationProtocol) neighborNode.getProtocol(protocolID);
                    Query neighborQuery = neighborIp.queryCache.containsQuery(query);
                    List<Resource> neighborResourceForQuery =
                            neighborIp.resourceList.findResourceForQuery(query);
                    if ((neighborQuery == null)
                            && (neighborResourceForQuery.size() > 0)){
                        ((Transport) node.getProtocol(FastConfig.getTransport(protocolID))).
                                send(node, neighborNode, json, protocolID);
                        MyCommonState.bandwidthConsume++;
                    }
                }
            }
        }
    }
}