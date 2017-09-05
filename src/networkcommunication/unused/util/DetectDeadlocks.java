package networkcommunication.unused.util;

import java.lang.management.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DetectDeadlocks extends Thread {

    private static Logger logger = Logger.getLogger(DetectDeadlocks.class.getName());

    @Override
    public void run() {
        logger.setLevel(Level.ALL);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while(true) {
            ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            long[] threads = bean.findDeadlockedThreads();
            if (threads != null) {
                ThreadInfo[] infos = bean.getThreadInfo(threads);
                for (ThreadInfo info : infos) {
                    LockInfo[] lockinfos = info.getLockedSynchronizers();
                    MonitorInfo[] monitorInfos = info.getLockedMonitors();

                    System.out.println("Thread: " + info.getThreadName() +
                            " lock: " + info.getLockInfo() +
                            " owner: " + info.getLockOwnerName());
                }
                break;
            }
        }
    }
}
