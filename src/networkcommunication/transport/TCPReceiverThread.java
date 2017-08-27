package networkcommunication.transport;

import networkcommunication.messaging.*;
import networkcommunication.messaging.Event;
import networkcommunication.messaging.message.Message;
import networkcommunication.messaging.storenetworkinfo.MessagingNodesListReceive;
import networkcommunication.messaging.task.ReadyReceive;
import networkcommunication.messaging.task.TaskComplete;
import networkcommunication.messaging.task.TaskInitiate;
import networkcommunication.messaging.traffic.PullTrafficSummary;
import networkcommunication.messaging.traffic.TrafficSummary;
import networkcommunication.messaging.eventfactory.*;
import networkcommunication.node.Node;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * code adapted from code provided by instructor at http://www.cs.colostate.edu/~cs455/lectures/CS455-HelpSession1.pdf
 */

public class TCPReceiverThread extends Thread implements Protocol {

    private Socket communicationSocket;
    private DataInputStream dataInputStream;
    private Node node;
    private EventFactory eventFactory = EventFactory.getInstance();

    public TCPReceiverThread(Socket communicationSocket, Node node) throws IOException {
        this.communicationSocket = communicationSocket;
        this.node = node;
        dataInputStream = new DataInputStream(communicationSocket.getInputStream());
    }

    /**
     * Listens for a message coming in.
     */
    public void run() {
        int dataLength;
        while (communicationSocket != null) {
            try {
                dataLength = dataInputStream.readInt();

                byte[] data = new byte[dataLength];
                dataInputStream.readFully(data, 0, dataLength);

                determineMessageType(data);

            } catch (IOException ioe) {
                ioe.getMessage();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reads first line of message to determine the message type, then passes that to a switch statement to process
     * the message the rest of the way and pass it to the node.
     *
     * @param marshalledBytes packaged message
     * @throws IOException
     */
    public void determineMessageType(byte[] marshalledBytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream(marshalledBytes);
        DataInputStream dataInputStream =
                new DataInputStream(new BufferedInputStream(byteArrayInputStream));

        int messageType = dataInputStream.readInt();
        dataInputStream.close();

        switch (messageType) {
            case MESSAGING_NODES_LIST:
                Event<MessagingNodesListReceive> receiveMessagingListEvent =
                        eventFactory.receiveMessagingList(marshalledBytes);
                node.onEvent(receiveMessagingListEvent, communicationSocket);
                break;
            case READY:
                Event<ReadyReceive> receiveReadyEvent =
                        eventFactory.receiveReadyMessage(marshalledBytes);
                node.onEvent(receiveReadyEvent, communicationSocket);
                break;
            case TASK_INITIATE:
                Event<TaskInitiate> receiveTaskInitiateEvent =
                        eventFactory.receiveTaskInitiate(marshalledBytes);
                node.onEvent(receiveTaskInitiateEvent, communicationSocket);
                break;
            case MESSAGE:
                Event<Message> receiveMessageEvent =
                        eventFactory.receiveMessage(marshalledBytes);
                node.onEvent(receiveMessageEvent, communicationSocket);
                break;
            case TASK_COMPLETE:
                Event<TaskComplete> taskCompleteEvent =
                        eventFactory.taskComplete(marshalledBytes);
                node.onEvent(taskCompleteEvent, communicationSocket);
                break;
            case PULL_TRAFFIC_SUMMARY:
                Event<PullTrafficSummary> receivePullTrafficSummary =
                        eventFactory.receivePullTrafficSummary(marshalledBytes);
                node.onEvent(receivePullTrafficSummary, communicationSocket);
                break;
            case TRAFFIC_SUMMARY:
                Event<TrafficSummary> receiveTrafficSummary =
                        eventFactory.receiveTrafficSummary(marshalledBytes);
                node.onEvent(receiveTrafficSummary, communicationSocket);
                break;
            default:
                System.out.println("Something went horribly wrong, please restart.");
        }
    }
}
