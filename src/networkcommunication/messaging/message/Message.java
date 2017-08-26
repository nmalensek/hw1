package networkcommunication.messaging.message;

import networkcommunication.messaging.Protocol;
import networkcommunication.messaging.Event;

import java.io.*;
import java.util.concurrent.ThreadLocalRandom;

public class Message implements Protocol, Event<Message> {

    private int messageType = MESSAGE;
    private String routingPath;
    private int payload;

    public Message getType() {
        return this;
    }

    @Override
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
        dataOutputStream.writeInt(payload);

        byte[] identifierBytes = routingPath.getBytes();
        int elementLength = identifierBytes.length;
        dataOutputStream.writeInt(elementLength);
        dataOutputStream.write(identifierBytes);

        dataOutputStream.flush();
        marshalledBytes = byteArrayOutputStream.toByteArray();

        byteArrayOutputStream.close();
        dataOutputStream.close();

        return marshalledBytes;
    }

    public void readMessage(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream(marshalledBytes);
        DataInputStream dataInputStream =
                new DataInputStream(new BufferedInputStream(byteArrayInputStream));

        messageType = dataInputStream.readInt();
        payload = dataInputStream.readInt();

        int routeLength = dataInputStream.readInt();
        byte[] identifierBytes = new byte[routeLength];
        dataInputStream.readFully(identifierBytes);

        routingPath = new String(identifierBytes);

        byteArrayInputStream.close();
        dataInputStream.close();
    }

    public void setPayload() {
        payload = ThreadLocalRandom.current().nextInt(-2147483648, 2147483647);
    }
    public int getPayload() { return payload; }

}
