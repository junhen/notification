package com.xin;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * Created by xiaoxin on 17-4-24.
 */

public class InetAddressTest {
    public static void main(String[] args) throws Exception{
        //获取本机InetAddress的实例：
        InetAddress address = InetAddress.getLocalHost();
        System.out.println("本机名：" + address.getHostName());
        System.out.println("CanonicalHostName：" + address.getCanonicalHostName());
        System.out.println("IP地址：" + address.getHostAddress());
        byte[] bytes = address.getAddress();
        System.out.println("字节数组形式的IP地址：" + Arrays.toString(bytes));
        System.out.println("直接输出InetAddress对象：" + address);

        System.out.println("\n------------InetAddressTest().printAddress begin-----------");
        new InetAddressTest().printAddress();
        System.out.println("------------InetAddressTest().printAddress end-----------\n");
        System.out.println("baidu：" + address.getAllByName("www.baidu.com")[0].getHostName());
    }

    public void printAddress() throws IOException {
        for (Enumeration<NetworkInterface> en = NetworkInterface
                .getNetworkInterfaces(); en.hasMoreElements();) {
            System.out.println("    ++++++++++++++++++++++");
            NetworkInterface intf = en.nextElement();

            for (Enumeration<InetAddress> enumIpAddr = intf
                    .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                System.out.println("==========================");
                InetAddress inetAddress = enumIpAddr.nextElement();
                System.out.println("inetAddress：" + inetAddress);

                if (!inetAddress.isLoopbackAddress()) {
                    if (inetAddress.isReachable(1000) /*&& InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())*/) {
                        String localIp = inetAddress.getHostAddress();
                        System.out.println("IP地址：" + localIp);
                        byte[] localIpBytes = inetAddress.getAddress();
                        System.out.println("字节数组形式的IP地址：" + Arrays.toString(localIpBytes));
                        /*app.me.ipAddress = localIp;
                        Log.i("msg","自己ip="+localIp);
                        System.arraycopy(localIpBytes, 0, regBuffer, 44, 4);*/
                    }
                }
            }
        }
    }
}
