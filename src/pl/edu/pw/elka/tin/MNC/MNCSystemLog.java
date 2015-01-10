package pl.edu.pw.elka.tin.MNC;

import pl.edu.pw.elka.tin.MNC.MNCController.MNCDevice;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDatagram;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDeviceParameterSet;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCToken;

import java.util.Date;

import static pl.edu.pw.elka.tin.MNC.MNCConstants.MNCDict.Langs;
import static pl.edu.pw.elka.tin.MNC.MNCConstants.MNCDict.getLangText;

/**
 * Klasa odpowiedzialna za odbieranie i wyświetlanie wszelkich zdarzeń.
 * @author Paweł
 */
public class MNCSystemLog {
    private Langs lang;
    private String controllerName = null;
    private MNCDevice device;

    public MNCSystemLog(Langs l){
        setLang(l);
    }

    public void setDevice(MNCDevice dev){
        device = dev;
        if(dev != null)
            controllerName = device.getName();
        else
            controllerName = getLangText(lang, "NoNameController");
        print(getLangText(lang,"ControllerStarted") + controllerName);
    }

    public synchronized Langs getLang(){
        return lang;
    }

    public synchronized void setLang(Langs l){
        lang = l;
    }

    private void print(String text){
        Date date= new Date();
        //System.out.println(new Timestamp(date.getTime()) + " " + controllerName+ " " + text);
        System.out.println(controllerName+ " " + text);
    }

    public void acction(String type){
        print(type);
    }

    public void actionNewTokenOwner(String group){
        print(getLangText(lang,"HaveNewTokenOwner")+group+" "+device.getTokensOwners().get(group));
    }
    public void actionReceiveUnicastDatagram(MNCDatagram datagram){
        print(getLangText(lang,"ReceiveFromUnicast")+datagram);
    }

    public void actionSendUnicastDatagram(MNCDatagram datagram){
        print(getLangText(lang,"SendByUnicast")+datagram);
    }

    public void actionReceiveDatagram(MNCDatagram datagram){
        print(getLangText(lang,"ReceiveFromMulticast")+datagram);
    }

    public void actionAddedNewDevice(String group, MNCAddress address){
        print(getLangText(lang,"AddedNewDeviceToGroup")+group+" "+address);
    }

    public void actionDataAlreadyConsumed(MNCDatagram data){
        print(getLangText(lang,"DataAlreadyConsumed")+data);
    }

    public void actionDataReBroadcast(MNCDatagram data){
        print(getLangText(lang,"DataReBroadcast")+data);
    }

    public void actionReceivedToken(MNCToken token){
        print(getLangText(lang,"ReceivedToken")+token);
    }

    public void actionSendDatagram(MNCDatagram datagram){
        print(getLangText(lang,"SendByMulticast")+datagram);
    }

    public void dataConsumption(MNCDeviceParameterSet set){
        print(getLangText(lang,"DataConsumption")+set.getGroup()+" "+set.getParameterSetID());
    }

    public void actionSentDataBroadcastConfirm(){
        print(getLangText(lang,"SentDataBroadcastConfirm"));
    }

    public void actionTokenOutOfReach(MNCAddress address){
        print(getLangText(lang,"TokenOutOfReach")+address);
    }

    public void actionTokenOwnerAssignment(){
        print(getLangText(lang,"TokenOwnerAssignment"));
    }

    public void actionTokenTransfered(MNCAddress address){
        print(getLangText(lang,"TokenTransfered")+address);
    }

    public void actionTokenTransferError(MNCAddress address){
        print(getLangText(lang,"TokenTransferError")+address);
    }

    public void actionRemoveDeviceFromTokenList(MNCAddress address){
        print(getLangText(lang,"RemoveDeviceFromTokenList")+address);
    }
}
