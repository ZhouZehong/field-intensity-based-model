package field.initialize;

import field.protocol.InformationProtocol;
import field.util.MyCommonState;
import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.WireGraph;
import peersim.graph.Graph;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

/**
 * 这是一个继承了WireGraph的类
 * 目的是从读取真实的轨迹数据来进行模拟实验
 * PS：由于协议还不完整，因此涉及到缓存和带宽的属性（相遇次数 & 相遇时长）暂不考虑
 * Created by Hong on 2017/7/7.
 */
public class MyWireFromFile extends WireGraph {

    /** 信息协议 */
    private static final String PAR_IP_PROT = "information_protocol";
    /** 存储轨迹数据的文件名 */
    private static final String PAR_FILE = "file";

    private final int ipPid;
    private final String file;

    public MyWireFromFile(String prefix){
        super(prefix);
        ipPid = Configuration.getPid(prefix + "." + PAR_IP_PROT);
        file = Configuration.getString(prefix + "." + PAR_FILE);
    }

    @Override
    public void wire(Graph g) {
        try {
            FileReader fr = new FileReader(file);
            LineNumberReader lnr = new LineNumberReader(fr);
            String line;
            while ((line = lnr.readLine()) != null){
                String[] row = line.split("\\s+");
                // 这个地方之后还需要修改增补
                // 判断相遇时间是否在该周期的范围内，如果是，则添加邻居节点
                if ((Long.parseLong(row[2]) >= 120 * (MyCommonState.cycles ))
                        && (Long.parseLong(row[2]) < 120 * (MyCommonState.cycles + 1))){
                    g.setEdge(Integer.parseInt(row[0]) - 1, Integer.parseInt(row[1]) - 1);
                    g.setEdge(Integer.parseInt(row[1]) - 1, Integer.parseInt(row[0]) - 1);

                    Node currentNode = Network.get(Integer.parseInt(row[0]) - 1);
                    Node neighborNode = Network.get(Integer.parseInt(row[1]) - 1);

                    // 获取当前节点的信息协议
                    InformationProtocol currentIp =
                            (InformationProtocol) currentNode.getProtocol(ipPid);
                    currentIp.meetingTime.put(neighborNode, Long.parseLong(row[3]) - Long.parseLong(row[2]) + 1);

                    // 获取邻居节点的信息协议
                    InformationProtocol neighborIp =
                            (InformationProtocol) neighborNode.getProtocol(ipPid);
                    neighborIp.meetingTime.put(currentNode, Long.parseLong(row[3]) - Long.parseLong(row[2]) + 1);
                }
            }
            fr.close();
            MyCommonState.cycles++;
        }
        catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
