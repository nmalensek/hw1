package networkcommunication.messaging;

public interface Protocol {
    int MESSAGE = 0;
    int PULL_TRAFFIC_SUMMARY = 1;
    int TRAFFIC_SUMMARY = 2;
    int TASK_INITIATE = 3;
    int TASK_COMPLETE = 4;
    int MESSAGING_NODES_LIST = 5;
    int READY = 6;
    int SHUTDOWN = 99;
}
