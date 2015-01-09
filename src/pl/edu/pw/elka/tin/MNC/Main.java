package pl.edu.pw.elka.tin.MNC;

import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCConsts;
import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCDict;
import pl.edu.pw.elka.tin.MNC.MNCController.MNCController;
import pl.edu.pw.elka.tin.MNC.MNCController.MNCDevice;
import pl.edu.pw.elka.tin.MNC.MNCController.MNCMonitor;
import pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol.MNCDeviceParameterSet;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Scanner;


public class Main {

    public static void main(String[] args) throws SocketException{
        NetworkInterface netint = NetworkInterface.getByName(MNCConsts.DEFAULT_INTERFACE_NAME);
        Enumeration addresses = netint.getInetAddresses();
        InetAddress inetAddress = null;
        while (addresses.hasMoreElements()) {
            InetAddress addr = (InetAddress)addresses.nextElement();
            if(addr instanceof Inet6Address)
                if(!addr.isLinkLocalAddress()){
                    inetAddress = addr;
                    break;
                }
        }
        if(inetAddress == null){
            inetAddress = netint.getInterfaceAddresses().get(0).getAddress();
        }
        System.out.println(inetAddress.getHostAddress());
        MNCAddress myAddress;
        String command;
        Scanner in = new Scanner(System.in);
        MNCDict.Langs lang = MNCDict.Langs.PL;
        try {
            if(args.length >= 2) {
                MNCDevice device;
                if (args[1].equals("C")) {
                    myAddress =  new MNCAddress(inetAddress.getHostAddress(), MNCAddress.TYPE.CONTROLLER);
                    device = new MNCController(args[0], myAddress, new MNCSystemLog(lang));
                }
                else {
                    myAddress =  new MNCAddress(inetAddress.getHostAddress(), MNCAddress.TYPE.MONITOR);
                    device = new MNCMonitor(args[0], myAddress, new MNCSystemLog(lang));
                }
                for (int i = 2; i < args.length; i++)
                    device.addGroup(args[i]);
                while(true){
                    command = in.nextLine();
                    if(command.equals("token")){
                        command = in.nextLine();
                        System.out.println(((MNCController) device).getToken(command));
                    }
                    else if(command.equals("tcp")){
                        command = in.nextLine();
                        MNCDeviceParameterSet paramSet = new MNCDeviceParameterSet(command);
                        paramSet.populateSet();
                        ((MNCController) device).sendParameterSet(paramSet);
                    }
                    else if(command.equals("transfer")){
                        command = in.nextLine();
                        ((MNCController) device).transferToken(command);
                    }
                }
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        in.close();
    }
}
