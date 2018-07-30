package field.entity.Message;

/**
 * 在网络中流动的资源对应的实体类（流动起来后与消息类似）
 * Created by Hong on 2017/7/11.
 */
public class ResourceMessage extends Message implements Cloneable{

    private long resourceMessageID;
    /** 资源大小 */
    private double resourceSize;
    /** 资源评价值 */
    private double resourceValue;
    /** 资源消息的产生时间 */
    private long resourceMessageStartTime;

    public ResourceMessage(){
        this.set_class(ResourceMessage.class.getName());
    }

    public long getResourceMessageID() {
        return resourceMessageID;
    }

    public void setResourceMessageID(long resourceMessageID) {
        this.resourceMessageID = resourceMessageID;
    }

    public double getResourceSize() {
        return resourceSize;
    }

    public void setResourceSize(double resourceSize) {
        this.resourceSize = resourceSize;
    }

    public double getResourceValue() {
        return resourceValue;
    }

    public void setResourceValue(double resourceValue) {
        this.resourceValue = resourceValue;
    }

    public long getResourceMessageStartTime() {
        return resourceMessageStartTime;
    }

    public void setResourceMessageStartTime(long resourceMessageStartTime) {
        this.resourceMessageStartTime = resourceMessageStartTime;
    }

    @Override
    public ResourceMessage clone(){
        try {
            return (ResourceMessage) super.clone();
        }
        catch (CloneNotSupportedException e){
            e.printStackTrace();
        }
        return null;
    }
}
