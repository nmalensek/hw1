package networkcommunication.messaging.eventfactory;

import networkcommunication.messaging.Event;
import networkcommunication.messaging.message.Message;
import networkcommunication.messaging.task.TaskComplete;
import networkcommunication.messaging.task.TaskInitiate;
import networkcommunication.messaging.traffic.PullTrafficSummary;
import networkcommunication.messaging.traffic.TrafficSummary;

import java.io.IOException;

public final class EventFactory {

    /**
     * Class creates events based on the type of message received at a node so the node can respond accordingly.
     */

    private static final EventFactory instance = new EventFactory();

    private EventFactory() { }

    public static EventFactory getInstance() {
        return instance;
    }

    public static Event<TaskInitiate> receiveTaskInitiate(
            byte[] marshalledBytes) throws IOException {
        TaskInitiate taskInitiate = new TaskInitiate();
        taskInitiate.readTaskInitiateMessage(marshalledBytes);
        return taskInitiate;
    }

    public static Event<Message> receiveMessage(
            byte[] marshalledBytes) throws IOException {
        Message messageReceive = new Message();
        messageReceive.readMessage(marshalledBytes);
        return messageReceive;
    }

    public static Event<TaskComplete> taskComplete(
            byte[] marshalledBytes) throws IOException {
        TaskComplete taskComplete = new TaskComplete();
        taskComplete.readMessage(marshalledBytes);
        return taskComplete;
    }

    public static Event<PullTrafficSummary> receivePullTrafficSummary(
            byte[] marshalledBytes) throws IOException {
        PullTrafficSummary pullTrafficSummary = new PullTrafficSummary();
        pullTrafficSummary.readMessage(marshalledBytes);
        return pullTrafficSummary;
    }

    public static Event<TrafficSummary> receiveTrafficSummary(
            byte[] marshalledBytes) throws IOException {
        TrafficSummary trafficSummary = new TrafficSummary();
        trafficSummary.readMessage(marshalledBytes);
        return trafficSummary;
    }
}
