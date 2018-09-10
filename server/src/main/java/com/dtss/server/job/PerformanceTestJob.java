package com.dtss.server.job;

import com.dtss.client.core.AbstractJob;
import org.springframework.stereotype.Component;

import java.util.Random;

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
                Thread.sleep(new Random().nextInt(1000));
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
