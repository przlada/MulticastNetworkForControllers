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
    private Boolean someoneSendTmp;
    private Boolean secondPhase;
    private MNCDatagram isThereToken;
    private MNCDatagram iHaveTmp;
    private MNCDatagram iHaveToken;
    private MNCDatagram whoInGroup;


    public MNCControllerTokenGetter(MNCController controller, String group){
        parentController = controller;
        this.group = group;
        highestPrior = parentController.getMyAddress();
        found = false;
        rand = new Random(parentController.getMyAddress().hashCode());
        someoneSendTmp = false;
        secondPhase = false;
        isThereToken = new MNCDatagram(parentController.getMyAddress(), MNCConsts.MULTICAST_ADDR, group, MNCDatagram.TYPE.IS_THERE_TOKEN, null);
        iHaveTmp = new MNCDatagram(parentController.getMyAddress(), MNCConsts.MULTICAST_ADDR, group, MNCDatagram.TYPE.I_HAVE_TMP_TOKEN, null);
        iHaveToken = new MNCDatagram(parentController.getMyAddress(), MNCConsts.MULTICAST_ADDR, group, MNCDatagram.TYPE.I_HAVE_TOKEN, null);
        whoInGroup = new MNCDatagram(parentController.getMyAddress(), MNCConsts.MULTICAST_ADDR, group, MNCDatagram.TYPE.WHO_IN_GROUP, null);
    }
    public synchronized void foundToken(){
        found = true;
    }

    public synchronized void foundTmpToken(MNCAddress prior){
        someoneSendTmp = true;
        if (highestPrior.compareTo(prior) < 0)
            highestPrior = prior;
    }

    public synchronized void askIsThereToken(){
        if(secondPhase)
            try {
                parentController.sendDatagram(iHaveTmp);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void run() {
        try {
            while(true){
                parentController.sendDatagram(isThereToken);
                Thread.sleep(MNCConsts.WAIT_FOR_TOKEN_TIMEOUT);
                if(found)
                    break;
                if(someoneSendTmp){
                    Thread.sleep(MNCConsts.WAIT_FOR_TMP_TOKEN);
                    if(found) break;
                }
                else{
                    secondPhase = true;
                    parentController.sendDatagram(iHaveTmp);
                    Thread.sleep(MNCConsts.WAIT_FOR_TMP_TOKEN);
                    if(highestPrior.equals(parentController.getMyAddress())){
                        parentController.addToken(group);
                        parentController.sendDatagram(iHaveToken);
                        parentController.sendDatagram(whoInGroup);
                        break;
                    }
                    Thread.sleep(50);
                    if(found) break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally{
            parentController.tokenOwnerGetters.remove(group);
        }

        /*
        MNCDatagram isThereToken = new MNCDatagram(parentController.getMyAddress(), MNCConsts.MULTICAST_ADDR, group, MNCDatagram.TYPE.IS_THERE_TOKEN, null);
        try {
            parentController.sendDatagram(isThereToken);
            Thread.sleep(MNCConsts.WAIT_FOR_TOKEN_TIMEOUT);
            if(!found){
                if(!someoneSendTmp) {
                    MNCDatagram iHaveTmp = new MNCDatagram(parentController.getMyAddress(), MNCConsts.MULTICAST_ADDR, group, MNCDatagram.TYPE.I_HAVE_TMP_TOKEN, null);
                    Thread.sleep(rand.nextInt(50));
                    parentController.sendDatagram(iHaveTmp);
                    Thread.sleep(MNCConsts.WAIT_FOR_TMP_TOKEN);
                    if(!found)
                    parentController.sendDatagram(isThereToken);
                }

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
        */
    }
}
