package field.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 信息场对应的实体类
 * Created by Hong on 2017/7/8.
 */
public class Field implements Comparable, Cloneable {

    /** 信息场ID */
    private long fieldID;
    /** 需求场（0） or 资源场（1） */
    private long queryOrSource;
    /** 信息场的类别 */
    private long typeID;
    /** 信息场的总场强 */
    private double fieldStrength;
    /** 子信息场 < 扩散距离，该距离对应的总场强 > */
    public Map<Integer, Double> decayField;

    /**
     * 默认构造函数
     */
    public Field(){
        fieldID = -1;
        queryOrSource = -1;
        typeID = -1;
        fieldStrength = -1;
        decayField = new HashMap<>();
    }

    /**
     * 用于比较不同节点同类型信息场之间的场强大小
     * @param o 比较对象
     * @return 大于返回1，小于返回-1，等于返回0
     */
    @Override
    public int compareTo(Object o) {
        if (o == null) return 1;
        if (this.fieldStrength > ((Field)o).getFieldStrength())
            return 1;
        else if (this.fieldStrength < ((Field)o).getFieldStrength())
            return -1;
        return 0;
    }

    /**
     * 深复制，主要是Map的问题
     * @return 深复制后的信息场
     * （因为一个信息场需要存放在不同的节点上，且值会有所不同，所以需要进行深复制）
     * @throws CloneNotSupportedException
     */
    @Override
    public Field clone() throws CloneNotSupportedException{
        Field field = (Field) super.clone();
        field.decayField = new HashMap<>(this.decayField);
        return field;
    }

    /**
     * 将子信息场的场强叠加，得到该信息场的总场强
     * @return 信息场的总场强
     */
    public double calculateFieldStrength()
    {
        double strength = 0;
        for (Double value: decayField.values()) {
            strength += value;
        }
        return strength;
    }

    public long getFieldID() {
        return fieldID;
    }

    public void setFieldID(long fieldID) {
        this.fieldID = fieldID;
    }

    public long getQueryOrSource() {
        return queryOrSource;
    }

    public void setQueryOrSource(long queryOrSource) {
        this.queryOrSource = queryOrSource;
    }

    public long getTypeID() {
        return typeID;
    }

    public void setTypeID(long typeID) {
        this.typeID = typeID;
    }

    public double getFieldStrength() {
        return fieldStrength;
    }

    public void setFieldStrength(double fieldStrength) {
        this.fieldStrength = fieldStrength;
    }
}
