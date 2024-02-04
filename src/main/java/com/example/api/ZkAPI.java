package com.example.api;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ZkAPI {

    ZooKeeper zooKeeper;

    @Before
    public void init() throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String connectStr =  "172.16.190.134:2181,172.16.190.135:2181,172.16.190.136:2181";
        int sessionTimeOut = 5000;
        zooKeeper = new ZooKeeper(connectStr, sessionTimeOut, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                switch (watchedEvent.getType()) {
                    case None:
                        if (watchedEvent.getState().equals(Event.KeeperState.SyncConnected)) { // connect success
                            latch.countDown();
                        }
                        break;
                    case NodeCreated:
                        System.out.println("Node created!");
                        break;
                    case NodeDeleted:
                        System.out.println("Node deleted!");
                    case NodeChildrenChanged:
                        System.out.println("Child node created!");
                }
            }
        });
        System.out.println(zooKeeper);
        latch.await();
        System.out.println(zooKeeper);
    }

    @Test
    public void createNode() throws InterruptedException, KeeperException {
        zooKeeper.create("/ApiNode1", "ApiContent1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println("In createNode(): Node create success.");
    }

    @Test
    public void asyncCreateNode() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AsyncCallback.StringCallback stringCallback = new AsyncCallback.StringCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, String name) {
                System.out.printf("rc = %d, path = %s, ctx = %s, name = %s\n", rc, path, ctx, name);
                switch (rc) {
                    case 0:
                        System.out.println("Node create success");
                        break;
                    case 4:
                        System.out.println("Client and server connection closed");
                        break;
                    case -110:
                        System.out.println("The node is already exists");
                        break;
                    case -112:
                        System.out.println("Session expires");
                        break;
                }
                latch.countDown();
            }
        };
        zooKeeper.create("/ApiNode2/test", "ApiContent2".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL, stringCallback, "ctx");
        latch.await();
    }

    @Test
    public void deleteNode() throws InterruptedException, KeeperException {
        zooKeeper.delete("/ApiNode1", 1);
    }

    @Test
    public void asyncDeleteNode() throws InterruptedException, KeeperException {
        CountDownLatch latch = new CountDownLatch(1);
        AsyncCallback.VoidCallback deleteCallBack = new AsyncCallback.VoidCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx) {
                System.out.printf("rc = %d, path = %s, ctx = %s\n", rc, path, ctx);
                switch (rc) {
                    case 0:
                        System.out.println("Node delete success");
                        break;
                    case -4:
                        System.out.println("Client and server connection closed");
                        break;
                    case -101:
                        System.out.println("The node not exists");
                        break;
                    case -111:
                        System.out.println("The node is not empty");
                        break;
                    case -112:
                        System.out.println("Session is expires");
                        break;
                }
                latch.countDown();
            }
        };
        zooKeeper.delete("/test", -1, deleteCallBack, "ctx");
        latch.await();
    }

    @Test
    public void setNode() throws InterruptedException, KeeperException {
        zooKeeper.setData("/ApiNode2", "Good Programmer".getBytes(), -1);
    }

    @Test
    public void asyncSetNode() throws InterruptedException, KeeperException {
        CountDownLatch latch = new CountDownLatch(1);
        AsyncCallback.StatCallback setCallBack = new AsyncCallback.StatCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, Stat stat) {
                System.out.printf("rc = %d, path = %s, ctx = %s\n", rc, path, ctx);
                if (rc == 0) {
                    System.out.println("Node set success");
                } else {
                    System.out.println("Node set failed");
                }
                latch.countDown();
            }
        };
        zooKeeper.setData("/ApiNode2", "Big Data".getBytes(), -1, setCallBack, "");
        latch.await();
    }

    @Test
    public void getNode() throws InterruptedException, KeeperException {
        Stat stat = new Stat();
        byte[] data = zooKeeper.getData("/ApiNode2", true, stat);
        System.out.println("Data: " + new String(data));
        System.out.println(stat);
        System.out.println(stat.getVersion());
        System.out.println(stat.getCtime());
    }

    @Test
    public void asyncGetNode() throws InterruptedException, KeeperException {
        CountDownLatch latch = new CountDownLatch(1);
        AsyncCallback.DataCallback dataCallback = new AsyncCallback.DataCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                System.out.printf("rc = %d, path = %s, ctx = %s\n", rc, path, ctx);
                if (rc == 0) {
                    System.out.println("Data: " + new String(data));
                    System.out.println("Stat: " + stat);
                } else {
                    System.out.println("Node get failed");
                }
                latch.countDown();
            }
        };
        zooKeeper.getData("/ApiNode2", true, dataCallback, "");
        latch.await();
    }

    @Test
    public void getChildren() throws InterruptedException, KeeperException {
        List<String> children = zooKeeper.getChildren("/", true);
        System.out.println(children);
    }


    @Test
    public void asyncGetChildren() throws InterruptedException, KeeperException {
        CountDownLatch latch = new CountDownLatch(1);
        AsyncCallback.ChildrenCallback childrenCallback = new AsyncCallback.ChildrenCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, List<String> children) {
                System.out.printf("rc = %d, path = %s, ctx = %s\n", rc, path, ctx);
                if (rc == 0) {
                    System.out.println(children);
                } else {
                    System.out.println("Node children failed");
                }
                latch.countDown();
            }
        };
        zooKeeper.getChildren("/", true, childrenCallback, "");
        latch.await();
    }
}
