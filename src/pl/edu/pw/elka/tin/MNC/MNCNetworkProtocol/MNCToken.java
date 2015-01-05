package pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol;

import pl.edu.pw.elka.tin.MNC.MNCAddress;
import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCConsts;

import java.io.Serializable;
import java.util.TreeSet;

/**
 * Klasa reprezentująca token danej grupy
 * @author Przemek
 */
public class MNCToken implements Serializable {
    private final String group;
    private TreeSet<MNCAddress> devicesInGroup;
    private int lastDataId;
    private int broadcastCounter;

    public MNCToken(String group){
        this.group = group;
        devicesInGroup = new TreeSet<MNCAddress>();
        lastDataId = 0;
        broadcastCounter = 0;
    }

    public void addDevice(MNCAddress address){
        devicesInGroup.add(address);
    }
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
}
