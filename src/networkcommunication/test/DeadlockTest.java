package networkcommunication.test;

import networkcommunication.unused.util.DetectDeadlocks;

import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeadlockTest {

    private static Logger logger = Logger.getLogger(DeadlockTest.class.getName());
    private static ConsoleHandler handler = new ConsoleHandler();

    private static void test1() {
        logger.setLevel(Level.ALL);
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        final Object lock1 = new Object();
        final Object lock2 = new Object();

        Thread thread1 = new Thread(new Runnable() {
            @Override public void run() {
                synchronized (lock1) {
                    System.out.println("Thread1 acquired lock1");
                    logger.log(Level.FINE, "test");

                    try {
                        TimeUnit.MILLISECONDS.sleep(50);
                    } catch (InterruptedException ignore) {}
                    synchronized (lock2) {
                        System.out.println("Thread1 acquired lock2");
                    }
                }
            }

        });
        thread1.start();

        Thread thread2 = new Thread(new Runnable() {
            @Override public void run() {
                synchronized (lock2) {
                    System.out.println("Thread2 acquired lock2");
                    try {
                        TimeUnit.MILLISECONDS.sleep(50);
                    } catch (InterruptedException ignore) {}
                    synchronized (lock1) {
                        System.out.println("Thread2 acquired lock1");
                    }
                }
            }
        });
        thread2.start();

        DetectDeadlocks deadlocks = new DetectDeadlocks();
        deadlocks.start();
        // Wait a little for threads to deadlock.
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException ignore) {}
    }

    public static void main(String[] args) {
        DeadlockTest test = new DeadlockTest();
        test.test1();
    }
}
