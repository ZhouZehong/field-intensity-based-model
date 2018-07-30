package field.util;

import field.protocol.CoordinatesProtocol;
import peersim.core.CommonState;
import peersim.core.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * 常用方法
 * Created by Hong on 2017/7/5.
 */
public class CommonUtil {

    /**
     * 这是一个计算两个节点之间欧氏距离的方法
     * @param nodeA 节点A
     * @param nodeB 节点B
     * @param coordPid 坐标协议标识符
     * @return 两个节点之间的距离
     */
    public static double distance(Node nodeA, Node nodeB, int coordPid){
        double x1 =((CoordinatesProtocol)nodeA.getProtocol(coordPid))
                .getX();
        double x2 = ((CoordinatesProtocol)nodeB.getProtocol(coordPid))
                .getX();
        double y1 = ((CoordinatesProtocol)nodeA.getProtocol(coordPid))
                .getY();
        double y2 = ((CoordinatesProtocol)nodeB.getProtocol(coordPid))
                .getY();
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    /**
     * 从全路径中提取类名
     * @param classFullPath 全路径
     * @return 类名
     */
    public static String convert2SimpleName(String classFullPath){
        StringBuffer stringBuffer = new StringBuffer(classFullPath);
        int pos = stringBuffer.lastIndexOf(".");
        return stringBuffer.substring(pos + 1);
    }

    /**
     * 从list中随机挑选出一定数量的数据
     * @param list 存储数据的list
     * @param count 随机挑选的数量
     * @param <T> 泛型（List中的存储对象）
     * @return 随机挑选出的数据集合
     */
    public static <T> List<T> randomPickFromArray(List<T> list, long count){
        // 这步很重要，是为了不改变原有的List，因为Java传参传的都是引用
        List<T> cloneArrayList = new ArrayList<>(list);
        if (list.size() < count) return cloneArrayList;
        List<T> resultList = new ArrayList<>();
        while ((count > 0) && (cloneArrayList.size() > 0)){
            int randomIndex = CommonState.r.nextInt(cloneArrayList.size());
            T obj = cloneArrayList.get(randomIndex);
            resultList.add(obj);
            cloneArrayList.remove(obj);
            count--;
        }
        return resultList;
    }
}
