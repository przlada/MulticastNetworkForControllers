package pl.edu.pw.elka.tin.MNC.MNCController;

import pl.edu.pw.elka.tin.MNC.MNCAddress;
import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCConsts;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDatagram;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDeviceParameter;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDeviceParameterSet;
import pl.edu.pw.elka.tin.MNC.MNCSystemLog;

import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/**
 * Abstrakcyjna klasa reprezentująca sterownik
 * @author Maciek
 */
public abstract class MNCDevice implements Serializable{
    protected MNCSystemLog log;
    protected Hashtable<String, MNCAddress> tokensOwners;
    protected Set<String> myGroups;
    protected Hashtable<String, Hashtable<Integer, Hashtable<Integer, MNCDeviceParameter>>> receivedParameters;
    protected Hashtable<String, HashSet<Integer>> consumedParametersSets;
    private String name;
    private MNCAddress myAddress;
    private DatagramSocket udpClient;
    private Thread mcastReceiver;
    private Thread unicastReceiver;

    public MNCDevice(String name, MNCAddress addr, MNCSystemLog log) throws SocketException {
        this.name = name;
        this.log = log;
        myAddress = addr;
        log.setDevice(this);
        tokensOwners = new Hashtable<String, MNCAddress>();
        myGroups = new HashSet<String>();
        udpClient = new DatagramSocket();
        receivedParameters = new Hashtable<String, Hashtable<Integer, Hashtable<Integer, MNCDeviceParameter>>>();
        consumedParametersSets = new Hashtable<String, HashSet<Integer>>();
        try {
            mcastReceiver = new Thread(new MNCMulticastReceiver(this));
            unicastReceiver = new Thread(new MNCUnicastReceiver(this));
            mcastReceiver.start();
            unicastReceiver.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public MNCAddress getMyAddress() {
        return myAddress;
    }

    public void addGroup(String group){
        myGroups.add(group);
        checkTokenOwners();
    }

    public Hashtable<String, MNCAddress> getTokensOwners(){
        return tokensOwners;
    }

    public Set<String> getGroups(){
        return myGroups;
    }

    public void removeGroup(String group){
        myGroups.remove(group);
    }

    public abstract void receiveDatagram(MNCDatagram datagram);

    public abstract int receiveUnicastData(MNCDatagram datagram);

    public synchronized void sendDatagram(MNCDatagram d) throws IOException {
        byte data[] = MNCDatagram.toByteArray(d);
        DatagramPacket packet = new DatagramPacket(data, data.length, MNCConsts.MULTICAST_ADDR.getJavaAddress(), MNCConsts.MCAST_PORT);
        udpClient.send(packet);
        log.actionSendDatagram(d);
    }

    public int sendUnicastDatagram(MNCDatagram d){
        try{
            Socket socket = new Socket(d.getReceiver().getJavaAddress(), MNCConsts.UCAST_PORT);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in  = new ObjectInputStream(socket.getInputStream());
            out.writeObject(d);
            log.acction("wyslano unicast: "+d);
            int id = (Integer) in.readObject();
            log.acction("odebrano id: "+id);
            socket.close();
            return id;
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: kq6py");
        } catch  (IOException e) {
            System.out.println("No I/O");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void sendParameterSet(String group, MNCDeviceParameterSet set){
        MNCDatagram data = new MNCDatagram(myAddress,getTokensOwners().get(group),group, MNCDatagram.TYPE.DATA_FULL,set);
        int id = sendUnicastDatagram(data);
        set.setParameterSetID(id);
    }

    public boolean receiveParameter(String group, MNCDeviceParameter param){
        if(!receivedParameters.containsKey(group)){
            receivedParameters.put(group, new Hashtable<Integer, Hashtable<Integer, MNCDeviceParameter>>());
        }
        if(!receivedParameters.get(group).containsKey(param.getParameterSetId())){
            receivedParameters.get(group).put(param.getParameterSetId(), new Hashtable<Integer, MNCDeviceParameter>());
        }
        receivedParameters.get(group).get(param.getParameterSetId()).put(param.getIndex(),param);
        if(receivedParameters.get(group).get(param.getParameterSetId()).size() >= MNCConsts.PARAMETER_SET_SIZE) {
            return true;
        }
        return false;
    }

    public boolean dataConsumption(String group, int paramSetId){
        Hashtable<Integer, Hashtable<Integer, MNCDeviceParameter>> set = receivedParameters.get(group);
        if(set != null){
            if(!consumedParametersSets.containsKey(group))
                consumedParametersSets.put(group, new HashSet<Integer>());
            consumedParametersSets.get(group).add(paramSetId);
            MNCDeviceParameterSet parameterSet = new MNCDeviceParameterSet(set.remove(paramSetId));
            log.dataConsumption(parameterSet);
            return true;
        }
        return false;
    }
    protected abstract void checkTokenOwners();
}
