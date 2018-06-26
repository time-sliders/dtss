package com.dtss.server.core.job;

import com.dtss.server.core.zk.LeaderLatch;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 卢云(luyun)
 * @version 铜子集合标
 * @since 2018.03.22
 */
public class JobIdGenerator {

    private static Map<String, String> ipToIdMap = new HashMap<String, String>();

    static {
        ipToIdMap.put("192.168", "0001");
    }

    private static final String DEFAULT_IP_VALUE = "0000";
    private static final String BACKUP_9_VALUE = "100000000";

    public static String generateJobId() {
        String localIp = LeaderLatch.LOCAL_IP;
        String ip4 = ipTo12Str(localIp);
        String tss19 = TimeStampSequence.getTimeStampSequence();
        return BACKUP_9_VALUE + ip4 + tss19;
    }

    private static String ipTo12Str(String ip) {
        for (Map.Entry<String, String> entry : ipToIdMap.entrySet()) {
            if (ip.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return DEFAULT_IP_VALUE;
    }
}