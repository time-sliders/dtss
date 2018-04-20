package com.dtss.client.core.zk.watcher;

import org.apache.zookeeper.Watcher;

/**
 * @author LuYun
 * @since 2018.04.10
 */
public abstract class AbstractPathWatcher implements Watcher {

    private String path;

    public AbstractPathWatcher(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractPathWatcher that = (AbstractPathWatcher) o;

        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
