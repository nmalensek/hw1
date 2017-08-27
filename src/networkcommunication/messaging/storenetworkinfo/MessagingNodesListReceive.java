package networkcommunication.messaging.storenetworkinfo;

import networkcommunication.messaging.Event;
import networkcommunication.messaging.Protocol;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class MessagingNodesListReceive implements Protocol, Event<MessagingNodesListReceive> {
    private int messageType = MESSAGING_NODES_LIST;
    private String nodesToConnectTo;
    //messaging node info (hostname:portnum)

    public void listConnections() {
        System.out.println(getNodesToConnectTo());
    }

    public MessagingNodesListReceive getType() { return this; }

    @Override
    public int getMessageType() {
        return messageType;
    }

    public MessagingNodesListReceive(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream(marshalledBytes);
        DataInputStream dataInputStream =
                new DataInputStream(new BufferedInputStream(byteArrayInputStream));

        messageType = dataInputStream.readInt();

        int nodeLength = dataInputStream.readInt();
        byte[] identifierBytes = new byte[nodeLength];
        dataInputStream.readFully(identifierBytes);

        nodesToConnectTo = new String(identifierBytes);

        byteArrayInputStream.close();
        dataInputStream.close();
    }

    public String getNodesToConnectTo() {
        return nodesToConnectTo;
    }

    public byte[] getBytes() {
        byte[] marshalledBytes = null;
        return marshalledBytes;
    }
}
