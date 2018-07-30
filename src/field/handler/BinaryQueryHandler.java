package field.handler;

import field.initialize.FieldConstructor;
import field.protocol.InformationProtocol;
import field.util.MyCommonState;
import field.entity.Field;
import field.entity.Message.Query;
import field.entity.Message.ResourceMessage;
import field.entity.Resource;
import field.util.JsonUtil;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.MyLinkable;
import peersim.core.Node;
import peersim.transport.Transport;

import java.util.List;

/**
 * 固定备份机制（二分法）下处理query并对query进行正确地转发的Handler
 * Created by Hong on 2017/8/17.
 */
public class BinaryQueryHandler extends Handler {

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

        // 根据当前节点的资源场情况，来对query的RelayTime（可复制上限）进行调整
        Field currentField = informationProtocol.fieldList.findFieldForType(
                1, query.getTypeID());
        // 如果有与query相关的信息场，则减少query的RelayTime
        if (currentField != null) {
            if (query.getRelayCapacity() < currentField.getFieldStrength())
                query.setRelayCapacity(0);
            else
                query.setRelayCapacity(query.getRelayCapacity() - currentField.getFieldStrength());
        }
        else {
            query.setRelayCapacity(query.getRelayCapacity() - FieldConstructor.decayUpperLimit);
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
            // 再判断cache中是否已经存有该query
            Query existedQuery = informationProtocol.queryCache.containsQuery(query);
            if (existedQuery == null)
                informationProtocol.queryCache.add(queryForCache);
            else {
                informationProtocol.queryCache.remove(existedQuery);
                informationProtocol.queryCache.add(queryForCache);
            }

            // 获取当前周期内的邻居节点，并判断邻居节点的信息场信息
            int linkableID = FastConfig.getLinkable(protocolID);
            MyLinkable currentMyLinkable =
                    (MyLinkable) node.getProtocol(linkableID);
            if (currentMyLinkable.degree() > 0){
                query.setHop(query.getHop() + 1);
                for (int i = 0; i < currentMyLinkable.degree(); i++) {
                    Node neighbor = currentMyLinkable.getNeighbor(i);
                    // 获取邻居节点的信息协议
                    InformationProtocol neighborIp =
                            (InformationProtocol) neighbor.getProtocol(protocolID);
                    // 判断邻居节点上是否有同类型的信息场，且场强值大于当前节点
                    Field neighborField = neighborIp.fieldList.findFieldForType(
                            1, query.getTypeID());
                    // 判断邻居节点的query缓存中是否已经有了相关信息
                    Query neighborCacheQuery = neighborIp.queryCache.containsQuery(query);
                    if ((neighborCacheQuery == null)
                            && (queryForCache.getRelayCapacity() > 0)){
                        query.setRelayCapacity(queryForCache.getRelayCapacity() / 2);
                        queryForCache.setRelayCapacity(queryForCache.getRelayCapacity() / 2);
                        String json = JsonUtil.toJson(query);
                        ((Transport) node.getProtocol(FastConfig.getTransport(protocolID))).send
                                (node, neighbor, json, protocolID);
                        // 更新缓存内的query的转发能力值
                        if (neighborField != null)
                            queryForCache.setRelayCapacity
                                    (queryForCache.getRelayCapacity() - neighborField.getFieldStrength());
                        else
                            queryForCache.setRelayCapacity
                                    (queryForCache.getRelayCapacity() - FieldConstructor.decayUpperLimit);
                        MyCommonState.bandwidthConsume++;
                        continue;
                    }
                    // 另外一种情况，转发能力值已为0，才用直接等待的方式进行消息转发
                    List<Resource> neighborResourceForQuery =
                            neighborIp.resourceList.findResourceForQuery(query);
                    if ((neighborCacheQuery == null)
                            && (neighborResourceForQuery.size() > 0)){
                        String json = JsonUtil.toJson(query);
                        ((Transport) node.getProtocol(FastConfig.getTransport(protocolID))).send
                                (node, neighbor, json, protocolID);
                        MyCommonState.bandwidthConsume++;
                    }
                }
            }
        }
    }
}
