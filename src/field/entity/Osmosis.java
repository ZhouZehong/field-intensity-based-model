package field.entity;

/**
 * Osmosis机制下必须的实体类，用来记录溶质与溶剂的体积
 * Created by Hong on 2017/8/19.
 */
public class Osmosis {

    /** Osmosis ID */
    private long osmosisID;
    private long typeID;
    /** 溶质体积 */
    private double solute;
    /** 溶剂体积 */
    private double solvent;
    /** 浓度 */
    private double concentration;

    /**
     * 默认构造函数
     */
    public Osmosis(){
        osmosisID = -1;
        typeID = -1;
        solute = -1;
        solvent = -1;
        concentration = -1;
    }

    /**
     * 计算当前浓度大小
     * @return 当前浓度大小
     */
    public double calculateCon(){
        return solute / solvent;
    }

    public long getOsmosisID() {
        return osmosisID;
    }

    public void setOsmosisID(long osmosisID) {
        this.osmosisID = osmosisID;
    }

    public long getTypeID() {
        return typeID;
    }

    public void setTypeID(long typeID) {
        this.typeID = typeID;
    }

    public double getSolute() {
        return solute;
    }

    public void setSolute(double solute) {
        this.solute = solute;
    }

    public double getSolvent() {
        return solvent;
    }

    public void setSolvent(double solvent) {
        this.solvent = solvent;
    }

    public double getConcentration() {
        return concentration;
    }

    public void setConcentration(double concentration) {
        this.concentration = concentration;
    }
}
