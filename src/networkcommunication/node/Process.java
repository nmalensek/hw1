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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

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
        createServerThread();
    }

    private void createServerThread() throws IOException {
        receivingSocket = new TCPServerThread(this, thisNodePort); //node starts listening on specified port
        receivingSocket.start();
    }

    @Override
    public void onEvent(Event event, Socket destinationSocket) throws IOException {
        if (event instanceof TaskInitiate) {
            int rounds = ((TaskInitiate) event).getRounds();
            readConfigFileAndCacheConnections();
            sendMessages(rounds);
            connectToCollator();
            sendTaskCompleteToCollator();
        } else if (event instanceof Message) {
            communicationTracker.incrementReceiveTracker();
            communicationTracker.incrementReceiveSummation(((Message) event).getPayload());
        } else if (event instanceof PullTrafficSummary) {
            createAndSendTrafficSummary();
            communicationTracker.resetCounters();
        }
    }

    private void readConfigFileAndCacheConnections() throws IOException {
        List<String> fileLines = Files.readAllLines(Paths.get(configFilePath));
        for (String line : fileLines) {
            String[] splitLine = line.split(":");
            String lineIP = splitLine[0];
            int linePort = Integer.parseInt(splitLine[1]);

            if (lineIP.equals(thisNodeIP) && linePort == thisNodePort) {
                //don't connect to self
            } else {
                Socket nodeSocket = new Socket(lineIP, linePort);
                NodeRecord node = new NodeRecord(lineIP, linePort, nodeSocket);
                nodesInOverlay.put(line, node);
            }
        }
        System.out.println("Config file successfully read and network information stored.");
    }

    private void sendMessages(int numberOfRounds) throws IOException {
        MessageCreator messageCreator = new MessageCreator(communicationTracker, nodesInOverlay);
            for (int roundsSent = 0; roundsSent < numberOfRounds; roundsSent++) {
                messageCreator.sendMessage();
            }
    }

    private void sendTaskCompleteToCollator() throws IOException {
        TaskComplete taskComplete = new TaskComplete();
        taskComplete.setIpAddress(thisNodeIP);
        taskComplete.setPortNumber(thisNodePort);
        collatorSender.sendData(taskComplete.getBytes());
    }

    private void connectToCollator() throws IOException {
        collatorSocket = new Socket(collatorHost, collatorPort);
        collatorSender = new TCPSender(collatorSocket);
    }

    private void createAndSendTrafficSummary() throws IOException {
        TrafficSummary summary = communicationTracker.createTrafficSummary(thisNodeIP, thisNodePort);
        collatorSender.sendData(summary.getBytes());
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
