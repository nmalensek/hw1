package networkcommunication.collate;

import networkcommunication.messaging.traffic.TrafficSummary;

public class CommunicationTracker {

    private int sendTracker = 0;
    private int receiveTracker = 0;
    private long sendSummation = 0;
    private long receiveSummation = 0;

    public synchronized void incrementSendTracker() {
        sendTracker++;
    }

    public synchronized void incrementReceiveTracker() {
        receiveTracker++;
    }

    public synchronized void incrementSendSummation(int amountToAdd) {
        sendSummation = sendSummation + amountToAdd;
    }

    public synchronized void incrementReceiveSummation(int amountToAdd) {
        receiveSummation = receiveSummation + amountToAdd;
    }

    public TrafficSummary createTrafficSummary(String nodeIP, int nodePort) {
        TrafficSummary trafficSummary = new TrafficSummary();
        trafficSummary.setIpAddress(nodeIP);
        trafficSummary.setPortNumber(nodePort);
        trafficSummary.setSentMessages(sendTracker);
        trafficSummary.setSendSummation(sendSummation);
        trafficSummary.setReceivedMessages(receiveTracker);
        trafficSummary.setReceiveSummation(receiveSummation);
        return trafficSummary;
    }

    public void resetCounters() {
        sendTracker = 0;
        receiveTracker = 0;
        sendSummation = 0;
        receiveSummation = 0;
    }

    public void printCounters() {
        System.out.println(sendTracker);
        System.out.println(sendSummation);
        System.out.println(receiveTracker);
        System.out.println(receiveSummation);
    }

}
