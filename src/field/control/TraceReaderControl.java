package field.control;

import field.protocol.InformationProtocol;
import field.util.MyCommonState;
import peersim.config.Configuration;
import peersim.core.*;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

/**
 * 这是一个Control类
 * 作用是依据轨迹文件来进行动态拓扑调整
 * 根据相遇时间来判断在本周期内两节点是否为邻居
 * 由于协议类等尚未完善，所以有关于持续时间 & 相遇次数以后再补上
 * Created by Hong on 2017/7/8.
 */
public class TraceReaderControl implements Control{

    /**
     * MyLinkable协议
     * 这里需要特别注意，在配置文件中得写成linkable而不是mylinkable
     * 否则，FastConfig类中的getLinkable则没法正常使用
     */
    private static final String PAR_MY_LINKABLE_PROT = "linkable";

    /** 信息协议 */
    private static final String PAR_IP_PROT = "information_protocol";

    /** 存储轨迹数据的文件名 */
    private static final String PAR_FILE = "file";

    /** Control的开始时间 */
    private static final String PAR_START_TIME = "start_time";

    /** MyLinkable协议标识符 */
    private final int linkPid;
    private final int ipPid;
    private final String file;
    private final long startTime;

    public TraceReaderControl(String prefix){
        linkPid = Configuration.getPid(prefix + "." + PAR_MY_LINKABLE_PROT);
        ipPid = Configuration.getPid(prefix + "." + PAR_IP_PROT);
        file = Configuration.getString(prefix + "." + PAR_FILE);
        startTime = Configuration.getLong(prefix + "." + PAR_START_TIME);
    }

    @Override
    public boolean execute() {
        if (CommonState.getTime() >= startTime){
            // 遍历整个网络（注意这里的网络大小），更新邻居节点
            for (int i = 0; i < 99; i++){
                Node currentNode = Network.get(i);
                // 获取当前节点的MyLinkable协议
                MyLinkable myLinkable = (MyLinkable)currentNode.getProtocol(linkPid);
                // 先将当前节点的邻居节点删除
                while (myLinkable.degree() > 0)
                {
                    Node neighborNode = myLinkable.getNeighbor(0);
                    myLinkable.removeNeighbor(neighborNode);
                }
            }
            // 接下来是根据轨迹数据来添加新的邻居节点（相遇周期与相遇时间相关）
            try {
                FileReader fr = new FileReader(file);
                LineNumberReader lnr = new LineNumberReader(fr);
                String line;
                while ((line = lnr.readLine()) != null) {
                    String[] row = line.split("\\s+");
                    // 这个地方之后还需要修改增补
                    // 判断相遇时间是否在该周期的范围内，如果是，则相互添加邻居节点
                    if ((Long.parseLong(row[2]) >= 120 * (MyCommonState.cycles))
                            && (Long.parseLong(row[2]) <120 * (MyCommonState.cycles + 1))) {
                        // 获取节点的MyLinkable协议
                        Node currentNode = Network.get(Integer.parseInt(row[0]) - 1);
                        Node neighborNode = Network.get(Integer.parseInt(row[1]) - 1);
                        MyLinkable myLinkable = (MyLinkable)currentNode.getProtocol(linkPid);
                        MyLinkable neighborLinkable =
                                (MyLinkable)neighborNode.getProtocol(linkPid);
                        myLinkable.addNeighbor(neighborNode);
                        neighborLinkable.addNeighbor(currentNode);

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
        return false;
    }
}
