package pl.edu.pw.elka.tin.MNC.MNCController;

import pl.edu.pw.elka.tin.MNC.MNCAddress;
import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCConsts;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDatagram;
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
        log.acction("wyslano "+d.toString());
    }

    public void sendUnicastDatagram(MNCDatagram d){
        try{
            Socket socket = new Socket(d.getReceiver().getJavaAddress(), MNCConsts.UCAST_PORT);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in  = new ObjectInputStream(socket.getInputStream());
            out.writeObject(d);
            log.acction("wyslano unicast: "+d);
            int id = (Integer) in.readObject();
            log.acction("nadano id: "+id);
            socket.close();
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: kq6py");
        } catch  (IOException e) {
            System.out.println("No I/O");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    protected abstract void checkTokenOwners();
}
