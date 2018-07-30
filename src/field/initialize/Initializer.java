package field.initialize;

import field.protocol.InformationProtocol;
import field.util.MyCommonState;
import field.entity.FieldList;
import field.entity.Message.QueryCache;
import field.entity.Message.ResourceMessageCache;
import field.entity.ResourceList;
import field.entity.UserInterestList;
import field.support.ResourceDb;
import field.support.UserInterestDb;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

import java.util.HashMap;

/**
 * 初始化类（节点信息的初始化）
 * Created by Hong on 2017/7/13.
 */
public class Initializer implements Control {

    /** 信息协议 */
    private static final String PAR_IP_PROT = "information_protocol";
    /** 资源总量 */
    private static final String PAR_RESOURCE_NUM ="resource_num";
    /** 资源大小上限 */
    private static final String PAR_MAX_RESOURCE_SIZE = "max_resource_size";
    /** 资源评价值上限 */
    private static final String PAR_MAX_RESOURCE_VALUE = "max_resource_value";
    /** 每个节点上的资源数量上限 */
    private static final String PAR_MAX_RESOURCE_PER_NODE = "max_resource_per_node";
    /** 用户兴趣总量 */
    private static final String PAR_INTEREST_NUM = "interest_num";
    /** 用户兴趣程度值上限 */
    private static final String PAR_MAX_USER_INTEREST_DEGREE = "max_interest_degree";
    /** 每个节点上的用户兴趣上限 */
    private static final String PAR_MAX_INTEREST_PER_NODE = "max_interest_per_node";
    /** 类别总数 */
    private static final String PAR_TYPE_NUM = "type_num";
    /** 判断是否为真实轨迹实验 */
    private static final String PAR_TRACE = "trace";
    /** 统一带宽上限，若为真实轨迹实验，则代表单位时间内的带宽上限，若为-1，则说明带宽无限制 */
    private static final String PAR_UNIFORM_BANDWIDTH = "uniform_bandwidth";

    private final int ipPid;
    private final int resourceNum;
    public static int maxResourcePerNode;
    private final int interestNum;
    public static int maxInterestPerNode;

    public Initializer(String prefix){
        ipPid = Configuration.getPid(prefix + "." + PAR_IP_PROT);
        resourceNum = Configuration.getInt(prefix + "." + PAR_RESOURCE_NUM);
        MyCommonState.maxResourceSize = Configuration.getDouble(prefix + "." + PAR_MAX_RESOURCE_SIZE);
        MyCommonState.maxResourceValue = Configuration.getDouble(prefix + "." + PAR_MAX_RESOURCE_VALUE);
        maxResourcePerNode = Configuration.getInt(prefix + "." + PAR_MAX_RESOURCE_PER_NODE);
        interestNum = Configuration.getInt(prefix + "." + PAR_INTEREST_NUM);
        MyCommonState.maxUserInterestDegree = Configuration.getInt(prefix + "." + PAR_MAX_USER_INTEREST_DEGREE);
        maxInterestPerNode = Configuration.getInt(prefix + "." + PAR_MAX_INTEREST_PER_NODE);
        MyCommonState.typeNum = Configuration.getInt(prefix + "." + PAR_TYPE_NUM);
        MyCommonState.whetherTrace = Configuration.getBoolean(prefix + "." + PAR_TRACE);
        MyCommonState.uniformBandwidth = Configuration.getLong(prefix + "." + PAR_UNIFORM_BANDWIDTH);
    }

    @Override
    public boolean execute() {

        // 资源初始化
        ResourceDb.init(resourceNum);
        // 用户兴趣初始化
        UserInterestDb.init(interestNum);
        // 遍历整个网络，对节点信息（InformationProtocol）进行初始化
        for (int i = 0; i < Network.size(); i++) {
            Node node = Network.get(i);
            // 获取信息协议（InformationProtocol）
            InformationProtocol informationProtocol = (InformationProtocol)
                    node.getProtocol(ipPid);
            // 初始化每个节点信息协议上的信息
            informationProtocol.fieldList = new FieldList();
            informationProtocol.resourceList = new ResourceList();
            int resourcePerNode = CommonState.r.nextInt(maxResourcePerNode + 1);
            informationProtocol.resourceList.setResources(
                    ResourceDb.generateListForNode(resourcePerNode)
            );
            informationProtocol.userInterestList = new UserInterestList();
            int interestPerNode = CommonState.r.nextInt(maxInterestPerNode + 1);
            informationProtocol.userInterestList.setUserInterests(
                    UserInterestDb.generateListForNode(interestPerNode)
            );
            informationProtocol.queryCache = new QueryCache();
            informationProtocol.resourceMessageCache = new ResourceMessageCache();
//            informationProtocol.nodeCache = new HashMap<>();
            if (MyCommonState.whetherTrace) informationProtocol.meetingTime = new HashMap<>();
        }
        return false;
    }
}
