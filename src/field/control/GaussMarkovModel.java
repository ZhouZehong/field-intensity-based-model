package field.control;

import field.util.MyCommonState;
import field.protocol.CoordinatesProtocol;
import field.util.CommonUtil;
import peersim.config.Configuration;
import peersim.core.*;

/**
 * Created by Hong on 2017/6/29.
 * 这是一个Control类
 * 作用是根据高斯马尔科夫移动模型来对node间进行动态link连接或解连接
 * 进而形成动态拓扑（动态邻居/移动性）
 */
public class GaussMarkovModel implements Control{

    /**
     * MyLinkable协议
     * 这里需要特别注意，在配置文件中得写成linkable而不是mylinkable
     * 否则，FastConfig类中的getLinkable则没法正常使用
     */
    private static final String PAR_MY_LINKABLE_PROT = "linkable";
    /** 需要匹配一个坐标协议，MyLinkable协议参数在其父类中设置 */
    private static final String PAR_COORDINATES_PROT = "coord_protocol";
    /** 节点之间的感应距离上限 */
    private static final String PAR_INTERACT_RANGE = "interact_range";
    /** 节点移动区域的大小（正方形） */
    private static final String PAR_BORDER = "border";
    /**
     * alpha是高斯-马尔科夫移动模型中控制速度与方向变化的一个常数
     * 取值区间为[0，1]
     */
    private static final String PAR_ALPHA = "alpha";
    /** Control的开始时间 */
    private static final String PAR_START_TIME = "start_time";

    /** MyLinkable协议标识符 */
    private final int linkPid;
    /** 对应了坐标协议的pid */
    private final int coordPid;
    private final double interactRange;
    private final double border;
    private final double alpha;
    private final long startTime;

    public GaussMarkovModel(String prefix){
        if (Configuration.contains(prefix + "." + PAR_MY_LINKABLE_PROT)){
            linkPid = Configuration.getPid(prefix + "." + PAR_MY_LINKABLE_PROT);
        }
        else {
            linkPid = -10;
        }
        coordPid = Configuration.getPid(prefix + "." +PAR_COORDINATES_PROT);
        interactRange = Configuration.getDouble(prefix + "." + PAR_INTERACT_RANGE);
        border = Configuration.getDouble(prefix + "." + PAR_BORDER);
        alpha = Configuration.getDouble(prefix + "." + PAR_ALPHA);
        startTime = Configuration.getLong(prefix + "." + PAR_START_TIME);
    }

