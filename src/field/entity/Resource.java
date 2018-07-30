package field.entity;

/**
 * 资源对应的实体类（暂不考虑关键字）
 * Created by Hong on 2017/7/9.
 */
public class Resource {

    /** 资源ID */
    private long resourceID;
    /** 资源类别 */
    private long typeID;
    /** 资源大小 */
    private double resourceSize;
    /** 资源评价值 */
    private double resourceValue;

    public Resource(){
        resourceID = -1;
        typeID = -1;
        resourceSize = -1;
        resourceValue = -1;
    }

    public long getResourceID() {
        return resourceID;
    }

    public void setResourceID(long resourceID) {
        this.resourceID = resourceID;
    }

    public long getTypeID() {
        return typeID;
    }

    public void setTypeID(long typeID) {
        this.typeID = typeID;
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
}
