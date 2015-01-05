package pl.edu.pw.elka.tin.MNC.MNCController;

import pl.edu.pw.elka.tin.MNC.MNCAddress;
import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCConsts;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDatagram;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCToken;
import pl.edu.pw.elka.tin.MNC.MNCSystemLog;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.TreeMap;

/**
 * Klasa reprezentująca pełnoprawny kontroller
 * @author Paweł
 */
public class MNCController extends MNCDevice {
    private TreeMap<String, MNCToken> tokens;
    protected TreeMap<String, MNCControllerTokenGetter> tokenOwnerGetters;

    public MNCController(String name, MNCAddress addr, MNCSystemLog log) throws SocketException, UnknownHostException {
        super(name, addr, log);
        tokens = new TreeMap<String, MNCToken>();
        tokenOwnerGetters = new TreeMap<String, MNCControllerTokenGetter>();
    }

    public void addToken(String group){
        tokens.put(group,new MNCToken(group));
        tokensOwners.put(group, getMyAddress());
    }
    public MNCToken getToken(String group){
        return tokens.get(group);
    }

    public synchronized void receiveDatagram(MNCDatagram datagram) {
        if(datagram.getSender().equals(getMyAddress()))
            return;
        log.acction("odebrano "+datagram.toString());
        switch (datagram.getType()){
            case IAM_IN_GROUP:
                MNCToken token = tokens.get(datagram.getGroup());
                if(token != null)
                    token.addDevice(datagram.getSender());
                break;
            case IS_THERE_TOKEN:
                if(tokens.containsKey(datagram.getGroup())) {
                    MNCDatagram iHaveToken = new MNCDatagram(getMyAddress(), MNCConsts.MULTICAST_ADDR, datagram.getGroup(), MNCDatagram.TYPE.I_HAVE_TOKEN, null);
                    try {
                        sendDatagram(iHaveToken);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case I_HAVE_TMP_TOKEN:
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
                break;
            case I_HAVE_TOKEN:
                if(tokenOwnerGetters.containsKey(datagram.getGroup())){
                    tokenOwnerGetters.get(datagram.getGroup()).foundToken();
                }
                tokensOwners.put(datagram.getGroup(),datagram.getSender());
                break;
            case WHO_IN_GROUP:
                if(getGroups().contains(datagram.getGroup())){
                    try {
                        sendDatagram(new MNCDatagram(getMyAddress(), MNCConsts.MULTICAST_ADDR, datagram.getGroup(), MNCDatagram.TYPE.IAM_IN_GROUP, null));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }
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

}
