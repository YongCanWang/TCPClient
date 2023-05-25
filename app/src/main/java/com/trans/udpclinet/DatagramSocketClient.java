package com.trans.udpclinet;

import android.util.Log;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * @author Tom灿
 * @description: UDP通信客户端 要求在同一局域网下(同一网段)
 * @date :2023/5/25 14:10
 */
public class DatagramSocketClient {
    private static final String TAG = "DatagramSocketClient";
    private static String ip = "172.19.250.161"; // IP
    private static int port = 56789; // 端口

    private static final int TIMEOUT = 3000;  // 阻塞时长
    private static final int RETRY_NUM = 5000;
    private static final String data = "helle,我是UDP数据,我来自客户端";
    public static Runnable net = new Runnable() {
        @Override
        public void run() {
            sendMessage(data);
        }
    };


    /**
     * 发送udp消息
     *
     * @param data 数据
     */
    public static void sendMessage(String data) {
        try {
            Log.e(TAG, "start");
            InetAddress inetAddress = InetAddress.getByName(ip);  // 设置IP
            byte[] dataBytes = data.getBytes();
            DatagramPacket dataPacket = new DatagramPacket(dataBytes, dataBytes.length,
                    inetAddress, port); // 数据包: 设置ip、端口、数据
            Log.e(TAG, "创建数据包:ip、端口、数据");
            DatagramSocket client = new DatagramSocket(); // Socket对象
            Log.e(TAG, "创建Socket对象");
            // 设置接收数据时阻塞的最长时间
            client.setSoTimeout(TIMEOUT);
            // 响应数据包
            byte[] responseBytes = new byte[1024 * 1024];
            DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);
            int tries = 0;
            boolean receivedResponse = false;
            while (!receivedResponse && tries < RETRY_NUM) {
                // 发送数据
                client.send(dataPacket);
                Log.e(TAG, "发送数据成功");
                try {
                    // 响应数据
                    client.receive(responsePacket);
                    String serviceIp = responsePacket.getAddress().getHostAddress();
                    String response = new String(responseBytes, 0, responsePacket.getLength());
                    Log.e(TAG, "接收响应数据成功:" + response);
                    receivedResponse = true;
                } catch (InterruptedIOException e) {
                    // 如果接收数据时阻塞超时，重发并减少一次重发的次数
                    tries += 1;
                    Log.e(TAG, "接收响应数据超时:" + e);
                }
            }
            client.close();
            Log.e(TAG, "断开连接");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "发送数据失败: " + e);
        }
    }


}
