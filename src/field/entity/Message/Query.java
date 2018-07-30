package field.entity.Message;

/**
 * Query消息对应的实体类（继承了Message）
 * Created by Hong on 2017/7/11.
 */
public class Query extends Message implements Cloneable{

    private long queryID;
    /** query产生的时间 */
    private long queryStartTime;

    public Query(){
        this.set_class(Query.class.getName());
    }

    public long getQueryID() {
        return queryID;
    }

    public void setQueryID(long queryID) {
        this.queryID = queryID;
    }

    public long getQueryStartTime() {
        return queryStartTime;
    }

    public void setQueryStartTime(long queryStartTime) {
        this.queryStartTime = queryStartTime;
    }

    /**
     * 浅复制即可
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    public Query clone() throws CloneNotSupportedException{
        return (Query) super.clone();
    }
}
