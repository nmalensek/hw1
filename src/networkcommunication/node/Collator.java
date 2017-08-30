package networkcommunication.node;

import networkcommunication.collate.TrafficPrinter;
import networkcommunication.messaging.Event;
import networkcommunication.messaging.storenetworkinfo.MessagingNodesList;
import networkcommunication.messaging.task.ReadyReceive;
import networkcommunication.messaging.task.TaskComplete;
import networkcommunication.messaging.task.TaskInitiate;
import networkcommunication.messaging.traffic.PullTrafficSummary;
import networkcommunication.messaging.traffic.TrafficSummary;
import networkcommunication.transport.TCPServerThread;
import networkcommunication.util.ConfigFileWriter;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Collator implements Node {

    private static int thisNodePort;
    private static String configFilePath;
    private AtomicInteger readyNodes = new AtomicInteger(0);
    private AtomicInteger finishedNodes = new AtomicInteger(0);
    private AtomicInteger numberOfSummariesReceived = new AtomicInteger(0);
    private static int numberOfRounds;
    private static TrafficPrinter trafficPrinter = new TrafficPrinter();
    private HashMap<String, NodeRecord> nodeMap = new HashMap<>();

    public Collator() throws UnknownHostException {

    }

    private void startUp() throws IOException {
        TCPServerThread collatorServerThread = new TCPServerThread(this, thisNodePort);
        collatorServerThread.start();
        readConfigFileAndCacheConnections();
        dispatchOverlayInformation();
    }

    private void readConfigFileAndCacheConnections() throws IOException {
        List<String> fileLines = Files.readAllLines(Paths.get(configFilePath));
        for (String line : fileLines) {
                String[] splitLine = line.split(":");
                String lineHost = splitLine[0];
                int linePort = Integer.parseInt(splitLine[1]);

                Socket nodeSocket = new Socket(lineHost, linePort);
                NodeRecord node = new NodeRecord(lineHost, linePort, nodeSocket);
                nodeMap.put(line, node);
        }
        System.out.println("Config file successfully read and network information stored.");
    }

    private void dispatchOverlayInformation() throws IOException {
        MessagingNodesList messagingNodesList = new MessagingNodesList();
        messagingNodesList.setMessagingNodes(nodeMap);
        for (NodeRecord node : nodeMap.values()) {
            node.getSender().sendData(messagingNodesList.getBytes());
        }
    }

    @Override
    public void onEvent(Event event, Socket destinationSocket) throws IOException {
        if (event instanceof ReadyReceive) {
            readyNodes.getAndIncrement();
            System.out.println(readyNodes + " node(s) ready");
            if (readyNodes.get() == nodeMap.size()) {
                System.out.println("there are " + nodeMap.size() + " nodes in the overlay.");
                initiateMessagingProcess();
            }
        } else if (event instanceof TaskComplete) {
            finishedNodes.getAndIncrement();
            System.out.println(finishedNodes.get() + " nodes done.");
            if (finishedNodes.get() == nodeMap.size()) {
                try {
                    Thread.sleep(15000);
                    pullTrafficSummary();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else if (event instanceof TrafficSummary) {
            trafficPrinter.processSummary(((TrafficSummary) event));
            numberOfSummariesReceived.getAndIncrement();
            if (numberOfSummariesReceived.get() == nodeMap.size()) {
                trafficPrinter.addTotalsToString();
                trafficPrinter.printTrafficSummary();
                resetCounters();
//                ConfigFileWriter.getInstance().clearFile(configFilePath);
            }
        }
    }

    private void initiateMessagingProcess() throws IOException {
        TaskInitiate taskInitiate = new TaskInitiate();
        taskInitiate.setRounds(numberOfRounds);
        for (NodeRecord node : nodeMap.values()) {
            node.getSender().sendData(taskInitiate.getBytes());
        }
    }

    private void pullTrafficSummary() throws IOException {
        PullTrafficSummary pullTrafficSummary = new PullTrafficSummary();
        for (NodeRecord nodeRecord : nodeMap.values()) {
            nodeRecord.getSender().sendData(pullTrafficSummary.getBytes());
        }
    }

    private void resetCounters() {
        numberOfSummariesReceived.set(0);
        trafficPrinter.resetTrafficStringAndCounters();
        finishedNodes.set(0);
    }

    public static void main(String[] args) throws UnknownHostException {
        configFilePath = args[0];
        numberOfRounds = Integer.parseInt(args[1]);
        thisNodePort = Integer.parseInt(args[2]);
        Collator collator = new Collator();
        try {
            collator.startUp();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
