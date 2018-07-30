package field.entity.Message;

import field.entity.Field;

import java.util.List;

/**
 * 信息场扩散消息
 * Created by Hong on 2017/7/17.
 */
public class FieldSpreadMessage extends Message implements Cloneable {

    /** 消息中携带的信息场 */
    private Field field;
    /** 扩散消息的扩散路径 */
    public List<Long> spreadPath;

    public FieldSpreadMessage(){
        this.set_class(FieldSpreadMessage.class.getName());
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }
    public Object clone() throws CloneNotSupportedException{
        return super.clone();
    }
}
