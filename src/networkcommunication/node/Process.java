package networkcommunication.node;

import networkcommunication.collate.CommunicationTracker;
import networkcommunication.messaging.Event;
import networkcommunication.messaging.message.Message;
import networkcommunication.messaging.message.MessageCreator;
import networkcommunication.messaging.storenetworkinfo.MessagingNodesList;
import networkcommunication.messaging.storenetworkinfo.MessagingNodesListReceive;
import networkcommunication.messaging.task.ReadySend;
import networkcommunication.messaging.task.TaskComplete;
import networkcommunication.messaging.task.TaskInitiate;
import networkcommunication.messaging.traffic.PullTrafficSummary;
import networkcommunication.messaging.traffic.TrafficSummary;
import networkcommunication.transport.TCPSender;
import networkcommunication.transport.TCPServerThread;
import networkcommunication.util.ConfigFileReader;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.HashMap;

public class Process implements Node {

    private static int thisNodePort;
    private static String collatorHost;
    private static int collatorPort;
    private String thisNodeIP = Inet4Address.getLocalHost().getHostAddress();
    private String thisNodeHostName = Inet4Address.getLocalHost().getHostName();
    private String thisNodeID;
    private TCPServerThread receivingSocket;
    private CommunicationTracker communicationTracker = new CommunicationTracker();
    private HashMap<String, NodeRecord> nodesInOverlay = new HashMap<>();
    private Socket collatorSocket;
    private TCPSender collatorSender;

    public Process() throws IOException {
    }

    private void startUp() throws IOException {
        thisNodeID = thisNodeIP + ":" + thisNodePort;
        createServerThread();
    }

    private void createServerThread() throws IOException {
        receivingSocket = new TCPServerThread(this, thisNodePort); //node starts listening on specified port
        receivingSocket.start();
    }

    @Override
    public void onEvent(Event event, Socket destinationSocket) throws IOException {
        if (event instanceof MessagingNodesListReceive) {
            cacheOverlayInformation(((MessagingNodesListReceive) event).getNodesToConnectTo());
            connectToCollator();
            sendReadyMessage();
        } else if (event instanceof TaskInitiate) {
            int rounds = ((TaskInitiate) event).getRounds();
            System.out.println("sending messages...");
            sendMessages(rounds);
            sendTaskCompleteToCollator();
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
        Socket nodeSocket = new Socket(hostName, port);
//        nodeSocket.setReuseAddress(true);
        NodeRecord node = new NodeRecord(hostName, port, nodeSocket);
        nodesInOverlay.put(hostName + ":" + port, node);
        System.out.println(hostName + ":" + port);
    }

    private void connectToCollator() throws IOException {
        collatorSocket = new Socket(collatorHost, collatorPort);
        collatorSender = new TCPSender(collatorSocket);
    }

    private void sendReadyMessage() throws IOException {
        ReadySend ready = new ReadySend();
        collatorSender.sendData(ready.getBytes());
    }

    private void sendMessages(int numberOfRounds) throws IOException {
        MessageCreator messageCreator = new MessageCreator(communicationTracker, nodesInOverlay);
            for (int roundsSent = 0; roundsSent < numberOfRounds; roundsSent++) {
                messageCreator.sendMessage();
                System.out.println(thisNodeHostName + " has sent " + roundsSent * 5 + " messages");
            }
    }

    private void sendTaskCompleteToCollator() throws IOException {
        System.out.println("Done sending messages.");
        TaskComplete taskComplete = new TaskComplete();
        taskComplete.setIpAddress(thisNodeIP);
        taskComplete.setPortNumber(thisNodePort);
        collatorSender.sendData(taskComplete.getBytes());
    }

    private void createAndSendTrafficSummary() throws IOException {
        TrafficSummary summary = communicationTracker.createTrafficSummary(thisNodeHostName, thisNodePort);
        collatorSender.sendData(summary.getBytes());
    }

    public static void main(String[] args) {
        thisNodePort = Integer.parseInt(args[0]);
        collatorHost = args[1];
        collatorPort = Integer.parseInt(args[2]);

        try {
            Process process = new Process();
            process.startUp();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
