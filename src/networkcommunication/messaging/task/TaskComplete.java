package networkcommunication.messaging.task;

import networkcommunication.messaging.Protocol;
import networkcommunication.messaging.Event;

import java.io.*;

public class TaskComplete implements Protocol, Event<TaskComplete> {

    int messageType = TASK_COMPLETE;
    int portNumber;
    String ipAddress;

    public TaskComplete getType() {
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
        dataOutputStream.writeInt(portNumber);

        byte[] identifierBytes = ipAddress.getBytes();
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
        portNumber = dataInputStream.readInt();

        int routeLength = dataInputStream.readInt();
        byte[] identifierBytes = new byte[routeLength];
        dataInputStream.readFully(identifierBytes);

        ipAddress = new String(identifierBytes);

        byteArrayInputStream.close();
        dataInputStream.close();
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
