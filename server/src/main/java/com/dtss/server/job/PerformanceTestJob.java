package com.dtss.server.job;

import com.dtss.client.core.AbstractJob;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author LuYun
 * @since 2018.04.17
 */
@Component
public class PerformanceTestJob extends AbstractJob {

    @Override
    public String execute(String param) {

        while (!isTerminated()) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
    }
}
