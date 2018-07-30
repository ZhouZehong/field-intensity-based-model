package field.entity.Message;

/**
 * 消息的实体类
 * Created by Hong on 2017/7/11.
 */
public class Message {

    /** 消息源 */
    private long messageSource;
    /** 消息的类别 */
    private long typeID;
    /** 消息的存活时间 */
    private long lifeTime;
    /** 消息的复制能力上限 */
    private double relayCapacity;
    /** 消息允许被传递的跳数 */
    private int hop;
    /** 消息的具体类型 */
    private String _class;

    public Message(){
        messageSource = -1;
        typeID = -1;
        lifeTime = -1;
        relayCapacity = -1;
        hop = -1;
        this.set_class(Message.class.getName());
    }

    public long getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(long messageSource) {
        this.messageSource = messageSource;
    }

    public long getTypeID() {
        return typeID;
    }

    public void setTypeID(long typeID) {
        this.typeID = typeID;
    }

    public long getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(long lifeTime) {
        this.lifeTime = lifeTime;
    }

    public double getRelayCapacity() {
        return relayCapacity;
    }

    public void setRelayCapacity(double relayCapacity) {
        this.relayCapacity = relayCapacity;
    }

    public int getHop() {
        return hop;
    }

    public void setHop(int hop) {
        this.hop = hop;
    }

    public String get_class() {
        return _class;
    }

    public void set_class(String _class) {
        this._class = _class;
    }
}
