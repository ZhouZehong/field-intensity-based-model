package field.control;

import field.entity.Resource;
import field.initialize.FieldConstructor;
import field.protocol.InformationProtocol;
import field.util.MyCommonState;
import field.entity.Field;
import field.entity.Message.FieldSpreadMessage;
import field.entity.Message.Query;
import field.entity.Message.ResourceMessage;
import field.util.JsonUtil;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.*;
import peersim.transport.Transport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 由于邻居状态会发生周期性变化，因此，
 * 节点上的信息场状态、query缓存、资源消息缓存也会随之发生变化，此Control类用于控制在每个周期内
 * 当邻居状态发生变化后，对新的邻居节点发送自身的信息场、query和资源消息
 * Created by Hong on 2017/7/30.
 */
public class CycleControl implements Control{

    /** 信息协议 */
    private static final String PAR_IP_PROT = "information_protocol";
    /** MyLinkable协议 */
    private static final String PAR_MY_LINKABLE_PROT = "linkable";
    /** Control的开始时间 */
    private static final String PAR_START_TIME = "start_time";
    /** 信息场强度在每个周期内的衰减程度 */
    private static final String PAR_DECAY = "decay";

    private final int ipPid;
    private final int linkPid;
    private final long startTime;
    private final long decay;

    public CycleControl(String prefix){
        ipPid = Configuration.getPid(prefix + "." + PAR_IP_PROT);
        linkPid = Configuration.getPid(prefix + "." + PAR_MY_LINKABLE_PROT);
        startTime = Configuration.getLong(prefix + "." + PAR_START_TIME);
        decay = Configuration.getLong(prefix + "." + PAR_DECAY);
    }

