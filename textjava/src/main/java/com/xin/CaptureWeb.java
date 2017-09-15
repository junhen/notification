package com.xin;
//CaptureWeb

/**
 * Created by xiaoxin on 17-4-25.
 */


class CaptureWeb extends Thread {
    boolean waiting = true;
    boolean ready = false;

    CaptureWeb() {
    }

    public void run() {
        String thrdName = Thread.currentThread().getName();
        System.out.println(thrdName + " starting.");
        while (waiting)
            try {
                Thread.sleep(50);
                System.out.println("waiting:" + waiting);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        System.out.println("waiting...");
        startWait();
        try {
            Thread.sleep(1000);
        } catch (Exception exc) {
            System.out.println(thrdName + " interrupted.");
        }
        System.out.println(thrdName + " terminating.");
    }

    synchronized void startWait() {
        try {
            while (!ready) wait();
        } catch (InterruptedException exc) {
            System.out.println("wait() interrupted");
        }
    }

    synchronized void notice() {
        ready = true;
        notify();
    }


    public static void main(String args[])
            throws Exception {
        CaptureWeb thrd = new CaptureWeb();
        thrd.setName("CaptureWeb #1");
        showThreadStatus(thrd);
        thrd.start();
        Thread.sleep(500);
        showThreadStatus(thrd);
        thrd.waiting = false;
        Thread.sleep(500);
        showThreadStatus(thrd);
        thrd.notice();
        Thread.sleep(500);
        showThreadStatus(thrd);
        while (thrd.isAlive())
            Thread.sleep(200);
            System.out.println("alive");
        showThreadStatus(thrd);
    }

    static void showThreadStatus(Thread thrd) {
        System.out.println(thrd.getName() + ",   Alive := " + thrd.isAlive() + ",   State:=" + thrd.getState());
    }

}



