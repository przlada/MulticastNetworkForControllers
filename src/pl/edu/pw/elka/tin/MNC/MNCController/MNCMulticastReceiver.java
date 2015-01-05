package pl.edu.pw.elka.tin.MNC.MNCController;

import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCConsts;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDatagram;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Wątek odpowiedzialny za odbieranie komunikatów multicast.
 * @author Paweł
 */
public class MNCMulticastReceiver implements Runnable {
    private MNCDevice myDevice;
    private MulticastSocket udpListener;

    MNCMulticastReceiver(MNCDevice device) throws IOException {
        myDevice = device;
        udpListener = new MulticastSocket(MNCConsts.MCAST_PORT);
        InetAddress group = MNCConsts.MULTICAST_ADDR.getJavaAddress();
        udpListener.joinGroup(group);
    }


    @Override
    public void run() {
        while(true){
            byte[] buf = new byte[MNCConsts.MAX_UDP_PACKET_SIZE];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                udpListener.receive(packet);
                MNCDatagram datagram = MNCDatagram.toMNCDatagram(packet.getData());
                myDevice.receiveDatagram(datagram);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
