package field.support;

import field.util.MyCommonState;
import field.entity.Resource;
import field.util.CommonUtil;
import peersim.core.CommonState;

import java.util.ArrayList;
import java.util.List;

/**
 * 所有的资源
 * Created by Hong on 2017/7/13.
 */
public class ResourceDb {

    private static List<Resource> resourceDb;

    /**
     * 初始化整个网络中的资源数据，为每一个资源对象初始化相关变量
     * @param resourceNum 资源总量
     */
    public static void init(int resourceNum){
        resourceDb = new ArrayList<>();
        for (int i = 0; i < resourceNum; i++){
            Resource resource = new Resource();
            resource.setResourceID(i);
            resource.setTypeID(CommonState.r.nextInt(MyCommonState.typeNum));
            resource.setResourceSize(CommonState.r.nextDouble() * MyCommonState.maxResourceSize);
            double resourceValue = CommonState.r.nextGaussian();
            if (Math.abs(resourceValue) > MyCommonState.maxResourceValue)
                resourceValue = MyCommonState.maxResourceValue;
            if (resourceValue > 0) resource.setResourceValue(resourceValue);
            else resource.setResourceValue(-resourceValue);
            resourceDb.add(resource);
        }
    }

    /**
     * 从所有的资源中（resourceDb）随机取出一定数量的资源分配给节点
     * @param resourceNum 资源的数量
     * @return 随机取出的资源列表
     */
    public static List<Resource> generateListForNode(int resourceNum){
        return CommonUtil.randomPickFromArray(resourceDb, resourceNum);
    }
}
