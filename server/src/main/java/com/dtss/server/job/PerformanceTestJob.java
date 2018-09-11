package com.dtss.server.job;

import com.dtss.client.core.AbstractJob;
import org.springframework.stereotype.Component;

/**
 * @author LuYun
 * @since 2018.04.17
 */
@Component
public class PerformanceTestJob extends AbstractJob {

    @Override
    public String execute(String param) {
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "SUCCESS";
    }

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
    }
}
