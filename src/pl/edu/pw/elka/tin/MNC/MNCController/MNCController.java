package pl.edu.pw.elka.tin.MNC.MNCController;

import pl.edu.pw.elka.tin.MNC.MNCAddress;
import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCConsts;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDatagram;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDeviceParameter;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDeviceParameterSet;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCToken;
import pl.edu.pw.elka.tin.MNC.MNCSystemLog;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDatagram.TYPE.*;

/**
 * Klasa reprezentująca pełnoprawny kontroller
 * @author Paweł
 */
public class MNCController extends MNCDevice {
    protected TreeMap<String, MNCControllerTokenGetter> tokenOwnerGetters;
    private TreeMap<String, MNCToken> tokens;

    private BlockingQueue<MNCDeviceParameterSet> sendBuffer;
    private SendParameterSetSupervisor sendSupervisor;

    public MNCController(String name, MNCAddress addr, MNCSystemLog log) throws SocketException, UnknownHostException {
        super(name, addr, log);
        tokens = new TreeMap<String, MNCToken>();
        tokenOwnerGetters = new TreeMap<String, MNCControllerTokenGetter>();
        sendBuffer = new LinkedBlockingQueue<MNCDeviceParameterSet>();
        sendSupervisor = new SendParameterSetSupervisor();
        new Thread(sendSupervisor).start();
    }

    public void addToken(String group){
        int counter = 0;
        if(consumedParametersSets.containsKey(group) && !consumedParametersSets.get(group).isEmpty())
            counter = consumedParametersSets.get(group).last();
        MNCToken token = new MNCToken(group, counter);
        token.addDevice(getMyAddress());
        tokens.put(group,token);
        tokensOwners.put(group, getMyAddress());
    }
    public MNCToken getToken(String group){
        return tokens.get(group);
    }

