package field.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户兴趣的集合
 * Created by Hong on 2017/7/12.
 */
public class UserInterestList {

    /** 用户兴趣的集合 */
    private List<UserInterest> userInterests;

    public UserInterestList(){
        userInterests = new ArrayList<>();
    }

    /**
     * 根据用户兴趣的类型来对兴趣程度值的综合进行计算（核心节点场强）
     * @return 各类型用户兴趣的程度值总和《类型，程度值总和》
     */
    public Map<Long, Double> calTotalDegreeForType(){
        Map<Long, Double> totalDegree = new HashMap<>();
        for (UserInterest userInterest : userInterests) {
            if (!totalDegree.containsKey(userInterest.getTypeID())){
                totalDegree.put(userInterest.getTypeID(), userInterest.getDegree());
            }
            else {
                double totalInterestDegree = totalDegree.get(userInterest.getTypeID());
                totalInterestDegree += userInterest.getDegree(); // 对同种类别的用户兴趣程度进行累加
                totalDegree.put(userInterest.getTypeID(), totalInterestDegree);
            }
        }
        return totalDegree;
    }

    public List<UserInterest> getUserInterests() {
        return userInterests;
    }

    public void setUserInterests(List<UserInterest> userInterests) {
        this.userInterests = userInterests;
    }
}
