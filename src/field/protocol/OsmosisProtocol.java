package field.protocol;

import field.entity.OsmosisList;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.vector.SingleValueHolder;

/**
 * Osmosis信息的容器，不具备行为元素
 * Created by Hong on 2017/8/19.
 */
public class OsmosisProtocol extends SingleValueHolder implements EDProtocol {

    /** Osmosis集合 */
    public OsmosisList osmosisList;

    public OsmosisProtocol(String prefix){
        super(prefix);
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {

    }
}