    @Override
    public boolean execute() {

        if (CommonState.getTime() >= startTime){
            double newV, newTheta, newX, newY, newAvgV, newAvgTheta;
            double oldV, oldTheta, oldX, oldY, oldAvgV, oldAvgTheta;

            // 先遍历整个网络，根据模型的公式对每个节点的位置、速度及运动方向进行更新
            for (int i = 0; i < Network.size(); i++){
                Node currentNode = Network.get(i);
                // 先获取坐标协议
                CoordinatesProtocol cdp = (CoordinatesProtocol) currentNode.getProtocol(coordPid);
                oldV = cdp.getV();
                oldTheta = cdp.getTheta();
                oldX = cdp.getX();
                oldY = cdp.getY();
                oldAvgV = cdp.getAvgV();
                oldAvgTheta = cdp.getAvgTheta();
                // 根据公式计算新的速度
                newV = alpha * oldV
                        + (1 - alpha) * oldAvgV
                        + Math.sqrt((1 - alpha * alpha)) * CommonState.r.nextGaussian();
                // 根据公式计算新的角度（方向）
                newTheta = alpha * oldTheta
                        + (1 - alpha) * oldAvgTheta
                        + Math.sqrt((1 - alpha * alpha)) * CommonState.r.nextGaussian();
                // 计算节点当前的位置
                newX = oldX + oldV * Math.cos(oldTheta * Math.PI / 180);
                newY = oldY + oldV * Math.sin(oldTheta * Math.PI / 180);
                // 计算新的平均速度和角度
                newAvgV = (oldAvgV * MyCommonState.cycles
                        + newV) / (MyCommonState.cycles + 1);
                newAvgTheta = (oldAvgTheta * MyCommonState.cycles
                        + newTheta) / (MyCommonState.cycles + 1);
                // 更新该节点的坐标协议信息
                cdp.setAvgV(newAvgV);
                // 如果超过了边界，则通过改变平均角度来进行调整
                double warningBoundary = border - 30;
                if ((newX > -warningBoundary) && (newX < warningBoundary)
                        && (newY > -warningBoundary) && (newY < warningBoundary)){
                    cdp.setAvgTheta(newAvgTheta);
                }
                else {
                    if (newX <= -warningBoundary){
                        // 左边界
                        if ((newY > -warningBoundary) && (newY < warningBoundary)) cdp.setAvgTheta(0);
                        else {
                            // 左下角边界
                            if (newY <= -warningBoundary) cdp.setAvgTheta(45);
                                // 左上角边界
                            else  cdp.setAvgTheta(315);
                        }
                    }
                    if (newX >= warningBoundary){
                        // 右边界
                        if ((newY > -warningBoundary) && (newY < warningBoundary)) cdp.setAvgTheta(180);
                        else {
                            // 右下角边界
                            if (newY <= -warningBoundary) cdp.setAvgTheta(135);
                                // 右上角边界
                            else  cdp.setAvgTheta(225);
                        }
                    }
                    // 下边界
                    if ((newX > -warningBoundary) && (newX < warningBoundary) && (newY <= -warningBoundary)) cdp.setAvgTheta(90);
                    // 上边界
                    if ((newX > -warningBoundary) && (newX < warningBoundary) && (newY >= warningBoundary)) cdp.setAvgTheta(270);
                }
                cdp.setTheta(newTheta);
                cdp.setV(newV);
//            // 注意不能越界了
//            if ((newX > -border) && (newX < border))
                cdp.setX(newX);
//            else {
//                if (newX < -border) cdp.setX(-border);
//                else cdp.setX(border);
//            }
//            if ((newY > -border) && (newY < border))
                cdp.setY(newY);
//            else {
//                if (newY < -border) cdp.setY(-border);
//                else cdp.setY(border);
//            }
            }
            // 更新周期数，以便于计算平均速度和角度（这里以后可能需要修改）
            MyCommonState.cycles++ ;

            // 再次遍历整个网络，根据节点的新位置来计算节点之间的距离，完成对邻居节点的更新
            for (int i = 0; i < Network.size(); i++){
                Node currentNode = Network.get(i);
                // 获取当前节点MyLinkable协议
                MyLinkable myLinkable = (MyLinkable)currentNode.getProtocol(linkPid);
//            // 获取当前节点坐标协议
//            CoordinatesProtocol cuurentCdp =
//                    (CoordinatesProtocol) currentNode.getProtocol(coordPid);
                // 先将当前的邻居节点删除
                while (myLinkable.degree() > 0)
                {
                    Node neighborNode = myLinkable.getNeighbor(0);
                    myLinkable.removeNeighbor(neighborNode);
                }
                // 根据距离是否属于感应范围来为节点添加新的邻居
                for (int j = 0; j < Network.size(); j++){
                    Node compareNode = Network.get(j);
//                MyLinkable comLinkable = (MyLinkable)compareNode.getProtocol(linkPid);
//                CoordinatesProtocol compareCdp =
//                        (CoordinatesProtocol) compareNode.getProtocol(coordPid);
                    if (CommonUtil.distance(currentNode, compareNode, coordPid) <= interactRange && j != i){
                        // 相互添加新的邻居节点
                        myLinkable.addNeighbor(compareNode);
//                    comLinkable.addNeighbor(currentNode);
                    }
                }
            }
        }
        return false;
    }
}
