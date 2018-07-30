package field.initialize;

import field.entity.ResourceListValue;
import field.protocol.InformationProtocol;
import field.entity.Field;
import field.entity.Message.FieldSpreadMessage;
import field.util.JsonUtil;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Control;
import peersim.core.MyLinkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.transport.Transport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 信息场构建（初始化）
 * Created by Hong on 2017/7/17.
 */
public class FieldConstructor implements Control {

    /** 信息协议 */
    private static final String PAR_IP_PORT = "information_protocol";
    /** MyLinkable协议 */
    private static final String PAR_MY_LINKABLE_PROT = "linkable";
    /** 场强计算公式中的常量值k */
    private static final String PAR_K = "k";
    /** 信息场的扩散上限（场强小于该数值时信息场不再传递） */
    private static final String PAR_DECAY_UPPER_LIMIT = "decay_upper_limit";

    private final int ipPid;
    private final int linkPid;

    public static double k;
    public static double decayUpperLimit;
    public static long fieldCount;

    public FieldConstructor(String prefix){
        ipPid = Configuration.getPid(prefix + "." + PAR_IP_PORT);
        linkPid = Configuration.getPid(prefix + "." + PAR_MY_LINKABLE_PROT);
        k = Configuration.getDouble(prefix + "." + PAR_K);
        decayUpperLimit = Configuration.getDouble(prefix + "." + PAR_DECAY_UPPER_LIMIT);
    }

    @Override
    public boolean execute() {
        // 信息场的数量
        fieldCount = 0;
        // 遍历整个网络，依据已经初始化好的拓扑、资源与用户兴趣来构建节点上的信息场
        for (int i = 0; i < Network.size(); i++) {
            Node currentNode = Network.get(i);
            // 获取节点的MyLinkable协议
            MyLinkable currentLp = (MyLinkable)
                    currentNode.getProtocol(linkPid);
            // 获取节点的信息协议
            InformationProtocol currentIp = (InformationProtocol)
                    currentNode.getProtocol(ipPid);
            // 依据当前节点自身的资源与用户兴趣构建扩散距离为1的信息场
            // 资源场的构建
            Map<Long, ResourceListValue> totalResourceValue = currentIp.resourceList.calTotalValueForType();
            for (Map.Entry<Long, ResourceListValue> entry : totalResourceValue.entrySet()){
                Field field = new Field();
                field.setFieldID(fieldCount);
                fieldCount++;
                field.setQueryOrSource(1); // 依据资源产生的场为资源场
                field.setTypeID(entry.getKey());
                field.decayField.put(1, entry.getValue().getResourceListValue() * k); // 初始化扩散距离为1的子信息场
                double fieldStrength = field.calculateFieldStrength(); // 依据子信息场的信息计算目前的总场强
                field.setFieldStrength(fieldStrength);
                currentIp.fieldList.add(field);

                // 生成信息场扩散消息，在当前的拓扑下进行扩散
                FieldSpreadMessage fieldSpreadMessage = new FieldSpreadMessage();
                fieldSpreadMessage.setMessageSource(i);
                List<Long> spreadPath = new ArrayList<>();
                fieldSpreadMessage.spreadPath = spreadPath;
                fieldSpreadMessage.setHop(0);
                fieldSpreadMessage.setField(field);
                String json = JsonUtil.toJson(fieldSpreadMessage);
                // 先向自己发送消息，触发相关的Handler，进而将信息场扩散出去
                ((Transport)currentNode.getProtocol(FastConfig.getTransport(ipPid)))
                        .send(currentNode, currentNode, json, ipPid);
            }
            // 需求场的构建
            Map<Long, Double> totalInterestDegree = currentIp.userInterestList.calTotalDegreeForType();
            for (Map.Entry<Long, Double> entry : totalInterestDegree.entrySet()){
                Field field = new Field();
                field.setFieldID(fieldCount);
                fieldCount++;
                field.setQueryOrSource(0); // 依据用户兴趣产生的场为需求场
                field.setTypeID(entry.getKey());
                field.decayField.put(1, entry.getValue() * k);
                double fieldStrength = field.calculateFieldStrength();
                field.setFieldStrength(fieldStrength);
                currentIp.fieldList.add(field);

                // 生成信息场扩散消息，在当前的拓扑下进行扩散
                FieldSpreadMessage fieldSpreadMessage = new FieldSpreadMessage();
                fieldSpreadMessage.setMessageSource(i);
                List<Long> spreadPath = new ArrayList<>();
                fieldSpreadMessage.spreadPath = spreadPath;
                fieldSpreadMessage.setHop(0);
                fieldSpreadMessage.setField(field);
                String json = JsonUtil.toJson(fieldSpreadMessage);
                // 先向自己发送消息，出发相关的Handler，进而将信息场扩散出去
                ((Transport)currentNode.getProtocol(FastConfig.getTransport(ipPid)))
                        .send(currentNode, currentNode, json, ipPid);
            }
            // 依据初始化的邻居情况构建相遇节点缓存
//            if (currentLp.degree() > 0){
//                for (int j = 0; j < currentLp.degree(); j++) {
//                    if (!currentIp.nodeCache.containsKey(currentLp.getNeighbor(j))){
//                        currentIp.nodeCache.put(currentLp.getNeighbor(j), (long)1);
//                    }
//                    else {
//                        long frequency = currentIp.nodeCache.get(currentLp.getNeighbor(j));
//                        frequency++;
//                        currentIp.nodeCache.put(currentLp.getNeighbor(j), frequency);
//                    }
//                }
//            }
        }
        return false;
    }
}
