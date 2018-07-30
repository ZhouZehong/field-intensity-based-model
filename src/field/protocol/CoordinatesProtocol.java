package field.protocol;

import peersim.core.Protocol;

/**
 * Created by Hong on 2017/6/29.
 * 这是一个protocol类，负责存储二维坐标值，节点的（实时/平均）移动速度和运动方向
 * 不具备行为元素，只是一个容器
 */
public class CoordinatesProtocol implements Protocol {

    /** 二维坐标 */
    private double x, y;
    /** 实时移动速度 */
    private double v;
    /** 平均速度 */
    private double avgV;
    /** 运动方向（角度） */
    private double theta;
    /** 平均角度 */
    private double avgTheta;
//    /** 由于需要计算平均速度和角度，所以得记录周期数（时间） */
//    public static int cycles;


    public CoordinatesProtocol(String prefix){
        x = y = 0;
    }

    @Override
    public Object clone() {
        CoordinatesProtocol cop = null;
        try {
            cop = (CoordinatesProtocol)super.clone();
        }catch (CloneNotSupportedException e) {
        }
        return cop;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getV() {
        return v;
    }

    public void setV(double v) {
        this.v = v;
    }

    public double getTheta() {
        return theta;
    }

    public void setTheta(double theta) {
        this.theta = theta;
    }

    public double getAvgV() {
        return avgV;
    }

    public void setAvgV(double avgV) {
        this.avgV = avgV;
    }

    public double getAvgTheta() {
        return avgTheta;
    }

    public void setAvgTheta(double avgTheta) {
        this.avgTheta = avgTheta;
    }
}
