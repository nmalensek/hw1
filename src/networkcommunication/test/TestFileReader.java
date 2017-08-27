package networkcommunication.test;

import networkcommunication.node.NodeRecord;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class TestFileReader {

    private static final String configFilePath = "test.txt";
    private HashMap<String, String> nodeRecordHashMap = new HashMap<>();
    private String thisNodeIP = Inet4Address.getLocalHost().getHostName();
    private int thisNodePort = 53835;

    public TestFileReader() throws UnknownHostException {
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
//                Socket nodeSocket = new Socket(lineIP, linePort);
//                NodeRecord node = new NodeRecord(lineIP, linePort, nodeSocket);
                nodeRecordHashMap.put(line, line);
            }
        }
        System.out.println("Config file successfully read and network information stored.");
    }

    private void printNodeMap() {
        for (String nodeID : nodeRecordHashMap.keySet()) {
            System.out.println(nodeID);
        }
    }

    public static void main(String[] args) throws UnknownHostException {
        TestFileReader testFileReader = new TestFileReader();
        try {
            testFileReader.readConfigFileAndCacheConnections();
            testFileReader.printNodeMap();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
