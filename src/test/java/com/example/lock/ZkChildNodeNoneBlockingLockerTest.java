package com.example.lock;

import org.junit.Test;

public class ZkChildNodeNoneBlockingLockerTest {
    @Test
    public void singleThreadTest() {
        ZkChildNodeNoneBlockingLocker locker1 = new ZkChildNodeNoneBlockingLocker("app3");
        ZkChildNodeNoneBlockingLocker locker2 = new ZkChildNodeNoneBlockingLocker("app3");

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
            threads[i] = new LockThread(new ZkNodeNoneBlockingLock("app3"));
        }
        for (LockThread thread : threads) {
            thread.start();
        }
        for (LockThread thread : threads) {
            thread.join();
        }
    }

    private static class LockThread extends Thread {
        ZkNodeNoneBlockingLock locker;
        public LockThread(ZkNodeNoneBlockingLock locker) {
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
