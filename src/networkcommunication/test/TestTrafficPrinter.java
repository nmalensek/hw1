package networkcommunication.test;

import networkcommunication.messaging.traffic.TrafficSummary;

public class TestTrafficPrinter {

    private String traffic = "";
    private int totalMessagesSent;
    private int totalMessagesReceived;
    private long totalSendSummation;
    private long totalReceiveSummation;

    public TestTrafficPrinter() {
        createOutputHeaders();
    }

    private void createOutputHeaders() {
        traffic += String.format("%-30s %-10s %-10s %-15s %-15s %n", "", "",
                "", "Summation", "Summation", "");
        traffic += String.format("%-30s %-10s %-10s %-15s %-15s %n", "", "Sent",
                "Received", "sent", "received");
        traffic += String.format("%-30s %-10s %-10s %-15s %-15s %n", "Node ID", "messages",
                "messages", "messages", "messages");
    }

    /**
     * Synchronized so the counts are accurate
     * @param summary individual node's traffic summary
     */
    public synchronized void processSummary(TrafficSummary summary) {
        String nodeID = summary.getHostName() + ":" + summary.getPortNumber();
        int sentMessages = summary.getSentMessages();
        totalMessagesSent = totalMessagesSent + sentMessages;

        long sentSummation = summary.getSendSummation();
        totalSendSummation = totalSendSummation + sentSummation;

        int receivedMessages = summary.getReceivedMessages();
        totalMessagesReceived = totalMessagesReceived + receivedMessages;

        long receiveSummation = summary.getReceiveSummation();
        totalReceiveSummation = totalReceiveSummation + receiveSummation;

        traffic += String.format("%-30s %-10s %-10s %-15s %-15s %n",
                nodeID, sentMessages, receivedMessages, sentSummation, receiveSummation);
    }

    public void addTotalsToString() {
        traffic += String.format("%-30s %-10s %-10s %-15s %-15s %n", "Sum",
                totalMessagesSent, totalMessagesReceived,
                totalSendSummation, totalReceiveSummation);
    }

    public void printTrafficSummary() {
        System.out.println(traffic);
    }

    public void resetTrafficStringAndCounters() {
        traffic = "";
        totalMessagesSent = 0;
        totalMessagesReceived = 0;
        totalSendSummation = 0;
        totalReceiveSummation = 0;
    }

    private TrafficSummary createTestSummary() {
        TrafficSummary testSummary = new TrafficSummary();
        testSummary.setHostName("127.0.0.1");
        testSummary.setPortNumber(5000);
        testSummary.setSentMessages(25000);
        testSummary.setReceivedMessages(24500);
        testSummary.setSendSummation(-1042363440);
        testSummary.setReceiveSummation(-1042363440);

        return testSummary;
    }

    public static void main(String[] args) {
        TestTrafficPrinter test = new TestTrafficPrinter();
        test.processSummary(test.createTestSummary());
        test.addTotalsToString();
        test.printTrafficSummary();
    }
}
