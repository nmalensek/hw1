package networkcommunication.test;

import networkcommunication.messaging.task.ShutdownMessage;
import networkcommunication.transport.TCPSender;

import java.io.IOException;
import java.net.Socket;

public class ShutdownTest {

    private static int serverPort;
    private Socket testSocket = new Socket("Nicholass-MacBook-Air.local", serverPort);

    public ShutdownTest() throws IOException {
    }

    private void sendMessage() throws IOException {
        TCPSender sender = new TCPSender();
        ShutdownMessage shutdownMessage = new ShutdownMessage();
        sender.sendToSpecificSocket(testSocket, shutdownMessage.getBytes());
    }

    public static void main(String[] args) {
        serverPort = Integer.parseInt(args[0]);

        try {
            ShutdownTest test = new ShutdownTest();
            test.sendMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
