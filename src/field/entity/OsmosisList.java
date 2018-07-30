package field.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Osmosis实体类对应的集合
 * Created by Hong on 2017/8/19.
 */
public class OsmosisList {

    /** Osmosis集合 */
    public List<Osmosis> osmoses;

    public OsmosisList(){
        osmoses = new ArrayList<>();
    }

    /**
     * 查找当前集合中是否有对应类型的Osmosis实体
     * @param type 类型
     * @return 若有对应类型的Osmosis实体类，则返回对应实体，没有则返回null
     */
    public Osmosis findOsmosisForType(long type){
        for (Osmosis osmosis : osmoses) {
            if (osmosis.getTypeID() == type)
                return osmosis;
        }
        return null;
    }

    public void add(Osmosis osmosis){
        osmoses.add(osmosis);
    }

    public void remove(Osmosis osmosis){
        osmoses.remove(osmosis);
    }
}
