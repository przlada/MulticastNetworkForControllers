package pl.edu.pw.elka.tin.MNC;

import pl.edu.pw.elka.tin.MNC.MNCController.MNCDevice;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDatagram;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDeviceParameterSet;

import java.sql.Timestamp;
import java.util.Date;

import static pl.edu.pw.elka.tin.MNC.MNCConstants.MNCDict.*;

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

    public void actionReceiveDatagram(MNCDatagram datagram){
        print(getLangText(lang,"ReceiveFromMulticast")+datagram);
    }
    public void actionSendDatagram(MNCDatagram datagram){
        print(getLangText(lang,"SendByMulticast")+datagram);
    }

    public void dataConsumption(MNCDeviceParameterSet set){
        print(getLangText(lang,"DataConsumption")+set.getParameterSetID());
    }
}
