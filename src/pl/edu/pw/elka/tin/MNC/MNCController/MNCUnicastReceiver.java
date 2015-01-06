package pl.edu.pw.elka.tin.MNC.MNCController;

import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCConsts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Wątek odpowiedzialny za odbieranie połączeń TCP
 * @author Karol
 */
public class MNCUnicastReceiver implements Runnable{
    ServerSocket server;
    Socket client;
    PrintWriter out;
    BufferedReader in;
    MNCDevice myDevice;

    public MNCUnicastReceiver(MNCDevice device){
        myDevice = device;
        try{
            server = new ServerSocket(MNCConsts.UCAST_PORT);
            System.out.println("serwer nasłuchuje");
        } catch (IOException e) {
            System.out.println("Could not listen on port");
        }
    }

    @Override
    public void run() {
        while(true){
            try{
                client = server.accept();
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new PrintWriter(client.getOutputStream(), true);
                String line;
                line = in.readLine();
                myDevice.log.acction("odebrano unicast "+line);
                out.println(line);
                client.close();
            } catch (IOException e) {
                System.out.println("Accept failed: 4321");
            }
        }

    }
}
