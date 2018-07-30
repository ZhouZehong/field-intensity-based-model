package field.handler;

import field.protocol.InformationProtocol;
import field.entity.Field;
import field.entity.Message.FieldSpreadMessage;
import field.initialize.FieldConstructor;
import field.util.JsonUtil;
import peersim.config.FastConfig;
import peersim.core.MyLinkable;
import peersim.core.Node;
import peersim.transport.Transport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Osmosis机制下处理信息场扩散信息的Handler
 * Created by Hong on 2017/8/19.
 */
public class OsmosisFieldSpreadMessageHandler extends Handler {

    @Override
    public void handleMessage(Node node, int protocolID, Object message) {
        // 此时的Message已从Json转换成Java对象（在InformationProtocol的processEvent里）
        FieldSpreadMessage fieldSpreadMessage = (FieldSpreadMessage) message;

        // 如果场强达到了扩散上限，则停止扩散
        if (fieldSpreadMessage.getField().getFieldStrength() < FieldConstructor.decayUpperLimit){
            return;
        }

        // 获取当前节点的信息协议
        InformationProtocol currentIp = (InformationProtocol)node.getProtocol(protocolID);

        try{
            Field msgField = fieldSpreadMessage.getField().clone(); // Message中携带的信息场
            // 扩散距离
            int decayDistance = fieldSpreadMessage.getHop();
            // 第一次扩散都是自己给自己发送消息，第二次才是真正扩散的开始
            if (decayDistance != 0){
                // 先计算扩散的信息场场强
                Map<Integer, Double> newDecayField = new HashMap<>();
                // 根据信息场扩散的公式计算扩散后的信息场场强
                for (Map.Entry<Integer, Double> entry : msgField.decayField.entrySet()) {
                    double newFieldStrength =
                            entry.getValue() * Math.pow(entry.getKey(), 2) / Math.pow(entry.getKey() + 1, 2);

//                    if (newFieldStrength > 100){
//                        System.out.println("error");
//                    }

                    if (newFieldStrength >= FieldConstructor.decayUpperLimit)
                        newDecayField.put(entry.getKey() + 1, newFieldStrength);
                }
                msgField.decayField = newDecayField;
                // 因为子信息场改变了，因此总场强需要重新计算
                msgField.setFieldStrength(msgField.calculateFieldStrength());

                // 接下来判断当前节点是否有该类别的信息场
                Field currentField = currentIp.fieldList.findFieldForType(
                        msgField.getQueryOrSource(), msgField.getTypeID());

                // 如果没有，则直接将该信息场进行添加
                if (currentField == null){
                    currentIp.fieldList.add(msgField);
                }
                // 如果有，则将原有信息场与现信息场进行叠加
                else{
                    for (Map.Entry<Integer, Double> entry : msgField.decayField.entrySet()){
                        if (!currentField.decayField.containsKey(entry.getKey())){
                            currentField.decayField.put(entry.getKey(), entry.getValue());
                        }
                        else {
                            double fieldStrength = currentField.decayField.get(entry.getKey());
                            fieldStrength += entry.getValue();

//                            if (fieldStrength > 100){
//                                System.out.println("error");
//                            }

                            currentField.decayField.put(entry.getKey(), fieldStrength);
                        }
                    }
                    currentField.setFieldStrength(currentField.calculateFieldStrength());
                }
            }

            // 无论扩散距离是否为0，都需要进一步扩散信息场
            FieldSpreadMessage newFieldSpreadMessage = new FieldSpreadMessage();
            newFieldSpreadMessage.setMessageSource(fieldSpreadMessage.getMessageSource());
            List<Long> spreadPath = new ArrayList<>(fieldSpreadMessage.spreadPath);
            spreadPath.add(node.getID());
            newFieldSpreadMessage.spreadPath = spreadPath;
            newFieldSpreadMessage.setHop(fieldSpreadMessage.getHop() + 1);
            newFieldSpreadMessage.setField(msgField);
            String json = JsonUtil.toJson(newFieldSpreadMessage);

            int linkableID = FastConfig.getLinkable(protocolID);
            // 获取当前节点的MyLinkable协议
            MyLinkable myLinkable = (MyLinkable) node.getProtocol(linkableID);
            // 将新的扩散消息发送给非消息源的其他邻居节点
            if (myLinkable.degree() > 0){
                for (int i = 0; i < myLinkable.degree(); i++) {
                    Node neighborNode = myLinkable.getNeighbor(i);
                    if (!newFieldSpreadMessage.spreadPath.contains(neighborNode.getID())){
                        ((Transport) node.getProtocol(FastConfig.getTransport(protocolID))).send(
                                node, neighborNode, json, protocolID);
//                        MyCommonState.bandwidthConsume++;
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
