package field.support;

import field.util.MyCommonState;
import field.entity.UserInterest;
import field.util.CommonUtil;
import peersim.core.CommonState;

import java.util.ArrayList;
import java.util.List;

/**
 * 所有的用户兴趣
 * Created by Hong on 2017/7/13.
 */
public class UserInterestDb {

    private static List<UserInterest> userInterestDb;

    /**
     * 初始化整个网络中的用户兴趣
     * @param userInterestNum 用户兴趣的总量
     */
    public static void init(int userInterestNum){
        userInterestDb = new ArrayList<>();
        for (int i = 0; i < userInterestNum; i++) {
            UserInterest userInterest = new UserInterest();
            userInterest.setInterestID(i);
            userInterest.setTypeID(CommonState.r.nextInt(MyCommonState.typeNum));
            userInterest.setDegree(CommonState.r.nextDouble() * MyCommonState.maxUserInterestDegree);
            userInterestDb.add(userInterest);
        }
    }

    /**
     * 从所有用户兴趣中（userInterestDb）中随机取出一定数量的兴趣分配给用户（节点）
     * @param userInterestNum 兴趣的数量
     * @return 随机取出的兴趣列表
     */
    public static List<UserInterest> generateListForNode(int userInterestNum){
        return CommonUtil.randomPickFromArray(userInterestDb, userInterestNum);
    }
}
