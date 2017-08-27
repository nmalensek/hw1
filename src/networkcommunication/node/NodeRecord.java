package networkcommunication.node;

import networkcommunication.transport.TCPReceiverThread;
import networkcommunication.transport.TCPSender;

import java.io.IOException;
import java.net.Socket;

public class NodeRecord {
    private String host;
    private int port;
    private Socket communicationSocket;
    private String nodeID;
    private TCPSender sender;
    private TCPReceiverThread receiver;

    public NodeRecord(String host, int port, Socket communicationSocket) throws IOException {
        this.host = host;
        this.port = port;
        this.nodeID = host + ":" + port;
        this.communicationSocket = communicationSocket;
        this.sender = new TCPSender(communicationSocket);
    }

    public String getHost() {
        return host;
    }

    public int getPort() { return port; }

    public String getNodeID() { return nodeID; }

    public Socket getCommunicationSocket() {
        return communicationSocket;
    }

    public void openConnection() throws IOException {
        communicationSocket = new Socket(host, port);
        sender = new TCPSender(communicationSocket);
    }

    public void closeConnection() throws IOException {
        communicationSocket.close();
    }

    public TCPSender getSender() { return this.sender; }

    public TCPReceiverThread getReceiver() {
        return receiver;
    }

    public void setReceiver(TCPReceiverThread receiver) {
        this.receiver = receiver;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeRecord that = (NodeRecord) o;

        if (port != that.port) return false;
        return host != null ? host.equals(that.host) : that.host == null;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return this.getNodeID();
    }
}
