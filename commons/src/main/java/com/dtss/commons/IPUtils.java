package com.dtss.commons;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import java.net.*;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * IP工具类
 */
public class IPUtils {

    private static Logger logger = Logger.getLogger(IPUtils.class);

    public static final String IP4_LOCAL_BACK_LOOP_IP = "127.0.0.1";
    public static final String IP6_LOCAL_BACK_LOOP_IP = "0:0:0:0:0:0:0:1";

    /**
     * 验证传入的IPv4地址是否在指定的IP区域段内部
     */
    public static boolean checkIPv4IpIn(String startIp, String endIp, String checkIp) {

        if (!checkIsIPv4s(startIp, endIp, checkIp)) {
            return false;
        }

        long start = getIpNum(startIp);
        long end = getIpNum(endIp);
        long check = getIpNum(checkIp);

        return check <= end && check >= start;
    }

    /**
     * 将IP地址中的每一个数字左填充为3位数字如 168.3.4.144 => 168003004144
     */
    public static long getIpNum(String ip) {

        String[] values = StringUtils.split(ip, "[.]");

        StringBuilder ipNumStr = new StringBuilder();

        for (String block : values) {
            String blockNum = StringUtils.leftPad(block, 3, "0");
            ipNumStr.append(blockNum);
        }

        if (ipNumStr.length() > 0 && NumberUtils.isNumber(ipNumStr.toString())) {
            return NumberUtils.toLong(ipNumStr.toString());
        }

        return 0;
    }

    /**
     * 校验传入的IPv4中是否全部都是正确的IP地址
     */
    public static boolean checkIsIPv4s(String... ips) {

        if (ips == null || ips.length <= 0) {
            return false;
        }

        for (String ip : ips) {
            if (!checkIsIPv4(ip)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 校验传入的IPv4是否是正确的IP地址
     */
    public static boolean checkIsIPv4(String ip) {

        return !StringUtils.isBlank(ip) && Pattern.matches(Regex.IPV4_REGEX, ip);

    }

    /**
     * 获取本机IP<br/>
     * 本地回环[127.0.0.1] 不会作为返回结果
     */
    public static String getLocalIp() {
        Enumeration<NetworkInterface> nis;
        try {
            nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    InetAddress ia = ias.nextElement();
                    if (ia instanceof Inet4Address
                            && !IP4_LOCAL_BACK_LOOP_IP.equals(ia.getHostAddress())) {
                        return ia.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

}
