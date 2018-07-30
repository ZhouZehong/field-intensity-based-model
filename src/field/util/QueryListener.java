package field.util;

import field.entity.QueryResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 在仿真过程中观察query的状态
 * Created by Hong on 2017/7/24.
 */
public class QueryListener {

    private long queryID;
    /** 资源首次命中时间 */
    private long firstHitTime;
    /** 总共命中的资源总评价值 */
    private double totalResourceValue;
    /** query结果集合 */
//    public List<QueryResult> queryResults;

    public QueryListener(){
        this.queryID = -1;
        this.firstHitTime = -1;
        this.totalResourceValue = 0;
//        this.queryResults = new ArrayList<>();
    }

    public long getQueryID() {
        return queryID;
    }

    public void setQueryID(long queryID) {
        this.queryID = queryID;
    }

    public long getFirstHitTime() {
        return firstHitTime;
    }

    public void setFirstHitTime(long firstHitTime) {
        this.firstHitTime = firstHitTime;
    }

    public double getTotalResourceValue() {
        return totalResourceValue;
    }

    public void setTotalResourceValue(double totalResourceValue) {
        this.totalResourceValue = totalResourceValue;
    }

    /**
     * 通过接收query结果来对相关指标进行记录与更新
     * @param queryResult 对应的一个结果
     */
    public void receiveQueryResult(QueryResult queryResult){
        if (queryResult.getResourceValue() > 0){
//            queryResults.add(queryResult);
            if (firstHitTime == -1)
                firstHitTime = queryResult.getHitTime();
            totalResourceValue += queryResult.getResourceValue();
        }
    }
}
