package networkcommunication.messaging.task;

import networkcommunication.messaging.Event;
import networkcommunication.messaging.Protocol;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class ReadyReceive implements Protocol, Event<ReadyReceive> {
    private int messageType = MESSAGING_NODES_LIST;

    public ReadyReceive getType() { return this; }

    @Override
    public int getMessageType() {
        return messageType;
    }

    public ReadyReceive(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream(marshalledBytes);
        DataInputStream dataInputStream =
                new DataInputStream(new BufferedInputStream(byteArrayInputStream));

        messageType = dataInputStream.readInt();

        byteArrayInputStream.close();
        dataInputStream.close();
    }

    public byte[] getBytes() {
        byte[] marshalledBytes = null;
        return marshalledBytes;
    }
}
