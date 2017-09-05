package networkcommunication.node;

import networkcommunication.collate.TrafficPrinter;
import networkcommunication.messaging.Event;
import networkcommunication.messaging.storenetworkinfo.MessagingNodesList;
import networkcommunication.messaging.task.ReadyReceive;
import networkcommunication.messaging.task.TaskComplete;
import networkcommunication.messaging.task.TaskInitiate;
import networkcommunication.messaging.traffic.PullTrafficSummary;
import networkcommunication.messaging.traffic.TrafficSummary;
import networkcommunication.transport.TCPSender;
import networkcommunication.transport.TCPServerThread;

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
    private HashMap<String, NodeRecord> nodeMapCopy;
    private TCPSender sender;
    private long timeStart;
    private long timeEnd;

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
                sender = new TCPSender(nodeSocket);
        }
        nodeMapCopy = new HashMap<>(nodeMap);
        System.out.println("Config file successfully read and network information stored.");
    }

    private void dispatchOverlayInformation() throws IOException {
        MessagingNodesList messagingNodesList = new MessagingNodesList();
        messagingNodesList.setMessagingNodes(nodeMap);
        messageAllNodes(messagingNodesList);
    }

    @Override
    public void onEvent(Event event, Socket destinationSocket) throws IOException {
        if (event instanceof ReadyReceive) {
            readyNodes.getAndIncrement();
            System.out.println(readyNodes + " node(s) ready");
            if (readyNodes.get() == nodeMap.size()) {
                timeStart = System.currentTimeMillis();
                System.out.println("there are " + nodeMap.size() + " nodes in the overlay.");
                initiateMessagingProcess();
            }
        } else if (event instanceof TaskComplete) {
            finishedNodes.getAndIncrement();
            System.out.println(finishedNodes.get() + " nodes done.");
            removeFinishedNodes(((TaskComplete) event).getIpAddress() + ":" + ((TaskComplete) event).getPortNumber());
            printDone();
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
                timeEnd = System.currentTimeMillis();
                System.out.println("Messaging took: " + (timeEnd - timeStart));
                resetCounters();
            }
        }
    }

    private synchronized void removeFinishedNodes(String id) {
        nodeMapCopy.remove(id);
    }

    private void messageAllNodes(Event message) throws IOException {
        System.out.println("Messaging all nodes...");
        for (String node : nodeMap.keySet()) {
            String nodeHost = node.split(":")[0];
            int nodePort = Integer.parseInt(node.split(":")[1]);
            Socket socket = new Socket(nodeHost, nodePort);
            sender.sendToSpecificSocket(socket, message.getBytes());
            socket.close();
        }
        System.out.println("All nodes messaged...");
    }

    private synchronized void printDone() {
        String done = "The following nodes are still going: ";
        for (String id : nodeMapCopy.keySet()) {
            done += id + ",";
        }
        System.out.println(done);
    }

    private void initiateMessagingProcess() throws IOException {
        TaskInitiate taskInitiate = new TaskInitiate();
        taskInitiate.setRounds(numberOfRounds);
        messageAllNodes(taskInitiate);
    }

    private void pullTrafficSummary() throws IOException {
        PullTrafficSummary pullTrafficSummary = new PullTrafficSummary();
        messageAllNodes(pullTrafficSummary);
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
        if (args.length != 3) {
            System.out.println("Usage: [config file path] [number of rounds] [this node's port]");
        } else {
            Collator collator = new Collator();
            try {
                collator.startUp();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
