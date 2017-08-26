package networkcommunication.util;

import networkcommunication.node.NodeRecord;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class ConfigFileReader {

    private static final ConfigFileReader instance = new ConfigFileReader();

    private ConfigFileReader() {}

    public static ConfigFileReader getInstance() { return instance; }

    public static synchronized void readConfigFileAndCacheConnections(String configFilePath,
                                                         HashMap<String, NodeRecord> nodesInOverlay,
                                                         String nodeIP,
                                                         int nodePort) throws IOException {
        List<String> fileLines = Files.readAllLines(Paths.get(configFilePath));
        for (String line : fileLines) {
            String[] splitLine = line.split(":");
            String lineIP = splitLine[0];
            int linePort = Integer.parseInt(splitLine[1]);

            if (!lineIP.equals(nodeIP) || linePort != nodePort) {
                Socket nodeSocket = new Socket(lineIP, linePort);
                NodeRecord node = new NodeRecord(lineIP, linePort, nodeSocket);
                nodesInOverlay.put(line, node);
            }
        }
        System.out.println("Config file successfully read and network information stored.");
    }
}
