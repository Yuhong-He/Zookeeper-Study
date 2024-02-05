package com.example.lock;

import org.junit.Test;

public class ZkChildNodeBlockingLockerTest {
    @Test
    public void singleThreadTest() {
        ZkChildNodeBlockingLocker locker1 = new ZkChildNodeBlockingLocker("app4");
        ZkChildNodeBlockingLocker locker2 = new ZkChildNodeBlockingLocker("app4");

        System.out.println("locker1 lock: " + locker1.lock());

        System.out.println("locker1 check lock exist: " + locker1.exists());
        System.out.println("locker2 check lock exist: " + locker2.exists());

        System.out.println("locker2 lock first try: " + locker2.lock());

        System.out.println("locker1 unlock: " + locker1.unlock());

        System.out.println("locker2 lock second try: " + locker2.lock());
    }

    @Test
    public void multipleThreadTest() throws InterruptedException {
        LockThread[] threads = new LockThread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new LockThread(new ZkChildNodeBlockingLocker("app4"));
        }
        for (LockThread thread : threads) {
            thread.start();
        }
        for (LockThread thread : threads) {
            thread.join();
        }
    }

    private static class LockThread extends Thread {
        ZkChildNodeBlockingLocker locker;
        public LockThread(ZkChildNodeBlockingLocker locker) {
            this.locker = locker;
        }

        @Override
        public void run() {
            boolean lockRes = locker.lock();
            if(lockRes) {
                System.out.println(getName() + " lock success!");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignore) {}
                boolean unlockRes = locker.unlock();
                System.out.println(getName() + " unlock: " + unlockRes);
            } else {
                System.out.println(getName() + " lock failed.");
            }
        }
    }
}
