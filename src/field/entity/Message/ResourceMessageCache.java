package field.entity.Message;


import java.util.ArrayList;
import java.util.List;

/**
 * 资源消息的缓存
 * Created by Hong on 2017/7/12.
 */
public class ResourceMessageCache {

    /** 资源消息的缓存 */
    public List<ResourceMessage> resourceMessages;

    public ResourceMessageCache(){
        resourceMessages = new ArrayList<>();
    }

    public int size(){
        return resourceMessages.size();
    }

    public void add(ResourceMessage resourceMessage){
        resourceMessages.add(resourceMessage);
    }

    public void remove(ResourceMessage resourceMessage){
        resourceMessages.remove(resourceMessage);
    }

    /**
     * 查看当前缓存中是否已存有该资源消息
     * @param resourceMessage 资源消息
     * @return 若已存在，怎返回该资源消息，否则返回null
     */
    public ResourceMessage containsResourceMessage(ResourceMessage resourceMessage){
        for (ResourceMessage cacheResourceMessage : resourceMessages) {
            if (cacheResourceMessage.getResourceMessageID() == resourceMessage.getResourceMessageID())
                return cacheResourceMessage;
        }
        return null;
    }
}
