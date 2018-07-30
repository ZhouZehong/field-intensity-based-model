package field.initialize;

import field.util.CommonUtil;
import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.WireGraph;
import peersim.graph.Graph;

/**
 * 继承了WireGraph，在初始化时根据已初始化的节点坐标进行画图
 * Created by Hong on 2017/7/5.
 */
public class WireCoordinatesTopology extends WireGraph {

    /** 需要匹配一个坐标协议，MyLinkable协议参数在其父类中设置 */
    private static final String PAR_COORDINATES_PROT = "coord_protocol";
    /** 节点之间的感应距离上限 */
    private static final String PAR_INTERACT_RANGE = "interact_range";

    /** 对应了坐标协议的pid */
    private final int coordPid;
    private final double interactRange;

    public WireCoordinatesTopology(String prefix){
        super(prefix);
        coordPid = Configuration.getPid(prefix + "." +PAR_COORDINATES_PROT);
        interactRange = Configuration.getDouble(prefix + "." + PAR_INTERACT_RANGE);
    }

    @Override
    public void wire(Graph g) {
        for (int i = 0; i < Network.size(); i++){
            Node currentNode = Network.get(i);
            for (int j = i + 1; j < Network.size(); j++){
                Node compareNode = Network.get(j);
                if (CommonUtil.distance(currentNode, compareNode, coordPid) <= interactRange){
                    g.setEdge((int) currentNode.getID(), (int) compareNode.getID());
                    g.setEdge((int)compareNode.getID(), (int)currentNode.getID());
                }
            }
        }
    }
}
