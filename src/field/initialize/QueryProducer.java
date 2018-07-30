package field.initialize;

import field.protocol.InformationProtocol;
import field.util.MyCommonState;
import field.entity.Message.Query;
import field.util.JsonUtil;
import field.util.QueryListener;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.transport.Transport;

import java.util.Map;

/**
 * Query发生器（暂且采用一次性将所有query初始化的模式）（query数量不可直接控制）
 * Created by Hong on 2017/7/20.
 */
public class QueryProducer implements Control {

    /** 信息协议 */
    private static final String PAR_IP_PROT = "information_protocol";
    /** message的存活时间 */
    private static final String PAR_MESSAGE_LIFE = "message_life";
    /** 用于控制message允许被复制次数 */
    private static final String PAR_RELAY_TIME = "relay_time";
    /** 产生query的时间跨度 */
    private static final String PAR_QUERY_START_TIME_SPAN = "query_start_time_span";

    private final int ipPid;
    private final long queryStartTimeSpan;

    public QueryProducer(String prefix){
        ipPid = Configuration.getPid(prefix + "." + PAR_IP_PROT);
        MyCommonState.maxMessageLifeTime = Configuration.getLong(prefix + "." + PAR_MESSAGE_LIFE);
        MyCommonState.maxMessageRelayCapacity = Configuration.getDouble(prefix + "." + PAR_RELAY_TIME)
                * FieldConstructor.k * MyCommonState.maxResourceValue * Initializer.maxResourcePerNode;
        queryStartTimeSpan = Configuration.getLong(prefix + "." + PAR_QUERY_START_TIME_SPAN);
    }

    @Override
    public boolean execute() {
        // 遍历网络中的所有节点，根据每个节点上的用户兴趣分配情况产生相应的query
        for (int i = 0; i < Network.size(); i++) {
            Node currentNode = Network.get(i);
            // 获取当前节点的信息协议
            InformationProtocol currentIp =
                    (InformationProtocol)currentNode.getProtocol(ipPid);
            // 通过信息协议获取到用户兴趣集合《兴趣类型，程度值总和》
            Map<Long, Double> interestDegree =
                    currentIp.userInterestList.calTotalDegreeForType();
            // 根据这个兴趣集合计算出的兴趣情况产生对应数量的query并发送消息
            for (Map.Entry<Long, Double> entry : interestDegree.entrySet()) {
                for (int j = 0; j < entry.getValue(); j++) {
                    Query query = new Query();
                    query.setQueryID(MyCommonState.queryCounter);
                    MyCommonState.queryCounter++;

                    // 一个query对应一个listener
                    QueryListener queryListener = new QueryListener();
                    queryListener.setQueryID(query.getQueryID());
                    MyCommonState.addNewListener(query.getQueryID(), queryListener);

                    long queryStartTime = CommonState.r.nextLong(queryStartTimeSpan);
                    query.setQueryStartTime(queryStartTime);
                    // query的源节点是不能修改的
                    query.setMessageSource(i);
                    query.setTypeID(entry.getKey());
                    query.setLifeTime(MyCommonState.maxMessageLifeTime);
                    query.setRelayCapacity(MyCommonState.maxMessageRelayCapacity);
                    query.setHop(0);

                    // 得先将设置好的query放进当前节点的缓存中，因为很多query还没到开始运作的时间
                    try {
                        Query queryForCache = query.clone();
                        currentIp.queryCache.add(queryForCache);
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }

                    // 判断query开始运作的时间是否小于当前时间，如果是，则调整开始时间，并开始传递query消息
                    if (queryStartTime <= CommonState.getTime()){
                        query.setQueryStartTime(CommonState.getTime());
                        // 依然是通过自己给自己发送消息来触发query的传递
                        String json = JsonUtil.toJson(query);
                        ((Transport) currentNode.getProtocol(FastConfig.getTransport(ipPid))).
                                send(currentNode, currentNode, json, ipPid);
                    }
                }
            }
        }
        return false;
    }
}
