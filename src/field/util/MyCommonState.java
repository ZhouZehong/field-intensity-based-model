package field.util;

import field.entity.QueryResult;
import field.initialize.Initializer;
import field.util.QueryListener;
import peersim.core.CommonState;
import peersim.util.IncrementalStats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用来存储整个仿真过程中涉及到的状态
 * Created by Hong on 2017/7/5.
 */
public class MyCommonState {

    /** 周期数 */
    public static long cycles = 0;
    /** 类别总数 */
    public static int typeNum = -1;
    /** 统一带宽上限 */
    public static long uniformBandwidth = -1;
    /** 资源大小限制 */
    public static double maxResourceSize = -1;
    /** 资源评价值上限 */
    public static double maxResourceValue = -1;
    /** 用户感兴趣程度值上限*/
    public static int maxUserInterestDegree = -1;
    /** query的总数 */
    public static long queryCounter = 0;
    /** resourceMessage的总数 */
    public static long resourceMessageCounter = 0;
    /** 用来判断是否为真实数据实验 */
    public static boolean whetherTrace;
    /** 转发消息造成的带宽占用 */
    public static double bandwidthConsume = 0;
    /** 用来控制消息被复制的上限 */
    public static double maxMessageRelayCapacity = -1;
    /** 消息的存活时间 */
    public static long maxMessageLifeTime = -1;
    /** 找不到所需资源的个数 */
    public static long noResultCounter = 0;
    /** Osmosis总数 */
    public static long osmosisCounter = 0;

    /** 各个query与资源第一次命中的时长集合 */
    public static IncrementalStats firstHitTime = new IncrementalStats();
    /** 各个query命中的资源评价值综合集合 */
    public static IncrementalStats totalResourceValue = new IncrementalStats();

    /** queryListener集合《queryID，其对应的Listener》 */
    private static Map<Long, QueryListener> queryListenerMap = new HashMap<>();

    /**
     * 为queryListenerMap添加queryListener
     * @param queryID queryID
     */
    public static void addNewListener(long queryID, QueryListener queryListener){
        queryListenerMap.put(queryID, queryListener);
    }

    /**
     * 一个query对应一个Listener，接收结果集来对监听器中的指标进行更新
     * @param queryID queryID
     * @param queryResult query对应的一个结果
     */
    public static void receiveQueryResult(long queryID, QueryResult queryResult){
        QueryListener queryListener = queryListenerMap.get(queryID);
        queryListener.receiveQueryResult(queryResult);
    }

    /**
     * 各项评价指标值的统计
     */
    public static void calculate(){
        noResultCounter = 0;
        for (Map.Entry<Long, QueryListener> entry : queryListenerMap.entrySet()) {
            QueryListener queryListener = entry.getValue();
            if (queryListener.getFirstHitTime() != -1)
                firstHitTime.add(queryListener.getFirstHitTime());
            if (queryListener.getTotalResourceValue() > 0)
                totalResourceValue.add(queryListener.getTotalResourceValue());
            else
                noResultCounter++;
        }
    }
}
