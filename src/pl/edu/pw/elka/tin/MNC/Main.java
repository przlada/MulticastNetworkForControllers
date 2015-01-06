package pl.edu.pw.elka.tin.MNC;

import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCConsts;
import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCDict;
import pl.edu.pw.elka.tin.MNC.MNCController.MNCController;
import pl.edu.pw.elka.tin.MNC.MNCController.MNCDevice;
import pl.edu.pw.elka.tin.MNC.MNCController.MNCMonitor;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Scanner;


public class Main {

    public static void main(String[] args) throws SocketException{
        NetworkInterface netint = NetworkInterface.getByName(MNCConsts.DEFAULT_INTERFACE_NAME);
        InetAddress inetAddress = netint.getInterfaceAddresses().get(0).getAddress();
        System.out.println(inetAddress.getHostAddress());
        String command;
        Scanner in = new Scanner(System.in);
        MNCDict.Langs lang = MNCDict.Langs.PL;
        try {
            if(args.length >= 2) {
                MNCDevice device;
                if (args[1].equals("C"))
                    device = new MNCController(args[0], new MNCAddress(inetAddress.getHostAddress(), MNCAddress.TYPE.CONTROLLER), new MNCSystemLog(lang));
                else
                    device = new MNCMonitor(args[0], new MNCAddress(inetAddress.getHostAddress(), MNCAddress.TYPE.MONITOR), new MNCSystemLog(lang));
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
                        device.sendUnicastDatagram(null,device.getTokensOwners().get(command));
                    }
                }
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        in.close();


    }
}