    @Override
    public boolean execute() {
        if (CommonState.getTime() >= startTime){

            // 遍历整个网络，依据当前节点现有的缓存和信息场信息来对新的邻居进行消息传递
            for (int i = 0; i < Network.size(); i++) {
                Node currentNode = Network.get(i);
                // 获取当前节点的信息协议
                InformationProtocol currentIp =
                        (InformationProtocol) currentNode.getProtocol(ipPid);
                // 获取当前节点的MyLinkable协议
                MyLinkable currentLp =
                        (MyLinkable) currentNode.getProtocol(linkPid);

                // 先进行信息场强度的衰减操作
                for (Field field : currentIp.fieldList.fields) {
                    Iterator<Map.Entry<Integer, Double>> iterator = field.decayField.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<Integer, Double> entry = iterator.next();
                        if (entry.getKey() != 1) {
                            if ((entry.getValue() - decay) <= 0) {
                                entry.setValue((double)0);
                                iterator.remove();
                            } else {
                                entry.setValue(entry.getValue() - decay);
                            }
                        }
                    }
                }

                // 遍历当前节点的所有邻居
                if (currentLp.degree() > 0){
                    for (int j = 0; j < currentLp.degree(); j++) {
                        Node neighborNode = currentLp.getNeighbor(j);
                        // 获取邻居节点的信息协议
                        InformationProtocol neighborIp =
                                (InformationProtocol) neighborNode.getProtocol(ipPid);
                        // 该节点对该邻居的带宽上限
                        long bandwidthLimit;
                        if (MyCommonState.whetherTrace){
                            bandwidthLimit =
                                    currentIp.meetingTime.get(neighborNode) * MyCommonState.uniformBandwidth;
                        }
                        else {
                            bandwidthLimit = MyCommonState.uniformBandwidth;
                        }

                        // 更新相遇节点缓存nodeCache
//                    if (!currentIp.nodeCache.containsKey(neighborNode))
//                        currentIp.nodeCache.put(neighborNode, (long)1);
//                    else {
//                        long frequency = currentIp.nodeCache.get(neighborNode);
//                        frequency++;
//                        currentIp.nodeCache.put(neighborNode, frequency);
//                    }

                        // 优先进行信息场信息的扩散
                        for (Field field : currentIp.fieldList.fields) {
                            if ((bandwidthLimit > 0)
                                    && (field.getFieldStrength() >= FieldConstructor.decayUpperLimit)){
                                FieldSpreadMessage fieldSpreadMessage = new FieldSpreadMessage();
                                fieldSpreadMessage.setMessageSource(i);
                                List<Long> spreadPath = new ArrayList<>();
                                spreadPath.add(currentNode.getID());
                                fieldSpreadMessage.spreadPath = spreadPath;
                                fieldSpreadMessage.setHop(1);
                                fieldSpreadMessage.setField(field);
                                bandwidthLimit--;
                                String json = JsonUtil.toJson(fieldSpreadMessage);
                                ((Transport) currentNode.getProtocol(FastConfig.getTransport(ipPid))).
                                        send(currentNode, neighborNode, json, ipPid);
                                MyCommonState.bandwidthConsume++;
                            }
                            else break;
                        }

                        // 然后是query的传递
                        List<Query> cloneQueries = new ArrayList<>(currentIp.queryCache.queries);
                        // 先清理掉过期query和未开始运作的query（不做转发）
                        for (int k = 0; k < cloneQueries.size(); k++) {
                            Query query = cloneQueries.get(k);
                            long queryLife = query.getQueryStartTime() + query.getLifeTime();
                            if (queryLife < CommonState.getTime()){
                                cloneQueries.remove(query);
                                currentIp.queryCache.remove(query);
                                k--;
                            }
                            if (query.getQueryStartTime() > CommonState.getTime()){
                                cloneQueries.remove(query);
                                k--;
                            }
                        }
                        // 优先选择还在存活期内，且存活时间最短的query进行转发
                        while ((cloneQueries.size() != 0) && (bandwidthLimit > 0)) {
                            Query minLifeTimeQuery = cloneQueries.get(0);
                            for (Query query : cloneQueries) {
                                if (query.getQueryStartTime() < minLifeTimeQuery.getQueryStartTime())
                                    minLifeTimeQuery = query;
                            }
                            cloneQueries.remove(minLifeTimeQuery);
                            // 先判断是否还有继续转发的能力
                            Field currentField = currentIp.fieldList.
                                    findFieldForType(1, minLifeTimeQuery.getTypeID());
                            Field neighborField = neighborIp.fieldList.
                                    findFieldForType(1, minLifeTimeQuery.getTypeID());
                            Query cacheQuery = currentIp.queryCache.containsQuery(minLifeTimeQuery);
                            Query neighborQuery = neighborIp.queryCache.containsQuery(minLifeTimeQuery);
                            if ((neighborField != null)
                                    && (neighborQuery == null)
                                    && (minLifeTimeQuery.getRelayCapacity() > 0)
                                    && ((currentField == null)
                                    || (neighborField.getFieldStrength() > currentField.getFieldStrength()))){
                                minLifeTimeQuery.setHop(minLifeTimeQuery.getHop() + 1);
                                String json = JsonUtil.toJson(minLifeTimeQuery);
                                ((Transport) currentNode.getProtocol(FastConfig.getTransport(ipPid))).
                                        send(currentNode, neighborNode, json, ipPid);
                                bandwidthLimit--;
                                cacheQuery.setRelayCapacity
                                        (cacheQuery.getRelayCapacity() - neighborField.getFieldStrength());
                                MyCommonState.bandwidthConsume++;
                                continue;
                            }
                            // 另外一种情况，相遇节点的信息场强度并不比当前节点强，但其上的确有所需资源
                            List<Resource> neighborResourceForQuery =
                                    neighborIp.resourceList.findResourceForQuery(minLifeTimeQuery);
                            if ((neighborQuery == null)
                                    && (neighborResourceForQuery.size() > 0)){
                                minLifeTimeQuery.setHop(minLifeTimeQuery.getHop() + 1);
                                String json = JsonUtil.toJson(minLifeTimeQuery);
                                ((Transport) currentNode.getProtocol(FastConfig.getTransport(ipPid))).
                                        send(currentNode, neighborNode, json, ipPid);
                                bandwidthLimit--;
                                MyCommonState.bandwidthConsume++;
                            }
                        }

                        // 最后是资源消息的传递
                        List<ResourceMessage> cloneResourceMessages =
                                new ArrayList<>(currentIp.resourceMessageCache.resourceMessages);
                        for (int k = 0; k < cloneResourceMessages.size(); k++) {
                            ResourceMessage resourceMessage = cloneResourceMessages.get(k);
                            long resourceMessageLife =
                                    resourceMessage.getResourceMessageStartTime() + resourceMessage.getLifeTime();
                            if (resourceMessageLife < CommonState.getTime()){
                                cloneResourceMessages.remove(resourceMessage);
                                currentIp.resourceMessageCache.resourceMessages.remove(resourceMessage);
                                k--;
                            }
                            if (resourceMessage.getResourceMessageStartTime() > CommonState.getTime()){
                                cloneResourceMessages.remove(resourceMessage);
                                k--;
                            }
                        }
                        while ((cloneResourceMessages.size() != 0) && (bandwidthLimit > 0)){
                            ResourceMessage maxValueMessage = cloneResourceMessages.get(0);
                            for (ResourceMessage resourceMessage : cloneResourceMessages) {
                                if (resourceMessage.getResourceValue() >
                                        maxValueMessage.getResourceValue())
                                    maxValueMessage = resourceMessage;
                            }
                            cloneResourceMessages.remove(maxValueMessage);
                            Field currentField = currentIp.fieldList.findFieldForType
                                    (0, maxValueMessage.getTypeID());
                            Field neighborField = neighborIp.fieldList.findFieldForType
                                    (0, maxValueMessage.getTypeID());
                            ResourceMessage cacheMessage =
                                    currentIp.resourceMessageCache.containsResourceMessage(maxValueMessage);
                            ResourceMessage neighborMessage =
                                    neighborIp.resourceMessageCache.containsResourceMessage(maxValueMessage);
                            if ((neighborField != null)
                                    && (neighborMessage == null)
                                    && (maxValueMessage.getRelayCapacity() > 0)
                                    && ((currentField == null)
                                    || (neighborField.getFieldStrength() > currentField.getFieldStrength()))){
                                maxValueMessage.setHop(cacheMessage.getHop() + 1);
                                String json = JsonUtil.toJson(maxValueMessage);
                                ((Transport) currentNode.getProtocol(FastConfig.getTransport(ipPid))).
                                        send(currentNode, neighborNode, json, ipPid);
                                bandwidthLimit = bandwidthLimit - (long) maxValueMessage.getResourceSize();
                                cacheMessage.setRelayCapacity
                                        (cacheMessage.getRelayCapacity() - neighborField.getFieldStrength());
                                MyCommonState.bandwidthConsume =
                                        MyCommonState.bandwidthConsume + maxValueMessage.getResourceSize();
                                continue;
                            }
                            // 仅根据场强无法将资源消息传递给相遇节点，但相遇节点上的确有能够相匹配的query
                            for (Query query : neighborIp.queryCache.queries) {
                                if (neighborMessage != null) break;
                                if ((query.getMessageSource() == neighborNode.getID())
                                        && (query.getTypeID() == maxValueMessage.getTypeID())){
                                    maxValueMessage.setHop(cacheMessage.getHop() + 1);
                                    String json = JsonUtil.toJson(maxValueMessage);
                                    ((Transport) currentNode.getProtocol(FastConfig.getTransport(ipPid))).
                                            send(currentNode, neighborNode, json, ipPid);
                                    bandwidthLimit = bandwidthLimit - (long) maxValueMessage.getResourceSize();
                                    MyCommonState.bandwidthConsume =
                                            MyCommonState.bandwidthConsume + maxValueMessage.getResourceSize();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
