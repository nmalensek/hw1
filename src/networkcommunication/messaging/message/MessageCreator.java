package networkcommunication.messaging.message;

import networkcommunication.collate.CommunicationTracker;
import networkcommunication.messaging.task.ShutdownMessage;
import networkcommunication.node.NodeRecord;
import networkcommunication.node.Process;
import networkcommunication.transport.TCPSender;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageCreator {

    private HashMap<String, NodeRecord> nodeMap;
    private List<String> randomNodes = new ArrayList<>();
    private String hostToMessage;
    private int portToMessage;
    private NodeRecord nodeToMessage;
    private CommunicationTracker communicationTracker;
    private static final Logger logger = Logger.getLogger(Process.class.getName());
    private static final Handler consoleHandler = new ConsoleHandler();

    public MessageCreator(CommunicationTracker communicationTracker, HashMap<String, NodeRecord> nodeMap) {
        this.communicationTracker = communicationTracker;
        this.nodeMap = nodeMap;
        addNodesToRandomList();
        consoleHandler.setLevel(Level.ALL);
        logger.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);
    }

    private void addNodesToRandomList() {
        for (String nodeID : nodeMap.keySet()) {
            randomNodes.add(nodeID);
        }
    }

    /**
     * Sends message to random node, repeats 5 times according to assignment instructions.
     *
     * @throws IOException
     */
    public synchronized void sendMessageNewSocket(TCPSender sender) throws IOException {
//        logger.entering("MessageCreator", "sendMessageNewSocket");
        chooseRandomNode();
        Message message = new Message();
        Socket socketToMessage = new Socket(nodeToMessage.getHost(), nodeToMessage.getPort());
        try {
            for (int numMessages = 0; numMessages < 5; numMessages++) {
//            logger.log(Level.FINE, "Loop " + (numMessages + 1));
                try {
//                logger.log(Level.FINE, "Socket created...");

                    message.setPayload();
                    sender.sendToSpecificSocket(socketToMessage, message.getBytes());

                    communicationTracker.incrementSendTracker();
                    communicationTracker.incrementSendSummation(message.getPayload());
                } catch (Exception e) {
                        logger.log(Level.SEVERE, "SOMETHING IS WRONG");
                    e.printStackTrace();
                }

//                if (numMessages == 4) {
//                    logger.log(Level.FINER, "Sent 5 messages");
//                }
            }
        } finally {
            ShutdownMessage shutdownMessage = new ShutdownMessage();
            sender.sendToSpecificSocket(socketToMessage, shutdownMessage.getBytes());
//            socketToMessage.setSoLinger(true, 0);
//            socketToMessage.close();
        }
//        logger.exiting("MessageCreator", "sendMessageNewSocket");
    }

    public void sendMessageSameSocket(TCPSender sender) throws IOException {
        chooseRandomNodeRecord();
        Message message = new Message();
        for (int numMessages = 0; numMessages < 5; numMessages++) {
            message.setPayload();
            sender.sendToSpecificSocket(nodeToMessage.getCommunicationSocket(), message.getBytes());
            communicationTracker.incrementSendTracker();
            communicationTracker.incrementSendSummation(message.getPayload());
        }
    }

    /**
     * Chooses random node from overlay to message.
     */
    private synchronized void chooseRandomNode() {
        int randomNode = ThreadLocalRandom.current().nextInt(0, randomNodes.size());
        nodeToMessage = nodeMap.get(randomNodes.get(randomNode));
    }

    private void chooseRandomNodeRecord() {
        int randomNode = ThreadLocalRandom.current().nextInt(0, randomNodes.size());
        nodeToMessage = nodeMap.get(randomNodes.get(randomNode));
    }

}
