package networkcommunication.node;

import networkcommunication.collate.CommunicationTracker;
import networkcommunication.messaging.Event;
import networkcommunication.messaging.message.Message;
import networkcommunication.messaging.message.MessageCreator;
import networkcommunication.messaging.storenetworkinfo.MessagingNodesListReceive;
import networkcommunication.messaging.task.ReadySend;
import networkcommunication.messaging.task.TaskComplete;
import networkcommunication.messaging.task.TaskInitiate;
import networkcommunication.messaging.traffic.PullTrafficSummary;
import networkcommunication.messaging.traffic.TrafficSummary;
import networkcommunication.transport.TCPSender;
import networkcommunication.transport.TCPServerThread;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class Process implements Node {

    private static int thisNodePort;
    private static String collatorHost;
    private static int collatorPort;
    private static String configFilePath;
    private String thisNodeIP = Inet4Address.getLocalHost().getHostAddress();
    private String thisNodeHostName = Inet4Address.getLocalHost().getHostName();
    private String thisNodeID;
    private final CommunicationTracker communicationTracker = new CommunicationTracker();
    private HashMap<String, NodeRecord> nodesInOverlay = new HashMap<>();
    private TCPSender sender = new TCPSender();
    private boolean executingTask = false;

    public Process() throws IOException {
    }

    /**
     * Starts listening for connections, records its own host name and port.
     * @throws IOException
     */
    private void startUp() throws IOException {
        createServerThread();
        thisNodeID = thisNodeIP + ":" + thisNodePort;
    }

    private void createServerThread() throws IOException {
        TCPServerThread receivingSocket = new TCPServerThread(this, thisNodePort);
        receivingSocket.start();
    }

    /**
     * Prevents nodes from acting on multiple TaskInitiate messages.
     * @return
     */
    private synchronized boolean checkAndSetBoolean() {
        if (!executingTask) {
            executingTask = true;
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onEvent(Event event, Socket destinationSocket) throws IOException {
        if (event instanceof MessagingNodesListReceive) {
            cacheOverlayInformation(((MessagingNodesListReceive) event).getNodesToConnectTo());
//            connectToCollator();
            sendReadyMessage();
        } else if (event instanceof TaskInitiate) {
            if (!checkAndSetBoolean()) {
                int rounds = ((TaskInitiate) event).getRounds();
                System.out.println("sending messages...");
                sendMessages(rounds);
                sendTaskCompleteToCollator();
            }
        } else if (event instanceof Message) {
                communicationTracker.incrementReceiveTracker();
                communicationTracker.incrementReceiveSummation(((Message) event).getPayload());
        } else if (event instanceof PullTrafficSummary) {
            createAndSendTrafficSummary();
            communicationTracker.resetCounters();
        }
    }

    private void cacheOverlayInformation(String overlayString) throws IOException {
        String[] splitLines = overlayString.split("\n");
        for (String nodeID : splitLines) {
            String nodeIP = nodeID.split(":")[0];
            int nodePort = Integer.parseInt(nodeID.split(":")[1]);
            if (nodeIP.equals(thisNodeHostName) && nodePort == thisNodePort) {
                //don't connect to self
            } else {
                storeNodeInformation(nodeIP, nodePort);
            }
        }
    }

    private void storeNodeInformation(String hostName, int port) throws IOException {
        NodeRecord node = new NodeRecord(hostName, port);
        nodesInOverlay.put(hostName + ":" + port, node);
//        System.out.println(hostName + ":" + port);
    }

//    private void connectToCollator() throws IOException {
//        collatorSocket = new Socket(collatorHost, collatorPort);
//        sender = new TCPSender();
//    }

    private void sendReadyMessage() throws IOException {
        ReadySend ready = new ReadySend();
        Socket socket = new Socket(collatorHost, collatorPort);
        sender.sendToSpecificSocket(socket, ready.getBytes());
        socket.close();
    }

    private void sendMessages(int numberOfRounds) throws IOException {
        MessageCreator messageCreator = new MessageCreator(communicationTracker, nodesInOverlay);
            for (int roundsSent = 0; roundsSent < numberOfRounds; roundsSent++) {
                messageCreator.sendMessageNewSocket(sender);
//                messageCreator.sendMessageSameSocket(collatorSender);
                System.out.println(thisNodeHostName + " has sent " + (roundsSent + 1) * 5 + " messages");
            }
    }

    private void sendTaskCompleteToCollator() throws IOException {
        System.out.println("Done sending messages.");
        TaskComplete taskComplete = new TaskComplete();
        taskComplete.setIpAddress(thisNodeHostName);
        taskComplete.setPortNumber(thisNodePort);
        Socket socket = new Socket(collatorHost, collatorPort);
        sender.sendToSpecificSocket(socket, taskComplete.getBytes());
        socket.close();
    }

    private void createAndSendTrafficSummary() throws IOException {
        TrafficSummary summary = communicationTracker.createTrafficSummary(thisNodeHostName, thisNodePort);
        Socket socket = new Socket(collatorHost, collatorPort);
        sender.sendToSpecificSocket(socket, summary.getBytes());
        socket.close();
    }

    public static void main(String[] args) {
        collatorHost = args[0];
        collatorPort = Integer.parseInt(args[1]);
        thisNodePort = Integer.parseInt(args[2]);
        configFilePath = args[3];

        if (args.length != 4) {
            System.out.println("Usage: [collator host] [collator port] [this node port] [config file path]" +
                    "\nAll nodes should be running before the collator is running.");
        } else {
            try {
                Process process = new Process();
                process.startUp();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
