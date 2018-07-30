package field.control;

import field.util.MyCommonState;
import peersim.core.Control;

import java.io.FileWriter;
import java.io.IOException;

/**
 * 负责每个周期来打印评价指标到控制台和文件
 */
public class ListenerObserver implements Control{

    public ListenerObserver(String prefix){

    }
    @Override
    public boolean execute() {
        MyCommonState.calculate();
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter("result.txt");
            System.out.println("Total Resource Value: " + MyCommonState.totalResourceValue.getAverage() + " "
                    + MyCommonState.totalResourceValue.getStD());
            fileWriter.write("Total Resource Value: " + MyCommonState.totalResourceValue.getAverage() + " "
                    + MyCommonState.totalResourceValue.getStD() + "\r\n");
            System.out.println("First Hit Time: " + MyCommonState.firstHitTime.getAverage() + ""
                    + MyCommonState.firstHitTime.getStD());
            fileWriter.write("First Hit Time: " + MyCommonState.firstHitTime.getAverage() + ""
                    + MyCommonState.firstHitTime.getStD() + "\r\n");
            System.out.println("Total Bandwidth Consumption: " + MyCommonState.bandwidthConsume);
            fileWriter.write("Total Bandwidth Consumption: " + MyCommonState.bandwidthConsume + "\r\n");
            System.out.println("Success Rate: " +
                    (double)(MyCommonState.queryCounter - MyCommonState.noResultCounter) /  (double)MyCommonState.queryCounter);
            fileWriter.write("No Result Counter: " +
                    (double)(MyCommonState.queryCounter - MyCommonState.noResultCounter) /  (double)MyCommonState.queryCounter + "\r\n");
            fileWriter.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }
}
