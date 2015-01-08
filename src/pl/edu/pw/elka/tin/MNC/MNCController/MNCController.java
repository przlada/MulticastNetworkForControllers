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

import static pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDatagram.TYPE.*;

/**
 * Klasa reprezentująca pełnoprawny kontroller
 * @author Paweł
 */
public class MNCController extends MNCDevice {
    protected TreeMap<String, MNCControllerTokenGetter> tokenOwnerGetters;
    private TreeMap<String, MNCToken> tokens;


    public MNCController(String name, MNCAddress addr, MNCSystemLog log) throws SocketException, UnknownHostException {
        super(name, addr, log);
        tokens = new TreeMap<String, MNCToken>();
        tokenOwnerGetters = new TreeMap<String, MNCControllerTokenGetter>();
    }

    public void addToken(String group){
        MNCToken token = new MNCToken(group);
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
                MNCDatagram iHaveToken = new MNCDatagram(getMyAddress(), MNCConsts.MULTICAST_ADDR, datagram.getGroup(), MNCDatagram.TYPE.I_HAVE_TOKEN, null);
                try {
                    sendDatagram(iHaveToken);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
            else if(tokenOwnerGetters.containsKey(datagram.getGroup())){
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
            if(getGroups().contains(datagram.getGroup())){
                if(!consumedParametersSets.containsKey(datagram.getGroup()) || !consumedParametersSets.get(datagram.getGroup()).contains(((MNCDeviceParameter)datagram.getData()).getParameterSetId())){
                    if(receiveParameter(datagram.getGroup(), (MNCDeviceParameter)datagram.getData())) {
                        if(dataConsumption(datagram.getGroup(), ((MNCDeviceParameter) datagram.getData()).getParameterSetId())){
                            try {
                                sendDatagram(new MNCDatagram(getMyAddress(), MNCConsts.MULTICAST_ADDR, datagram.getGroup(), MNCDatagram.TYPE.CONSUMPTION_CONFIRMATION, ((MNCDeviceParameter)datagram.getData()).getParameterSetId()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                else{
                    log.actionDataAlreadyConsumed(datagram);
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
                    int id = token.getNextDataId();
                    MNCDeviceParameterSet paramSet = (MNCDeviceParameterSet)datagram.getData();
                    paramSet.setParameterSetID(id);
                    token.addParameterSetToTransmit(paramSet, this);
                    return id;
                }
                break;
            case GET_TOKEN:
                if(getGroups().contains(datagram.getGroup())){
                    tokens.put(datagram.getGroup(), (MNCToken)datagram.getData());
                    MNCDatagram iHaveToken = new MNCDatagram(getMyAddress(), MNCConsts.MULTICAST_ADDR, datagram.getGroup(), MNCDatagram.TYPE.I_HAVE_TOKEN, null);
                    try {
                        sendDatagram(iHaveToken);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return 1;
                }
        }
        return 0;
    }


    protected void checkTokenOwners(){
        for (String group : myGroups) {
            if(tokensOwners.contains(group) == false){
                MNCControllerTokenGetter tokenGetter = new MNCControllerTokenGetter(this, group);
                tokenOwnerGetters.put(group, tokenGetter);
                new Thread(tokenGetter).start();
            }
        }
    }

    public synchronized void transferToken(String group){
        MNCToken token = tokens.get(group);
        MNCAddress nextOwner = token.getNextController(getMyAddress());
        if(nextOwner != null) {
            tokens.remove(group);
            token.clearBeforeTransmition();
            MNCDatagram data = new MNCDatagram(getMyAddress(), nextOwner, group, MNCDatagram.TYPE.GET_TOKEN, token);
            sendUnicastDatagram(data);
        }
    }

}
