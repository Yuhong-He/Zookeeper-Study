package com.example.lock;

import org.apache.zookeeper.CreateMode;

public class ZkNodeNoneBlockingLock extends ZkLockerBase implements ZkLocker {

    public ZkNodeNoneBlockingLock(String lockName) {
        super();
        this.lockName = lockName;
    }

    public ZkNodeNoneBlockingLock(String connectString, String lockName) {
        super(connectString);
        this.lockName = lockName;
    }

    @Override
    public boolean lock() {
        return createNode(rootNodeName + "/" + lockName, CreateMode.PERSISTENT) != null;
    }

    @Override
    public boolean unlock() {
        return deleteNode(rootNodeName + "/" + lockName);
    }

    @Override
    public boolean exists() {
        return exists(rootNodeName + "/" + lockName);
    }
}
