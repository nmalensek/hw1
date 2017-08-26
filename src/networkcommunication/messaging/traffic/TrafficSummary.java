package networkcommunication.messaging.traffic;

import networkcommunication.messaging.Protocol;
import networkcommunication.messaging.Event;

import java.io.*;

public class TrafficSummary implements Protocol, Event<TrafficSummary> {

    int messageType = TRAFFIC_SUMMARY;
    String ipAddress;
    int portNumber;
    private int sentMessages;
    private int receivedMessages;
    private int relayedMessages;
    private long sendSummation;
    private long receiveSummation;

    public TrafficSummary getType() {
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

        byte[] identifierBytes = ipAddress.getBytes();
        int elementLength = identifierBytes.length;
        dataOutputStream.writeInt(elementLength);
        dataOutputStream.write(identifierBytes);

        dataOutputStream.writeInt(portNumber);
        dataOutputStream.writeInt(sentMessages);
        dataOutputStream.writeLong(sendSummation);
        dataOutputStream.writeInt(receivedMessages);
        dataOutputStream.writeLong(receiveSummation);
        dataOutputStream.writeInt(relayedMessages);

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

        int routeLength = dataInputStream.readInt();
        byte[] identifierBytes = new byte[routeLength];
        dataInputStream.readFully(identifierBytes);

        ipAddress = new String(identifierBytes);
        portNumber = dataInputStream.readInt();
        sentMessages = dataInputStream.readInt();
        sendSummation = dataInputStream.readLong();
        receivedMessages = dataInputStream.readInt();
        receiveSummation = dataInputStream.readLong();
        relayedMessages = dataInputStream.readInt();

        byteArrayInputStream.close();
        dataInputStream.close();
    }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public int getPortNumber() { return portNumber; }
    public void setPortNumber(int portNumber) { this.portNumber = portNumber; }

    public int getSentMessages() { return sentMessages; }
    public void setSentMessages(int sentMessages) { this.sentMessages = sentMessages; }

    public int getReceivedMessages() { return receivedMessages; }
    public void setReceivedMessages(int receivedMessages) { this.receivedMessages = receivedMessages; }

    public long getSendSummation() { return sendSummation; }
    public void setSendSummation(long sendSummation) { this.sendSummation = sendSummation; }

    public long getReceiveSummation() { return receiveSummation; }
    public void setReceiveSummation(long receiveSummation) { this.receiveSummation = receiveSummation; }
}
