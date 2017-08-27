package networkcommunication.messaging.storenetworkinfo;

import networkcommunication.messaging.Event;
import networkcommunication.messaging.Protocol;
import networkcommunication.node.NodeRecord;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static networkcommunication.messaging.Protocol.MESSAGING_NODES_LIST;

public class MessagingNodesList implements Protocol, Event<MessagingNodesList> {

    private int messageType = MESSAGING_NODES_LIST;
    private String messagingNodes;

    public MessagingNodesList getType() {
        return this;
    }

    public int getMessageType() {
        return messageType;
    }

    //marshalls bytes
    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream =
                new DataOutputStream(new BufferedOutputStream(byteArrayOutputStream));

        dataOutputStream.writeInt(messageType);

        byte[] identifierBytes = messagingNodes.getBytes();
        int elementLength = identifierBytes.length;
        dataOutputStream.writeInt(elementLength);
        dataOutputStream.write(identifierBytes);

        dataOutputStream.flush();
        marshalledBytes = byteArrayOutputStream.toByteArray();

        byteArrayOutputStream.close();
        dataOutputStream.close();

        return marshalledBytes;
    }

    public void setMessagingNodes(HashMap<String, NodeRecord> nodesToConnectTo) {
        messagingNodes = "";
        for (NodeRecord node : nodesToConnectTo.values()) {
            messagingNodes += node.getNodeID();
            messagingNodes += "\n";
        }
    }
}
