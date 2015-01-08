package pl.edu.pw.elka.tin.MNC.MNCController;

import pl.edu.pw.elka.tin.MNC.MNCAddress;
import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCConsts;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDatagram;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDeviceParameter;
import pl.edu.pw.elka.tin.MNC.MNCSystemLog;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.TreeMap;

/**
 * Monitor to podstawowa wersja sterownika, która nie jest brana pod uwagę przy tworzeniu pierścienia
 * @author Przemek
 */
public class MNCMonitor extends MNCDevice {
    protected TreeMap<String, Thread> tokenOwnerGetters;

    public MNCMonitor(String name, MNCAddress addr, MNCSystemLog log) throws SocketException, UnknownHostException {
        super(name, addr, log);
        tokenOwnerGetters = new TreeMap<String, Thread>();
    }

    public synchronized void receiveDatagram(MNCDatagram datagram) {
        if(datagram.getSender().equals(getMyAddress()))
            return;
        log.actionReceiveDatagram(datagram);
        if(datagram.getSender().equals(getMyAddress()))
            return;
        switch (datagram.getType()){
            case I_HAVE_TOKEN:
                tokensOwners.put(datagram.getGroup(),datagram.getSender());
                log.actionNewTokenOwner(datagram.getGroup());
                break;
            case WHO_IN_GROUP:
                if(getGroups().contains(datagram.getGroup())){
                    try {
                        sendDatagram(new MNCDatagram(getMyAddress(), MNCConsts.MULTICAST_ADDR, datagram.getGroup(), MNCDatagram.TYPE.IAM_IN_GROUP, null));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case DATA_FRAGMENT:
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
                }
                break;
        }
    }

    public synchronized int receiveUnicastData(MNCDatagram datagram){
        log.acction("odebrano "+datagram.toString());
        switch (datagram.getType()){
        }
        return 0;
    }

    protected void checkTokenOwners(){
        for (String group : myGroups) {
            if(tokensOwners.contains(group) == false){
                    Thread tokenGetter = new Thread(new MNCMonitorTokenGetter(this, group));
                    tokenOwnerGetters.put(group, tokenGetter);
                    tokenGetter.start();
            }
        }
    }
}
