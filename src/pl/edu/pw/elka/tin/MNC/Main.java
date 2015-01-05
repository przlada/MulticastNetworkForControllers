package pl.edu.pw.elka.tin.MNC;

import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCConsts;
import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCDict;
import pl.edu.pw.elka.tin.MNC.MNCController.MNCController;
import pl.edu.pw.elka.tin.MNC.MNCController.MNCMonitor;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDatagram;



public class Main {

    public static void main(String[] args) {
        MNCDict.Langs lang = MNCDict.Langs.PL;
        try {
            MNCController controller1 = new MNCController("controller1",new MNCAddress("1", MNCAddress.TYPE.CONTROLLER),new MNCSystemLog(lang,"controller1"));
            MNCController controller2 = new MNCController("controller2",new MNCAddress("2", MNCAddress.TYPE.CONTROLLER),new MNCSystemLog(lang,"controller2"));
            controller1.addGroup("grupa");
            controller2.addGroup("grupa");
            //controller.addToken("grupa");

            MNCMonitor monitor1 = new MNCMonitor("monitor1", new MNCAddress("3", MNCAddress.TYPE.MONITOR),new MNCSystemLog(lang,"monitor1"));
          //  MNCMonitor monitor2 = new MNCMonitor("monitor2", new MNCAddress("3", MNCAddress.TYPE.MONITOR),new MNCSystemLog(lang,"monitor2"));

            monitor1.addGroup("grupa");
           // monitor2.addGroup("grupa");

            //monitor1.sendDatagram(new MNCDatagram(monitor1.getMyAddress(), MNCConsts.MULTICAST_ADDR, "grupa", MNCDatagram.TYPE.IAM_IN_GROUP, null));
            //monitor2.sendDatagram(new MNCDatagram(monitor2.getMyAddress(), MNCConsts.MULTICAST_ADDR, "grupa", MNCDatagram.TYPE.IAM_IN_GROUP, null));

            Thread.sleep(15000);

            System.out.println(controller2.getToken("grupa").toString());

        } catch (java.io.IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
