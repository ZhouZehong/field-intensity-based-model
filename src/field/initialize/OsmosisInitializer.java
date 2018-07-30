package field.initialize;

import field.entity.OsmosisList;
import field.protocol.OsmosisProtocol;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

/**
 * Osmosis初始化类
 * Created by Hong on 2017/8/19.
 */
public class OsmosisInitializer implements Control {

    /** Osmosis协议 */
    private static final String PAR_OP_PROT = "osmosis_protocol";

    public static int opPid;

    public OsmosisInitializer(String prefix){
        opPid = Configuration.getPid(prefix + "." + PAR_OP_PROT);
    }
    @Override
    public boolean execute() {
        // 遍历整个网络，对Osmosis协议进行初始化
        for (int i = 0; i < Network.size(); i++) {
            Node currentNode = Network.get(i);
            // 获取当前节点的Osmosis协议
            OsmosisProtocol currentOp =
                    (OsmosisProtocol) currentNode.getProtocol(opPid);
            // 初始化每个节点上的Osmosis协议
            currentOp.osmosisList = new OsmosisList();
        }
        return false;
    }
}
