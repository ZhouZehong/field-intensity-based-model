package field.entity;

/**
 * 用户兴趣对应的实体类
 * Created by Hong on 2017/7/12.
 */
public class UserInterest {

    /** 兴趣ID */
    private long InterestID;
    /** 用户感兴趣的资源类别 */
    private long typeID;
    /** 用户感兴趣的程度 */
    private double degree;

    public UserInterest(){
        InterestID = -1;
        typeID = -1;
        degree = -1;
    }

    public long getInterestID() {
        return InterestID;
    }

    public void setInterestID(long interestID) {
        this.InterestID = interestID;
    }

    public long getTypeID() {
        return typeID;
    }

    public void setTypeID(long typeID) {
        this.typeID = typeID;
    }

    public double getDegree() {
        return degree;
    }

    public void setDegree(double degree) {
        this.degree = degree;
    }
}
