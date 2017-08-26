package networkcommunication.test;

import networkcommunication.messaging.Event;
import networkcommunication.messaging.task.TaskComplete;
import networkcommunication.node.Node;
import networkcommunication.node.NodeRecord;
import networkcommunication.transport.TCPServerThread;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class TestServer implements Node {
    private Map<String, NodeRecord> nodeMap = new HashMap<>();

    private void startServer() {
        TCPServerThread testServerThread = new TCPServerThread(this, 0);
        testServerThread.start();
    }

    @Override
    public void onEvent(Event event, Socket destinationSocket) throws IOException {
        if (event instanceof TaskComplete) {
        }
    }

    public static void main(String[] args) {
        TestServer testServer = new TestServer();
        testServer.startServer();
    }
}
