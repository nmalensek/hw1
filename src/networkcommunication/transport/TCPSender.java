package networkcommunication.transport;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * code adapted from code provided by instructor at http://www.cs.colostate.edu/~cs455/lectures/CS455-HelpSession1.pdf
 */

public class TCPSender {

    private Socket socketToSendTo;
    private DataOutputStream dataOutputStream;

    public TCPSender() throws IOException {
//        this.socketToSendTo = socketToSendTo;
//        dataOutputStream = new DataOutputStream(new BufferedOutputStream(socketToSendTo.getOutputStream()));
    }

//    public synchronized void sendData(byte[] dataToSend) throws IOException {
//        int dataLength = dataToSend.length;
//        dataOutputStream.writeInt(dataLength);
//        dataOutputStream.write(dataToSend, 0, dataLength);
//        dataOutputStream.flush();
//    }

    public synchronized void sendToSpecificSocket(Socket socket, byte[] data) throws IOException { //was synced
        try {
            int dataLength = data.length;
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeInt(dataLength);
            outputStream.write(data, 0, dataLength);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public void closeStreams() throws IOException {
//        dataOutputStream.close();
//    }

}
