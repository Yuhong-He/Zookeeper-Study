package com.example.lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZkLockerBase {
    protected ZooKeeper zooKeeper;
    protected String rootNodeName = "/zk-lock";
    protected String lockName = "";

    public ZkLockerBase() {
        this("172.16.190.134:2181,172.16.190.135:2181,172.16.190.136:2181");
    }

    public ZkLockerBase(String connectString) {
        CountDownLatch latch = new CountDownLatch(1);
        Watcher watcher = event -> {
            switch (event.getType()) {
                case None:
                    if (event.getState().equals(Watcher.Event.KeeperState.SyncConnected)) {
                        latch.countDown();
                    }
                    break;
                case NodeCreated:
                    break;
                case NodeDeleted:
                    break;
                case NodeChildrenChanged:
                    break;
            }
        };
        try {
            zooKeeper = new ZooKeeper(connectString, 5000, watcher);
            latch.await();
            createRootNode();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void createRootNode() {
        if (exists(rootNodeName)) {
            createNode(rootNodeName, CreateMode.CONTAINER);
        }
    }

    public boolean exists(String nodeName) {
        try {
            Stat exists = zooKeeper.exists(nodeName, false);
            return exists != null;
        } catch (KeeperException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public String createNode(String nodeName, CreateMode createMode) {
        try {
            return zooKeeper.create(nodeName, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
        } catch (KeeperException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public boolean deleteNode(String nodeName) {
        try {
            zooKeeper.delete(nodeName, -1);
            return true;
        } catch (InterruptedException | KeeperException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
}