    public synchronized void receiveDatagram(MNCDatagram datagram) {
        if(datagram.getSender().equals(getMyAddress()))
            return;

        log.actionReceiveDatagram(datagram);

        if(datagram.getType() == IAM_IN_GROUP){
            MNCToken token = tokens.get(datagram.getGroup());
            if(token != null) {
                token.addDevice(datagram.getSender());
                log.actionAddedNewDevice(datagram.getGroup(), datagram.getSender());
            }
        }
        else if(datagram.getType() == IS_THERE_TOKEN) {
            MNCToken token = tokens.get(datagram.getGroup());
            if(token != null) {
                token.addDevice(datagram.getSender());
                log.actionAddedNewDevice(datagram.getGroup(),datagram.getSender());
                MNCDatagram iHaveToken = new MNCDatagram(getMyAddress(), MNCConsts.MULTICAST_ADDR, datagram.getGroup(), MNCDatagram.TYPE.I_HAVE_TOKEN, null);
                try {
                    sendDatagram(iHaveToken);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(tokenOwnerGetters.containsKey(datagram.getGroup())){
                tokenOwnerGetters.get(datagram.getGroup()).askIsThereToken();
            }
        }
        else if(datagram.getType() == I_HAVE_TMP_TOKEN) {
            if(tokens.containsKey(datagram.getGroup())){
                MNCDatagram iHaveToken = new MNCDatagram(getMyAddress(), MNCConsts.MULTICAST_ADDR, datagram.getGroup(), MNCDatagram.TYPE.I_HAVE_TOKEN, null);
                try {
                    sendDatagram(iHaveToken);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(tokenOwnerGetters.containsKey(datagram.getGroup())){
                tokenOwnerGetters.get(datagram.getGroup()).foundTmpToken(datagram.getSender());
            }
        }
        else if(datagram.getType() == I_HAVE_TOKEN) {
            if(tokenOwnerGetters.containsKey(datagram.getGroup())){
                tokenOwnerGetters.get(datagram.getGroup()).foundToken();
            }
            tokensOwners.put(datagram.getGroup(),datagram.getSender());
            log.actionNewTokenOwner(datagram.getGroup());
        }
        else if(datagram.getType() == WHO_IN_GROUP) {
            if(getGroups().contains(datagram.getGroup())){
                try {
                    sendDatagram(new MNCDatagram(getMyAddress(), MNCConsts.MULTICAST_ADDR, datagram.getGroup(), MNCDatagram.TYPE.IAM_IN_GROUP, null));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else if(datagram.getType() == DATA_FRAGMENT) {
            MNCDatagram confirmation = new MNCDatagram(getMyAddress(), MNCConsts.MULTICAST_ADDR, datagram.getGroup(), MNCDatagram.TYPE.CONSUMPTION_CONFIRMATION, ((MNCDeviceParameter)datagram.getData()).getParameterSetId());
            if(getGroups().contains(datagram.getGroup())){
                if(!consumedParametersSets.containsKey(datagram.getGroup()) || !consumedParametersSets.get(datagram.getGroup()).contains(((MNCDeviceParameter)datagram.getData()).getParameterSetId())){
                    if(receiveParameter(datagram.getGroup(), (MNCDeviceParameter)datagram.getData())) {
                        sendSupervisor.receivedData(datagram.getGroup(), ((MNCDeviceParameter) datagram.getData()).getParameterSetId());
                        if(dataConsumption(datagram.getGroup(), ((MNCDeviceParameter) datagram.getData()).getParameterSetId())){
                            try {
                                sendDatagram(confirmation);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                else{
                    log.actionDataAlreadyConsumed(datagram);
                    try {
                        sendDatagram(confirmation);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else if(datagram.getType() == CONSUMPTION_CONFIRMATION) {
            MNCToken token = tokens.get(datagram.getGroup());
            if(token != null)
                token.parameterSetConfirmation((Integer) datagram.getData(), datagram.getSender());
        }
    }

    public synchronized int receiveUnicastData(MNCDatagram datagram){
        log.actionReceiveUnicastDatagram(datagram);
        switch (datagram.getType()){
            case DATA_FULL:
                MNCToken token = tokens.get(datagram.getGroup());
                if(token != null) {
                    MNCDeviceParameterSet paramSet = (MNCDeviceParameterSet)datagram.getData();
                    int id = paramSet.getParameterSetID();
                    if(id == 0) {
                        id = token.getNextDataId();
                        paramSet.setParameterSetID(id);
                    }
                    else{
                        log.actionDataReBroadcast(datagram);
                    }
                    token.addParameterSetToTransmit(paramSet, this);
                    return id;
                }
                break;
            case GET_TOKEN:
                if(getGroups().contains(datagram.getGroup())){
                    tokens.put(datagram.getGroup(), (MNCToken)datagram.getData());
                    tokensOwners.put(datagram.getGroup(), getMyAddress());
                    log.actionReceivedToken((MNCToken)datagram.getData());
                    MNCDatagram iHaveToken = new MNCDatagram(getMyAddress(), MNCConsts.MULTICAST_ADDR, datagram.getGroup(), MNCDatagram.TYPE.I_HAVE_TOKEN, null);
                    try {
                        sendDatagram(iHaveToken);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return 1;
                }
        }
        return -1;
    }


    protected synchronized void checkTokenOwners(){
        log.actionTokenOwnerAssignment();
        for (String group : myGroups) {
            if(tokensOwners.contains(group) == false && !tokenOwnerGetters.containsKey(group)){
                MNCControllerTokenGetter tokenGetter = new MNCControllerTokenGetter(this, group);
                tokenOwnerGetters.put(group, tokenGetter);
                new Thread(tokenGetter).start();
            }
        }
    }

    public synchronized void transferToken(String group){
        MNCToken token = tokens.get(group);
        if(token != null) {
            MNCAddress nextOwner = token.getNextController(getMyAddress());
            if (nextOwner != null) {
                tokens.remove(group);
                token.clearBeforeTransmition();
                MNCDatagram data = new MNCDatagram(getMyAddress(), nextOwner, group, MNCDatagram.TYPE.GET_TOKEN, token);
                if(sendUnicastDatagram(data) < 0){
                    tokens.put(group, token);
                    log.actionTokenTransferError(nextOwner);
                }
                else{
                    tokensOwners.put(group, nextOwner);
                    log.actionTokenTransfered(nextOwner);
                }
            }
        }
    }

    public void sendParameterSet(MNCDeviceParameterSet set){
        try {
            sendBuffer.put(set);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class SendParameterSetSupervisor implements Runnable{
        private MNCDeviceParameterSet waitingToConfirm = null;
        private Boolean confirmed = false;

        public synchronized void receivedData(String group, int id){
            if(waitingToConfirm != null && waitingToConfirm.getGroup().equals(group) && waitingToConfirm.getParameterSetID() == id) {
                confirmed = true;
                log.actionSentDataBroadcastConfirm();
            }
        }

        @Override
        public void run() {
            MNCDeviceParameterSet set;
            while(true){
                try {
                    set = sendBuffer.take();
                    while(true) {
                        MNCDatagram data = new MNCDatagram(getMyAddress(), getTokensOwners().get(set.getGroup()), set.getGroup(), MNCDatagram.TYPE.DATA_FULL, set);
                        int id = sendUnicastDatagram(data);
                        while (id <= 0) {
                            log.actionTokenOutOfReach(getTokensOwners().get(set.getGroup()));
                            checkTokenOwners();
                            Thread.sleep(MNCConsts.WAIT_FOR_TOKEN_TIMEOUT + MNCConsts.WAIT_FOR_TMP_TOKEN + MNCConsts.WAIT_FOR_TMP_TOKEN);
                            data = new MNCDatagram(getMyAddress(), getTokensOwners().get(set.getGroup()), set.getGroup(), MNCDatagram.TYPE.DATA_FULL, set);
                            id = sendUnicastDatagram(data);
                        }
                        set.setParameterSetID(id);
                        if(tokens.containsKey(set.getGroup()))
                            break;
                        synchronized (this) {
                            waitingToConfirm = set;
                            confirmed = false;
                        }
                        Thread.sleep(MNCConsts.WAIT_FOR_TOKEN_TO_BROADCAST);
                        synchronized (this) {
                            if (confirmed)
                                break;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
