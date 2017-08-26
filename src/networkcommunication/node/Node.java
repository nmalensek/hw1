package networkcommunication.node;

import networkcommunication.messaging.Event;

import java.io.IOException;
import java.net.Socket;

public interface Node {

    void onEvent(Event event, Socket destinationSocket) throws IOException;
}
