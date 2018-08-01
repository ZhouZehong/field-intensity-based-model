package peersim.core;

/**
 * Created by Hong on 2017/6/26.
 * 由于PeerSim自带的Linkable接口中不包含removeNeighbor的方法
 * 为了实现动态调整邻居节点，实现移动性的模拟，特此重写一份MyLinkable接口来取代Linkable
 */
public interface MyLinkable extends Cleanable{

    /**
     * @return 邻居节点的个数
     */
    public int degree();

    /**
     * 依据index来对邻居节点进行访问
     * @param i index
     * @return index对应的邻居节点
     */
    public Node getNeighbor(int i);

    /**
     * 添加新的邻居节点
     * @param n 新的邻居节点
     * @return 添加成功返回True，若所添加节点已为邻居节点返回False
     */
    public boolean addNeighbor(Node n);

    /**
     * 删除已有的邻居节点
     * @param n 需要删除的邻居节点
     * @return 删除成功返回True，若需删除的邻居节点不存在返回False
     */
    public boolean removeNeighbor(Node n);

    /**
     * 判断当某一节点是否为当前节点的邻居
     * @param n 需判断的节点
     * @return 所给节点为当前节点的邻居返回True，否则返回False
     */
    public boolean contains(Node n);

    public void pack();
}
