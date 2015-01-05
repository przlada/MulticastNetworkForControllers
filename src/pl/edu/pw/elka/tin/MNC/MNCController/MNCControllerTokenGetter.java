package pl.edu.pw.elka.tin.MNC.MNCController;

import pl.edu.pw.elka.tin.MNC.MNCAddress;
import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCConsts;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDatagram;

import java.io.IOException;
import java.util.Random;

/**
 * Wątek odpowiedzialny za negocjację podczas inicjalizacji tokena
 * @author Przemek
 */
public class MNCControllerTokenGetter implements Runnable {
    private MNCController parentController;
    private String group;
    private MNCAddress highestPrior;
    private Boolean found;
    private Random rand;

    public MNCControllerTokenGetter(MNCController controller, String group){
        parentController = controller;
        this.group = group;
        highestPrior = parentController.getMyAddress();
        found = false;
        rand = new Random(parentController.getMyAddress().hashCode());
    }
    public synchronized void foundToken(){
        found = true;
    }

    public synchronized void foundTmpToken(MNCAddress prior){
        if(highestPrior.compareTo(prior) < 0)
            highestPrior = prior;
    }

    @Override
    public void run() {
        MNCDatagram sendDatagram = new MNCDatagram(parentController.getMyAddress(), MNCConsts.MULTICAST_ADDR, group, MNCDatagram.TYPE.IS_THERE_TOKEN, null);
        try {
            parentController.sendDatagram(sendDatagram);
            Thread.sleep(MNCConsts.WAIT_FOR_TOKEN_TIMEOUT);
            if(!found){
                sendDatagram = new MNCDatagram(parentController.getMyAddress(), MNCConsts.MULTICAST_ADDR, group, MNCDatagram.TYPE.I_HAVE_TMP_TOKEN, null);
                Thread.sleep(rand.nextInt(50));
                parentController.sendDatagram(sendDatagram);
                Thread.sleep(MNCConsts.WAIT_FOR_TMP_TOKEN);
                if(highestPrior.equals(parentController.getMyAddress())){
                    parentController.addToken(group);
                    sendDatagram = new MNCDatagram(parentController.getMyAddress(), MNCConsts.MULTICAST_ADDR, group, MNCDatagram.TYPE.WHO_IN_GROUP, null);
                    parentController.sendDatagram(sendDatagram);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            parentController.tokenOwnerGetters.remove(group);
        }
    }
}
