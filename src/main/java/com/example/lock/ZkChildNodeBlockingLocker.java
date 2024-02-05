package com.example.lock;

import org.apache.zookeeper.AddWatchMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

import java.util.concurrent.CountDownLatch;

public class ZkChildNodeBlockingLocker extends ZkChildNodeLockerBase {

    public ZkChildNodeBlockingLocker(String connectString, String lockName) {
        super(connectString, lockName);
    }

    public ZkChildNodeBlockingLocker(String lockName) {
        super(lockName);
    }

    @Override
    public boolean canLock() {
        String previousNode = getPreviousNode();
        if (previousNode == null) {
            return true;
        }
        CountDownLatch latch = new CountDownLatch(1);
        try {
            zooKeeper.addWatch(rootNodeName + "/" + lockName + "/" + previousNode, event -> {
                if (event.getType().equals(Watcher.Event.EventType.NodeDeleted)) {
                    latch.countDown();
                }
            }, AddWatchMode.PERSISTENT);
            latch.await();
        } catch (KeeperException | InterruptedException ignore) {}
        return true;
    }
}
