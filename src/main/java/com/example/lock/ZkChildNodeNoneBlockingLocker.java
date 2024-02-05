package com.example.lock;

public class ZkChildNodeNoneBlockingLocker extends ZkChildNodeLockerBase {

    public ZkChildNodeNoneBlockingLocker(String connectString, String lockName) {
        super(connectString, lockName);
    }

    public ZkChildNodeNoneBlockingLocker(String lockName) {
        super(lockName);
    }

    @Override
    public boolean canLock() {
        String previousNode = getPreviousNode();
        if (previousNode == null) {
            return true;
        }
        deleteNode(nodeFullPath);
        return false;
    }
}
