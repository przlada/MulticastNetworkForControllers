package pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol;

import pl.edu.pw.elka.tin.MNC.MNCAddress;
import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCConsts;
import pl.edu.pw.elka.tin.MNC.MNCController.MNCController;

import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.TreeSet;

/**
 * Klasa reprezentująca token danej grupy
 * @author Przemek
 */
public class MNCToken implements Serializable {
    private final String group;
    private TreeSet<MNCAddress> devicesInGroup;
    private Hashtable<Integer, TokenRetransmitionBuffer> retransmitionBuffer;
    private int lastDataId;
    private int broadcastCounter;

    public MNCToken(String group){
        this.group = group;
        devicesInGroup = new TreeSet<MNCAddress>();
        lastDataId = 0;
        broadcastCounter = 0;
        retransmitionBuffer = new Hashtable<Integer, TokenRetransmitionBuffer>();
    }

    public MNCToken(String group, int dataId){
        this(group);
        lastDataId = dataId;
    }

    public void clearBeforeTransmition(){
        retransmitionBuffer.clear();
        broadcastCounter = 0;
    }

    public synchronized void addDevice(MNCAddress address){
        devicesInGroup.add(address);
    }
    public synchronized void removeDevice(MNCAddress address) { devicesInGroup.remove(address) ;}

    public MNCAddress getNextController(MNCAddress controller){
        MNCAddress next = devicesInGroup.higher(controller);
        while(next != controller){
            if(next == null)
                next = devicesInGroup.first();
            if(next.getType() == MNCAddress.TYPE.CONTROLLER)
                    return next;
            next = devicesInGroup.higher(next);
        }
        return null;
    }
    public String toString(){
        String text = "Token:";
        for (MNCAddress i : devicesInGroup)
            text+=i.toString()+ MNCConsts.LOCAL_LINE_SEPARATOR;
        return text;
    }
    public int getNextDataId(){
        lastDataId+=1;
        return lastDataId;
    }
    public Hashtable<Integer, TokenRetransmitionBuffer> getRetransmitionBuffer(){
        return retransmitionBuffer;
    }

    public synchronized void addParameterSetToTransmit(MNCDeviceParameterSet set, MNCController sender){
        TokenRetransmitionBuffer buffer = new TokenRetransmitionBuffer(set, new TreeSet<MNCAddress>(devicesInGroup), sender);
        retransmitionBuffer.put(set.getParameterSetID(), buffer);
        new Thread(buffer).start();
    }

    public synchronized void parameterSetConfirmation(int paramSetId, MNCAddress receiver){
        if(retransmitionBuffer.containsKey(paramSetId)){
            retransmitionBuffer.get(paramSetId).parameterSetConfirmation(receiver);
        }
    }

    private synchronized void removeFromRetransmitionBuffer(int paramId, MNCController controller){
        retransmitionBuffer.remove(paramId);
        if(broadcastCounter >= MNCConsts.MAX_BROADCAST_NUMBER && retransmitionBuffer.isEmpty()){
            controller.transferToken(group);
        }
    }

    private class TokenRetransmitionBuffer implements Runnable {
        private MNCDeviceParameterSet data;
        private TreeSet<MNCAddress> notConfirmed;
        private MNCController mySender;

        public TokenRetransmitionBuffer(MNCDeviceParameterSet set, TreeSet<MNCAddress> devices, MNCController sender){
            notConfirmed = devices;
            data = set;
            mySender = sender;
            notConfirmed.remove(sender.getMyAddress());
        }

        public synchronized void parameterSetConfirmation(MNCAddress receiver){
            if(notConfirmed.contains(receiver))
                notConfirmed.remove(receiver);
        }

        @Override
        public void run() {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(int i=0; i<MNCConsts.MAX_RETRANSMITION_NUMBER; i++){
                for(int j=0; j<MNCConsts.PARAMETER_SET_SIZE; j++){
                    MNCDatagram datagram = new MNCDatagram(mySender.getMyAddress(), MNCConsts.MULTICAST_ADDR, group, MNCDatagram.TYPE.DATA_FRAGMENT, data.getParameters()[j]);
                    try {
                        mySender.sendDatagram(datagram);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(MNCConsts.WAIT_FOR_DATA_CONFIRMATION);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(notConfirmed.size() <= 0)
                    break;
            }
            for(MNCAddress toRemove: notConfirmed){
                removeDevice(toRemove);
                mySender.getLog().actionRemoveDeviceFromTokenList(toRemove);
            }
            broadcastCounter++;
            removeFromRetransmitionBuffer(data.getParameterSetID(), mySender);
        }
    }
}
