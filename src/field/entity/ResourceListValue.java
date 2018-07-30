package field.entity;

/**
 * 用于记录一个资源列表的资源大小及评价值总和
 * Created by Hong on 2017/8/7
 */
public class ResourceListValue {
    private double resourceListSize;
    private double resourceListValue;

    public ResourceListValue(){
        resourceListSize = -1;
        resourceListValue = -1;
    }

    public double getResourceListSize() {
        return resourceListSize;
    }

    public void setResourceListSize(double resourceListSize) {
        this.resourceListSize = resourceListSize;
    }

    public double getResourceListValue() {
        return resourceListValue;
    }

    public void setResourceListValue(double resourceListValue) {
        this.resourceListValue = resourceListValue;
    }
}
