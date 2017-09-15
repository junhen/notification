package com.xin;

/**
 * Created by xiaoxin on 17-4-25.
 */

import java.net.*;
import java.io.*;

public class CheckPort {
    public static void main(String[] args) {
        Socket skt;
        String host = "localhost";
        if (args.length > 0)
            host = args[0];
        for (int i = 0; i < 1024; i++) {
            try {
                //System.out.println("查看 "+ i);
                skt = new Socket(host, i);
                System.out.println("端口 " + i + " 已被使用");
            } catch (UnknownHostException e) {
                System.out.println("UnknownHostException occured"+ e);
                break;
            } catch (IOException e) {
                //System.out.println("IOException occured"+ e);
            } finally {
                skt = null;
            }
        }
    }
}

