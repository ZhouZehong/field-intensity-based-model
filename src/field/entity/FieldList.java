package field.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 信息场集合对应的实体类
 * Created by Hong on 2017/7/9.
 */
public class FieldList {

    /** 信息场集合 */
    public List<Field> fields;

    public FieldList(){
        fields = new ArrayList<>();
    }

    /**
     * 查找当前信息场集合中是否有对应类型的信息场
     * @param queryOrSource 需求场 or 资源场
     * @param type 信息场类型
     * @return 若有对应类型的信息场，则返回信息场，没有则返回null
     */
    public Field findFieldForType(long queryOrSource, long type){
        for (Field field: fields) {
            if ((field.getQueryOrSource() == queryOrSource)
                    && (field.getTypeID() == type))
                return field;
        }
        return null;
    }

    public Field get(int index){
        return fields.get(index);
    }

    public void add(Field field){
        fields.add(field);
    }

    public void remove(int index){
        fields.remove(index);
    }
}
