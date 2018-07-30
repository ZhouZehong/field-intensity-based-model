package field.handler;

import field.entity.Osmosis;
import field.initialize.FieldConstructor;
import field.initialize.OsmosisInitializer;
import field.protocol.InformationProtocol;
import field.protocol.OsmosisProtocol;
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

/**
 * Osmosis机制下处理ResourceMessage并对其进行正确地转发的Handler
 * Created by Hong on 2017/8/19.
 */
public class OsmosisResourceMessageHandler extends Handler {

    @Override
    public void handleMessage(Node node, int protocolID, Object message) {
        // 由Json转换成的Java对象
        ResourceMessage resourceMessage = (ResourceMessage) message;
        // 获取当前节点的信息协议
        InformationProtocol currentIp =
                (InformationProtocol) node.getProtocol(protocolID);
        // 获取当前节点的Osmosis协议
        OsmosisProtocol currentOp =
                (OsmosisProtocol) node.getProtocol(OsmosisInitializer.opPid);

        // 检查当前节点上的原生query是否能与Resource匹配，若匹配，则检索成功
        for (Query query : currentIp.queryCache.queries) {
            // 先判断是否为该节点自己产生的query
            if ((query.getMessageSource() == node.getID())
                    && (query.getTypeID() == resourceMessage.getTypeID())
                    && (query.getQueryStartTime() <= CommonState.getTime())){
                QueryResult queryResult = new QueryResult();
                queryResult.setQueryID(query.getQueryID());
                queryResult.setResourceMessageID(resourceMessage.getResourceMessageID());
                queryResult.setHitTime(CommonState.getTime() - query.getQueryStartTime());
                queryResult.setResourceValue(resourceMessage.getResourceValue());
                // 进行结果统计，由该query对应的listener接受该result进行统计
                MyCommonState.receiveQueryResult(query.getQueryID(), queryResult);
            }
        }

        // 更新当前节点上的Osmosis集合
        Osmosis currentOsmosis = currentOp.osmosisList.findOsmosisForType(resourceMessage.getTypeID());
        // 判断当前节点上是否已有同类型的Osmosis，若有则在其基础上修改，没有则添加一个新的Osmosis
        if (currentOsmosis == null){
            Osmosis osmosis = new Osmosis();
            osmosis.setOsmosisID(MyCommonState.osmosisCounter);
            MyCommonState.osmosisCounter++;
            osmosis.setTypeID(resourceMessage.getTypeID());
            osmosis.setSolute(0);
            osmosis.setSolvent(2);
            osmosis.setConcentration(osmosis.calculateCon());
            currentOp.osmosisList.add(osmosis);
        }
        else {
            currentOsmosis.setSolvent(currentOsmosis.getSolvent() + 1);
            currentOsmosis.setConcentration(currentOsmosis.calculateCon());
        }

        // 根据当前节点的需求场情况，对resource message的RelayTime（可复制上限）进行调整
        Field currentField =  currentIp.fieldList.findFieldForType
                (0, resourceMessage.getTypeID());
        if (currentField != null){
            if (resourceMessage.getRelayCapacity() < currentField.getFieldStrength())
                resourceMessage.setRelayCapacity(0);
            else
                resourceMessage.setRelayCapacity(resourceMessage.getRelayCapacity() - currentField.getFieldStrength());
        }
        else {
            resourceMessage.setRelayCapacity(resourceMessage.getRelayCapacity() - FieldConstructor.decayUpperLimit);
        }

        // 将这个message（副本）放入当前节点的缓存中
        ResourceMessage resourceMessageForCache = resourceMessage.clone();
        // 先判断当前message是否已经超过存活时间而失效
        long resourceMessageLife = resourceMessage.getResourceMessageStartTime() + resourceMessage.getLifeTime();
        if (resourceMessageLife > CommonState.getTime()){

            // 再判断cache中是否已经存有该resource message
            ResourceMessage existedResourceMessage =
                    currentIp.resourceMessageCache.containsResourceMessage(resourceMessage);
            if (existedResourceMessage == null)
                currentIp.resourceMessageCache.add(resourceMessageForCache);
            else {
                    currentIp.resourceMessageCache.remove(existedResourceMessage);
                    currentIp.resourceMessageCache.add(resourceMessageForCache);
            }

            // 获取当前节点的邻居节点及其相关的信息场信息
            int linkableID = FastConfig.getLinkable(protocolID);
            MyLinkable currentMyLinkable =
                    (MyLinkable) node.getProtocol(linkableID);
            if (currentMyLinkable.degree() > 0){
                resourceMessage.setHop(resourceMessage.getHop() + 1);
                String json = JsonUtil.toJson(resourceMessage);
                for (int i = 0; i < currentMyLinkable.degree(); i++) {
                    Node neighbor = currentMyLinkable.getNeighbor(i);
                    // 获取邻居节点的信息协议
                    InformationProtocol neighborIp =
                            (InformationProtocol) neighbor.getProtocol(protocolID);
                    // 获取邻居节点的Osmosis协议
                    OsmosisProtocol neighborOp =
                            (OsmosisProtocol) neighbor.getProtocol(OsmosisInitializer.opPid);
                    // 判断邻居节点的缓存中是否已有了相关信息
                    ResourceMessage neighborCacheResourceMessage =
                            neighborIp.resourceMessageCache.containsResourceMessage(resourceMessage);
                    // 判断邻居节点上是否有同类型的信息场
                    Field neighborField = neighborIp.fieldList.findFieldForType
                            (0, resourceMessage.getTypeID());
                    // 判断邻居节点上是否有相同的Osmosis，且浓度大于当前节点
                    Osmosis neighborOsmosis = neighborOp.osmosisList.findOsmosisForType(resourceMessage.getTypeID());
                    if ((neighborOsmosis != null)
                            && (neighborCacheResourceMessage == null)
                            && (resourceMessageForCache.getRelayCapacity() > 0)
                            && ((currentOsmosis == null)
                            || (neighborOsmosis.getConcentration() > currentOsmosis.getConcentration()))){
                        ((Transport) node.getProtocol(FastConfig.getTransport(protocolID))).send
                                (node, neighbor, json, protocolID);
                        // 更新缓存内消息的转发能力值
                        if (neighborField != null)
                            resourceMessageForCache.setRelayCapacity
                                    (resourceMessageForCache.getRelayCapacity() - neighborField.getFieldStrength());
                        else
                            resourceMessageForCache.setRelayCapacity
                                    (resourceMessageForCache.getRelayCapacity() - FieldConstructor.decayUpperLimit);
                        MyCommonState.bandwidthConsume =
                                MyCommonState.bandwidthConsume + resourceMessage.getResourceSize();
                        continue;
                    }

                    // 判断邻居节点上是否有能够匹配的query，如果有，则无论如何都要将依然存活的资源传递过去
                    for (Query query : neighborIp.queryCache.queries) {
                        if (neighborCacheResourceMessage != null) break;
                        if ((query.getMessageSource() == neighbor.getID())
                                && (query.getTypeID() == resourceMessage.getTypeID())){
                            ((Transport) node.getProtocol(FastConfig.getTransport(protocolID))).
                                    send(node, neighbor, json, protocolID);
                            MyCommonState.bandwidthConsume =
                                    MyCommonState.bandwidthConsume + resourceMessage.getResourceSize();
                            break;
                        }
                    }
                }
            }
        }
    }
}
