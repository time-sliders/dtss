package com.dtss.client.core.zk.watcher;

import org.apache.zookeeper.Watcher;

/**
 * @author LuYun
 * @since 2018.04.10
 */
public abstract class AbstractAppWatcher implements Watcher {

    protected String app;

    public AbstractAppWatcher(String app) {
        this.app = app;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractAppWatcher that = (AbstractAppWatcher) o;

        return app.equals(that.app);
    }

    @Override
    public int hashCode() {
        return app.hashCode();
    }
}
