package field.entity;

/**
 * 单个query结果对应的实体类
 * Created by Hong on 2017/7/20.
 */
public class QueryResult {

    /** queryID */
    private long queryID;
    /** 资源消息ID */
    private long resourceMessageID;
    /** 资源流至需求节点时消耗的时间 */
    private long hitTime;
    /** 获取到资源的资源质量值 */
    private double resourceValue;

    public QueryResult(){
        queryID = -1;
        resourceMessageID = -1;
        hitTime = -1;
        resourceValue = -1;
    }

    public long getQueryID() {
        return queryID;
    }

    public void setQueryID(long queryID) {
        this.queryID = queryID;
    }

    public long getHitTime() {
        return hitTime;
    }

    public void setHitTime(long hitTime) {
        this.hitTime = hitTime;
    }

    public double getResourceValue() {
        return resourceValue;
    }

    public void setResourceValue(double resourceValue) {
        this.resourceValue = resourceValue;
    }

    public long getResourceMessageID() {
        return resourceMessageID;
    }

    public void setResourceMessageID(long resourceMessageID) {
        this.resourceMessageID = resourceMessageID;
    }
}
