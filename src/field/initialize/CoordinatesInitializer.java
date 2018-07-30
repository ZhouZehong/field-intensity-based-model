package field.initialize;

import field.util.MyCommonState;
import field.protocol.CoordinatesProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

/**
 * Created by Hong on 2017/6/29.
 * 这是一个初始化类（initializer）
 * 随机初始化每个节点的初始位置、速度和运动方向
 */
public class CoordinatesInitializer implements Control{

    /** 对应着一个坐标协议 */
    private static final String PAR_COORDINATES_PORT = "coord_protocol";
    /** 节点移动区域（正方形） */
    private static final String PAR_BORDER = "border";

    private final int coordPid;
    private final double border;

    public CoordinatesInitializer(String prefix){
        coordPid = Configuration.getPid(prefix + "." + PAR_COORDINATES_PORT);
        border = Configuration.getDouble(prefix + "." + PAR_BORDER);
    }

    @Override
    public boolean execute() {
        for (int i = 0; i < Network.size(); i++){
            Node n = Network.get(i);
            CoordinatesProtocol coordinatesProtocol =
                    (CoordinatesProtocol) n.getProtocol(coordPid);
            CommonState.r.setSeed(CommonState.r.nextLong());
            if (CommonState.r.nextBoolean()){
                coordinatesProtocol.setX(CommonState.r.nextDouble() * border);
                coordinatesProtocol.setY(CommonState.r.nextDouble() * border);
            }
            else {
                coordinatesProtocol.setX(CommonState.r.nextDouble() * border * -1);
                coordinatesProtocol.setY(CommonState.r.nextDouble() * border * -1);
            }
            coordinatesProtocol.setV(CommonState.r.nextDouble() * 120);
            coordinatesProtocol.setTheta(CommonState.r.nextDouble() * 360);
            coordinatesProtocol.setAvgV(CommonState.r.nextDouble() * 120);
            coordinatesProtocol.setAvgTheta(CommonState.r.nextDouble() * 360);
        }
        MyCommonState.cycles = 1;
        return true;
    }
}
