package networkcommunication.test;

import networkcommunication.transport.TCPSender;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;

public class TestConnection {


    public static void main(String[] args) {
        try {
            Socket socket = new Socket(Inet4Address.getLocalHost().getHostName(), 52767);
            TCPSender sender = new TCPSender(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
