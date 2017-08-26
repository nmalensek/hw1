package networkcommunication.messaging.task;

import networkcommunication.messaging.Protocol;
import networkcommunication.messaging.Event;

import java.io.*;

public class TaskInitiate implements Protocol, Event<TaskInitiate> {

    int messageType = TASK_INITIATE;
    int rounds;

    public TaskInitiate getType() {
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
        dataOutputStream.writeInt(rounds);

        dataOutputStream.flush();
        marshalledBytes = byteArrayOutputStream.toByteArray();

        byteArrayOutputStream.close();
        dataOutputStream.close();

        return marshalledBytes;
    }

    public void readTaskInitiateMessage(byte[] marshalledBytes) throws IOException {
        ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream(marshalledBytes);
        DataInputStream dataInputStream =
                new DataInputStream(new BufferedInputStream(byteArrayInputStream));

        messageType = dataInputStream.readInt();
        rounds = dataInputStream.readInt();

        byteArrayInputStream.close();
        dataInputStream.close();
    }

    public void setRounds(int rounds) { this.rounds = rounds; }
    public int getRounds() { return rounds; }
}
