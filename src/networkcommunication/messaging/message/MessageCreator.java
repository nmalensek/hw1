package networkcommunication.messaging.message;

import networkcommunication.collate.CommunicationTracker;
import networkcommunication.node.NodeRecord;
import networkcommunication.transport.TCPSender;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MessageCreator {

    private HashMap<String, NodeRecord> nodeMap;
    private List<String> randomNodes = new ArrayList<>();
    private String hostToMessage;
    private int portToMessage;
    private CommunicationTracker communicationTracker;

    public MessageCreator(CommunicationTracker communicationTracker, HashMap<String, NodeRecord> nodeMap) {
        this.communicationTracker = communicationTracker;
        this.nodeMap = nodeMap;
        addNodesToRandomList();
    }

    private void addNodesToRandomList() {
        for (String nodeID : nodeMap.keySet()) {
            randomNodes.add(nodeID);
        }
    }

    /**
     * Sends message to random node, repeats 5 times according to assignment instructions.
     * @throws IOException
     */
    public void sendMessage() throws IOException {
        chooseRandomNode();
        Message message = new Message();
        Socket socketToMessage = new Socket(hostToMessage, portToMessage);
        TCPSender sender = new TCPSender(socketToMessage);
        for (int numMessages = 0; numMessages < 5; numMessages++) {
            message.setPayload();
            sender.sendData(message.getBytes());
            communicationTracker.incrementSendTracker();
            communicationTracker.incrementSendSummation(message.getPayload());
        }
        socketToMessage.close();
    }

    /**
     * Chooses random node from overlay to message.
     */
    public void chooseRandomNode() {
        int randomNode = ThreadLocalRandom.current().nextInt(0, randomNodes.size());
        hostToMessage = randomNodes.get(randomNode).split(":")[0];
        portToMessage = Integer.parseInt(randomNodes.get(randomNode).split(":")[1]);
    }

}
