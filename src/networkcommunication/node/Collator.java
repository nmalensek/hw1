package networkcommunication.node;

import networkcommunication.collate.TrafficPrinter;
import networkcommunication.messaging.Event;
import networkcommunication.messaging.task.TaskComplete;
import networkcommunication.messaging.task.TaskInitiate;
import networkcommunication.messaging.traffic.PullTrafficSummary;
import networkcommunication.messaging.traffic.TrafficSummary;
import networkcommunication.transport.TCPServerThread;
import networkcommunication.util.ConfigFileReader;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class Collator implements Node {

    private String thisNodeIP = Inet4Address.getLocalHost().getHostAddress();
    private static int thisNodePort;
    private static String configFilePath;
    private int finishedNodes;
    private int numberOfSummariesReceived;
    private static int numberOfRounds;
    private TrafficPrinter trafficPrinter = new TrafficPrinter();
    private HashMap<String, NodeRecord> nodeMap = new HashMap<>();

    public Collator() throws UnknownHostException {

    }

    private void startUp() throws IOException {
        TCPServerThread collatorServerThread = new TCPServerThread(this, thisNodePort);
        collatorServerThread.start();
        readConfigFileAndCacheConnections();
        initiateMessagingProcess();
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
                nodeMap.put(line, node);
            }
        }
        System.out.println("Config file successfully read and network information stored.");
    }

    private void initiateMessagingProcess() throws IOException {
        trafficPrinter.resetTrafficStringAndCounters();
        finishedNodes = 0;
        TaskInitiate taskInitiate = new TaskInitiate();
        taskInitiate.setRounds(numberOfRounds);
        for (NodeRecord node : nodeMap.values()) {
            node.getSender().sendData(taskInitiate.getBytes());
        }
    }

    @Override
    public void onEvent(Event event, Socket destinationSocket) throws IOException {
        if (event instanceof TaskComplete) {
            ++finishedNodes;
            if(finishedNodes == nodeMap.size()) {
                try {
                    Thread.sleep(15000);
                    pullTrafficSummary();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else if (event instanceof TrafficSummary) {
            trafficPrinter.processSummary(((TrafficSummary) event));
            ++numberOfSummariesReceived;
            if (numberOfSummariesReceived == nodeMap.size()) {
                trafficPrinter.addTotalsToString();
                trafficPrinter.printTrafficSummary();
                numberOfSummariesReceived = 0;
            }
        }
    }

    private void pullTrafficSummary() throws IOException {
        PullTrafficSummary pullTrafficSummary = new PullTrafficSummary();
        for (NodeRecord nodeRecord : nodeMap.values()) {
            nodeRecord.getSender().sendData(pullTrafficSummary.getBytes());
        }
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
