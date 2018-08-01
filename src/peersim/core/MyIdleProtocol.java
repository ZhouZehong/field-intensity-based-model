package peersim.core;

import peersim.config.Configuration;

/**
 * Created by Hong on 2017/6/26.
 * PeerSim中的Idle是一个实现了Linkable接口的协议类
 * 其负责的是一个静态拓扑结构的实现，为其他协议提供邻居信息，但其中没有实现删除邻居节点的功能
 * 为此重写一份实现了MyLinkable的MyIdleProtocol协议类取而代之
 */
public class MyIdleProtocol implements Protocol, MyLinkable {

    /**
     * 默认的邻居节点上限
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 10;

    /**
     * 配置文件里的参数，代表了邻居节点个数的上限
     */
    private static final String PAR_INITCAP = "capacity";

    /** 邻居节点集合 */
    protected Node[] neighbors;

    /** 邻居节点的个数 */
    protected int len;

    public MyIdleProtocol(String s){
        neighbors = new Node[Configuration.getInt(s + "." + PAR_INITCAP,
                DEFAULT_INITIAL_CAPACITY)];
        len = 0;
    }

    @Override
    public int degree() {
        return len;
    }

    @Override
    public Node getNeighbor(int i) {
        return neighbors[i];
    }

    @Override
    public boolean addNeighbor(Node n) {
        if (contains(n)){
            return false;
        }
        if (len == neighbors.length){
            Node[] temp = new Node[3 * neighbors.length / 2];
            System.arraycopy(neighbors, 0, temp, 0, neighbors.length);
            neighbors = temp;
        }
        neighbors[len] = n;
        len++;
        return true;
    }

    @Override
    public boolean removeNeighbor(Node n) {
        for (int i = 0; i < len; i++){
            if (neighbors[i] == n){
                for (int j = i; j < len - 1; j++){
                    neighbors[j] = neighbors[j + 1];
                }
                neighbors[len - 1] = null;
                len--;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(Node n) {
        for (int i = 0; i < len; i++){
            if (neighbors[i] == n){
                return true;
            }
        }
        return false;
    }

    @Override
    public void pack() {
        if (len == neighbors.length){
            return;
        }
        Node[] temp = new Node[len];
        System.arraycopy(neighbors, 0, temp, 0, len);
        neighbors = temp;
    }

    @Override
    public void onKill() {
        neighbors = null;
        len = 0;
    }

    @Override
    public Object clone() {
        MyIdleProtocol mip = null;
        try{
            mip = (MyIdleProtocol) super.clone();
        }
        catch (CloneNotSupportedException e){}
        mip.neighbors = new Node[neighbors.length];
        System.arraycopy(neighbors, 0, mip.neighbors, 0, len);
        mip.len = len;
        return mip;
    }

    public String toString(){
        if (neighbors == null) return "DEAD!";
        StringBuffer buffer = new StringBuffer();
        buffer.append("len=" + len + " maxlen=" + neighbors.length + " [");
        for (int i = 0; i < len; i++){
            buffer.append(neighbors[i].getIndex() + " ");
        }
        return buffer.append("]").toString();
    }

}
