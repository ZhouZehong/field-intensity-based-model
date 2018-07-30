package field.control;

import field.protocol.CoordinatesProtocol;
import field.util.MyCommonState;
import peersim.config.Configuration;
import peersim.core.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 将仿真过程中网络的拓扑结构导出，便于观察与检验
 * Created by Hong on 2017/7/4.
 */
public class TopologyObserver implements Control {

    private static final String PAR_LINKABLE_PROT = "linkable";
    private static final String PAR_COORDINATES_PROT = "coord_protocol";
    private static final String PAR_LOG_FILE_NAME = "log_file_name";

    private final int linkPid;
    private final int coordPid;
    private String filename;

    public TopologyObserver(String prefix){
        linkPid = Configuration.getPid(prefix + "." + PAR_LINKABLE_PROT);
        coordPid = Configuration.getPid(prefix + "." + PAR_COORDINATES_PROT);
        filename = Configuration.getString(prefix + "." + PAR_LOG_FILE_NAME);
    }

    @Override
    public boolean execute() {

        try {
            String tmpFilename = filename + (MyCommonState.cycles - 1);
            FileWriter fileWriter = new FileWriter(tmpFilename + ".txt");
            // 遍历网络中的节点
            for (int i = 0; i < Network.size(); i++){
                Node currentNode = Network.get(i);
                // 获取坐标协议
                CoordinatesProtocol currentCdp = (CoordinatesProtocol)currentNode.getProtocol(coordPid);
                List<Node> neighbour = getNeighbour(currentNode, linkPid);
                fileWriter.write("Node " + i);
                fileWriter.write("(" + currentCdp.getX() + ", " + currentCdp.getY() + ")"
                        + " v: " + currentCdp.getV()
                        + " theta: " + currentCdp.getTheta()
                        + " avgV: " + currentCdp.getAvgV()
                        + " avgTheta: " + currentCdp.getAvgTheta() + " \r\n");
                fileWriter.write("Neighbour: ");
                for (Node node : neighbour) {
//                    CoordinatesProtocol neighborCdp = (CoordinatesProtocol)node.getProtocol(coordPid);
                    fileWriter.write(node.getID() + ", " );
//                    fileWriter.write("( " + neighborCdp.getX() + ", " + neighborCdp.getY() + "), ");
                }
                fileWriter.write("\r\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取某一节点的邻居节点列表
     * @param node 当前节点
     * @param linkPid MyLinkable协议Pid
     * @return 当前节点的邻居节点集合
     */
    private static List<Node> getNeighbour(Node node, int linkPid){
        List<Node> neighbour = new ArrayList<>();
        MyLinkable myLinkable = (MyLinkable) node.getProtocol(linkPid);
        if (myLinkable.degree() > 0){
            for (int i = 0; i < myLinkable.degree(); i++){
                Node neighborNode = myLinkable.getNeighbor(i);
                neighbour.add(neighborNode);
            }
        }
        return neighbour;
    }
}
