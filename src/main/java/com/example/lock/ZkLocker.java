package com.example.lock;

public interface ZkLocker {
    boolean lock();
    boolean unlock();
    boolean exists();
}
