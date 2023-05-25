package com.trans.tcpclient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author Tom灿
 * @description: TCP通信客户端 要求在同一局域网下(同一网段)
 * @date :2023/5/25 9:24
 */
public class SocketClient {
    private static String TAG = "SocketClient";
    private static String hostname = "172.19.250.161"; // 服务器IP
    private static int port = 12345; // 端口
    private static Socket socket;
    static Runnable net = new Runnable() {
        @Override
        public void run() {
            try {
                //socket=new Socket("192.168.1.102", 12345);//注意这里
                socket = new Socket();
                Log.e(TAG, "启动客户端");
                SocketAddress socAddress = new InetSocketAddress(hostname, port);
                socket.connect(socAddress, 3000);//超时3秒
                Log.e(TAG, "客户端连接成功（超时3秒）");
                // 监听服务端
                getServiceData();
                // 发送数据到客户端
                sendACKData(); // 代码不执行
            } catch (Exception e) {
                Log.e(TAG, "链接错误:" + e);
                e.printStackTrace();
            }
        }
    };

    /**
     * 监听服务端请求
     */
    private static void getServiceData() {
        InputStream inputStream = null;
        try {
//            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            inputStream = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = inputStream.read(buffer)) != -1) {
                String data = new String(buffer, 0, len);
                Log.e(TAG, "收到服务端数据:" + data);
            }
            Log.e(TAG, "客户端断开连接");
//            pw.close();
        } catch (IOException e) {
            Log.e(TAG, "接收服务端数据错误:" + e);
        }
    }

    /**
     * 发送数据到服务端
     *
     * @param msg
     */
    public static void sendDataToService(String msg) {
        if (socket != null && socket.isConnected()) {
            new Thread(() -> {
                try {
                    socket.getOutputStream().write(msg.getBytes());
                    socket.getOutputStream().flush();
                    Log.e(TAG, "发送数据给服务端");
                } catch (IOException e) {
                    Log.e(TAG, "发送数据错误:" + e);
                    e.printStackTrace();
                }
            }).start();
        } else {
            Log.e(TAG, "发送数据错误:连接失败");
        }
    }

    /**
     * 发送回执数据
     */
    private static void sendACKData() {
        //发送给服务端的消息
        String msg = "Hello,我来自客户端(ACK)";
        try {
            Log.e(TAG, "发送回执数据......");
            //获取输出流并实例化
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream()));
            out.write(msg + "\n");//防止粘包
            out.flush();//不加这个flush会怎样？
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "发送回执数据错误:" + e);
        } finally {
            //关闭Socket
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "关闭客户端错误:" + e);
            }
            Log.e(TAG, "客户端关闭");
        }
    }


    /**
     * 获取IP地址
     *
     * @param context
     * @return
     */
    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
            Log.e(TAG, "无网络，请先连接网络");
        }
        return null;
    }


    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    private static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

}
