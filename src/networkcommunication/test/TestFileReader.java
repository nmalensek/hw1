package networkcommunication.test;

import networkcommunication.node.NodeRecord;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class TestFileReader {

    private static final String FILEPATH = "test.txt";
    private HashMap<String, NodeRecord> nodeRecordHashMap = new HashMap<>();
    private String thisNodeIP = Inet4Address.getLocalHost().getHostName();
    private int thisNodePort = 53835;

    public TestFileReader() throws UnknownHostException {
    }


    private void readConfigFile() throws IOException {
        List<String> fileLines = Files.readAllLines(Paths.get(FILEPATH));
        for (String line : fileLines) {
            String[] splitLine = line.split(":");
            if (!splitLine[0].equals(thisNodeIP) || Integer.parseInt(splitLine[1]) != thisNodePort) {
                NodeRecord node = new NodeRecord(splitLine[0], Integer.parseInt(splitLine[1]));
                nodeRecordHashMap.put(line, node);
            }
        }
    }

    private void printNodeMap() {
        for (String nodeID : nodeRecordHashMap.keySet()) {
            System.out.println(nodeID);
        }
    }

    public static void main(String[] args) throws UnknownHostException {
        TestFileReader testFileReader = new TestFileReader();
        try {
            testFileReader.readConfigFile();
            testFileReader.printNodeMap();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
