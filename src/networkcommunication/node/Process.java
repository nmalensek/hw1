package networkcommunication.node;

import networkcommunication.collate.CommunicationTracker;
import networkcommunication.messaging.Event;
import networkcommunication.messaging.message.Message;
import networkcommunication.messaging.message.MessageCreator;
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
    private static String configFilePath;
    private static String collatorHost;
    private static int collatorPort;
    private String thisNodeIP = Inet4Address.getLocalHost().getHostAddress();
    private String thisNodeID;
    private ConfigFileReader reader = ConfigFileReader.getInstance();
    private TCPServerThread receivingSocket;
    private CommunicationTracker communicationTracker = new CommunicationTracker();
    private HashMap<String, NodeRecord> nodesInOverlay = new HashMap<>();
    private Socket collatorSocket;
    private TCPSender collatorSender;

    public Process() throws IOException {
    }

    private void startUp() throws IOException {
        thisNodeID = thisNodeIP + ":" + thisNodePort;
        reader.readConfigFileAndCacheConnections(configFilePath, nodesInOverlay, thisNodeIP, thisNodePort);
        createServerThread();
    }

    private void createServerThread() throws IOException {
        receivingSocket = new TCPServerThread(this, thisNodePort); //node starts listening on specified port
        receivingSocket.start();
    }

    @Override
    public void onEvent(Event event, Socket destinationSocket) throws IOException {
        if (event instanceof TaskInitiate) {
            int numberOfRounds = ((TaskInitiate) event).getRounds();
            MessageCreator messageCreator = new MessageCreator(communicationTracker, nodesInOverlay);
            for (int roundsSent = 0; roundsSent < numberOfRounds; roundsSent++) {
                messageCreator.sendMessage();
            }
            connectToCollator();
            sendTaskComplete();
        } else if (event instanceof Message) {
            communicationTracker.incrementReceiveTracker();
            communicationTracker.incrementReceiveSummation(((Message) event).getPayload());
        } else if (event instanceof PullTrafficSummary) {
            createAndSendTrafficSummary();
            communicationTracker.resetCounters();
        }
    }

    private void sendTaskComplete() throws IOException {
        TaskComplete taskComplete = new TaskComplete();
        taskComplete.setIpAddress(thisNodeIP);
        taskComplete.setPortNumber(thisNodePort);
        collatorSender.sendData(taskComplete.getBytes());
    }

    private void createAndSendTrafficSummary() throws IOException {
        TrafficSummary summary = communicationTracker.createTrafficSummary(thisNodeIP, thisNodePort);
        collatorSender.sendData(summary.getBytes());
    }

    private void connectToCollator() throws IOException {
        collatorSocket = new Socket(collatorHost, collatorPort);
        collatorSender = new TCPSender(collatorSocket);
    }

    public static void main(String[] args) {
        configFilePath = args[0];
        thisNodePort = Integer.parseInt(args[1]);
        collatorHost = args[2];
        collatorPort = Integer.parseInt(args[3]);

        try {
            Process process = new Process();
            process.startUp();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
