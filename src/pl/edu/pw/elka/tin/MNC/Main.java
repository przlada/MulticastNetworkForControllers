package pl.edu.pw.elka.tin.MNC;

import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCDict;
import pl.edu.pw.elka.tin.MNC.MNCController.MNCController;
import pl.edu.pw.elka.tin.MNC.MNCController.MNCDevice;
import pl.edu.pw.elka.tin.MNC.MNCController.MNCMonitor;

import java.util.Scanner;


public class Main {

    public static void main(String[] args) {
        String command;
        Scanner in = new Scanner(System.in);
        MNCDict.Langs lang = MNCDict.Langs.PL;
        try {
            if(args.length >= 3) {
                MNCDevice device;
                if (args[1].equals("C"))
                    device = new MNCController(args[0], new MNCAddress(args[2], MNCAddress.TYPE.CONTROLLER), new MNCSystemLog(lang, args[0]));
                else
                    device = new MNCMonitor(args[0], new MNCAddress(args[2], MNCAddress.TYPE.MONITOR), new MNCSystemLog(lang, args[0]));
                for (int i = 3; i < args.length; i++)
                    device.addGroup(args[i]);
                while(true){
                    command = in.nextLine();
                    if(command.equals("token")){
                        command = in.nextLine();
                        System.out.println(((MNCController) device).getToken(command));
                    }
                }
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        in.close();


    }
}
