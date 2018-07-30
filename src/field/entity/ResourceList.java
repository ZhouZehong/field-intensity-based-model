package field.entity;

import field.entity.Message.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 资源列表对应的实体类
 * Created by Hong on 2017/7/9.
 */
public class ResourceList {

    /** 资源列表 */
    public List<Resource> resources;

    public ResourceList(){
        resources = new ArrayList<>();
    }

    /**
     * 根据资源的类型来对评价值的总和进行计算（核心节点场强）
     * @return 各类型资源的评价值总和《类型，评价值总和》
     */
    public Map<Long, ResourceListValue> calTotalValueForType(){
        Map<Long, ResourceListValue> totalValue = new HashMap<>();
        for (Resource resource : resources) {
            if (!totalValue.containsKey(resource.getTypeID())){
                ResourceListValue resourceListValue = new ResourceListValue();
                resourceListValue.setResourceListSize(resource.getResourceSize());
                resourceListValue.setResourceListValue(resource.getResourceValue());
                totalValue.put(resource.getTypeID(), resourceListValue);
            }
            else {
                ResourceListValue resourceListValue = totalValue.get(resource.getTypeID());
                resourceListValue.setResourceListValue
                        (resourceListValue.getResourceListValue() + resource.getResourceValue());
                resourceListValue.setResourceListSize
                        (resourceListValue.getResourceListSize() + resource.getResourceSize());
                totalValue.put(resource.getTypeID(), resourceListValue);
            }
        }
        return totalValue;
    }

    /**
     * 从资源列表中找出query所需资源
     * @param query query
     * @return query所需资源的资源列表
     */
    public List<Resource> findResourceForQuery(Query query){
        List<Resource> resourceListForQuery = new ArrayList<>();
        for (Resource resource : resources) {
            // 类型相同即可视为所需资源
            if (resource.getTypeID() == query.getTypeID())
                resourceListForQuery.add(resource);
        }
        return resourceListForQuery;
    }

    public int size(){
        return resources.size();
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

}
