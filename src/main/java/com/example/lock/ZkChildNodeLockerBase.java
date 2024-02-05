package com.example.lock;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.util.Collections;
import java.util.List;

public abstract class ZkChildNodeLockerBase extends ZkLockerBase implements ZkLocker {

    protected String nodeFullPath;

    public ZkChildNodeLockerBase(String connectString, String lockName) {
        super(connectString);
        this.lockName = lockName;
        createLockNode();
    }

    public ZkChildNodeLockerBase(String lockName) {
        super();
        this.lockName = lockName;
        createLockNode();
    }

    private void createLockNode() {
        if (!exists(rootNodeName + "/" + lockName)) {
            createNode(rootNodeName + "/" + lockName, CreateMode.CONTAINER);
        }
    }

    @Override
    public boolean lock() {
        nodeFullPath = createNode(rootNodeName + "/" + lockName + "/child", CreateMode.EPHEMERAL_SEQUENTIAL);
        return canLock();
    }

    public abstract boolean canLock();

    @Override
    public boolean unlock() {
        return deleteNode(nodeFullPath);
    }

    @Override
    public boolean exists() {
        try {
            List<String> children = zooKeeper.getChildren(rootNodeName + "/" + lockName, false);
            return !children.isEmpty();
        } catch (KeeperException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    protected String getPreviousNode() {
        String previousNodeName = null;
        try {
            List<String> children = zooKeeper.getChildren(rootNodeName + "/" + lockName, false);

            Collections.sort(children);

            String childNodeName = nodeFullPath.substring((rootNodeName + "/" + lockName).length() + 1);

            for (String child : children) {
                if (child.equalsIgnoreCase(childNodeName)) {
                    break;
                }
                previousNodeName = child;
            }
        } catch (KeeperException | InterruptedException ignore) {}
        return previousNodeName;
    }
}
