package com.trans.libnet.tcpclient;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * @author Tom灿
 * @description: TCP通信客户端 要求在同一局域网下(同一网段)
 * @date :2023/5/25 9:24
 */
public class SocketClient {
    private static final String TAG = "SocketClient";
    //        private static String hostname = "172.19.250.161"; // 手机服务器IP
    private static String hostname = "192.168.10.123"; // obu设备IP
    //    private static String hostname = "172.19.250.13"; // 网络调试助手IP
    //    private static int port = 12345; // 手机服务器端口
    private static int port = 7130; // obu设备端口
    private static String endSymbol = "\0"; // 数据结束符
    private static int timeout = 5000; // 连接超时时间
    private static Socket socket;
    public static OnServiceDataListener onServiceDataListener;
    public static final Gson gson = new Gson();
    private static boolean isSubPackage = false;
    private static final StringBuilder stringBuilder = new StringBuilder(); // 高效处理分包数据
    private static Thread thread;
    private static final Runnable net = new Runnable() {
        @Override
        public void run() {
            try {
                //socket=new Socket("192.168.1.102", 12345);//注意这里
                Log.e(TAG, "Init");
                socket = new Socket();
                Log.e(TAG, "start");
                SocketAddress socAddress = new InetSocketAddress(hostname, port);
                Log.e(TAG, "启动客户端:正在与服务器(" + hostname + ")建立连接......");
                socket.connect(socAddress, timeout);//超时5秒
                Log.e(TAG, "连接服务器成功(超时值" + timeout + "ms)");  // 连接服务器成功，并进入阻塞状态...
                handler.sendEmptyMessage(10001);
                // 监听服务端
                getServiceData();
                // 发送数据到客户端
//                sendACKData(); // 代码不执行
            } catch (Exception e) {
                Log.e(TAG, "连接服务器错误:" + e);
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
            Log.e(TAG, "监听服务器(" + hostname + "):" + port + "端口......");
//            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            inputStream = socket.getInputStream();
            byte[] buffer = new byte[1024 * 2];
            int len = -1;
//            String datas = "";
            while ((len = inputStream.read(buffer)) != -1) {
                handlerData2(buffer, len);
            }
//            Log.e(TAG, "datas:" + datas);
            Log.e(TAG, "客户端-服务器: 断开连接");
//            pw.close();
        } catch (IOException e) {
            Log.e(TAG, "接收服务端数据错误:" + e);
            handler.sendMessage(handler.obtainMessage(10004, e.toString()));
        } finally {
            Log.e(TAG, "end");
            handler.sendEmptyMessage(10003);
        }
    }

    /**
     * 通过是否是json
     *
     * @param buffer
     * @param len
     */
    private static void handlerData(byte[] buffer, int len) {
        String data = new String(buffer, 0, len).trim();
        Log.e(TAG, "收到服务端数据:" + data);
        if (isJson(data) && !isSubPackage) {
            handler.sendMessage(handler.obtainMessage(10002, data.trim()));
        } else { // 分包处理
            isSubPackage = true;
//                    datas += data;
            stringBuilder.append(data);
            /**
             *  每次都去判断一下分包数据是否接收完毕
             *  判断依据为: 通过是否接收了一个完整的json格式数据判断，
             *  当拼接成一个完整的json格式数据的时候，说明分包数据已发送完毕。
             */
            String jsonData = stringBuilder.toString();
            if (isJson(jsonData)) {
                isSubPackage = false;
                handler.sendMessage(handler.obtainMessage(10002, jsonData.trim()));
//                        datas = "";
                stringBuilder.delete(0, stringBuilder.length());
            }
        }

    }

    /**
     * 通过结束符 /n 处理数据的分包、粘包
     *
     * @param buffer
     * @param len
     */
    private static void handlerData2(byte[] buffer, int len) {
        String data = new String(buffer, 0, len);
        Log.e(TAG, "收到服务端数据:" + data);
        boolean contains = data.contains(endSymbol);
        if (contains) {
            String[] split = data.split(endSymbol);
            if (split.length == 0) { // 分包处理
                stringBuilder.append(data.trim());
            }

            if (split.length == 1) { // 完整一条数据
                stringBuilder.append(split[0].trim());
                String dataJson = stringBuilder.toString().trim();
                if (isJson(dataJson)) {
                    handler.sendMessage(handler.obtainMessage(10002, dataJson));
                } else {
                    Log.e(TAG, "handlerData2: 数据丢失:" + dataJson.length());
                }
                stringBuilder.delete(0, stringBuilder.length());
            }

            if (split.length >= 2) {  // 分包处理 + 分包中带粘包
                // 处理第一条数据
                if (stringBuilder.length() == 0) {  // 完整数据
                    if (split.length != 0) {
                        String dataJson = split[0].trim();
                        if (isJson(dataJson)) {
                            handler.sendMessage(handler.obtainMessage(10002, dataJson));
                        } else {
                            Log.e(TAG, "handlerData2: 数据丢失:" + dataJson.length());
                        }
                        handler.sendMessage(handler.obtainMessage(10002, dataJson));
                    }
                } else {
                    stringBuilder.append(split[0]); // 被分包
                    String dataJson = stringBuilder.toString().trim();
                    if (isJson(dataJson)) {
                        handler.sendMessage(handler.obtainMessage(10002, dataJson));
                    } else {
                        Log.e(TAG, "handlerData2: 数据丢失:" + dataJson.length());
                    }
                    stringBuilder.delete(0, stringBuilder.length());
                }

                // 处理粘包数据
                for (int i = 1; i < split.length - 2; i++) {
                    String dataJson = split[i].trim();
                    if (isJson(dataJson)) {
                        handler.sendMessage(handler.obtainMessage(10002, dataJson));
                    } else {
                        Log.e(TAG, "handlerData2: 数据丢失:" + dataJson.length());
                    }
                }

                // 处理最后一条数据
                // 最后一条数据是否出现粘包
                if (String.valueOf(data.charAt(data.length() - 1)).equals(endSymbol)) {  // 完整数据
                    String dataJson = split[split.length - 1].trim();
                    if (isJson(dataJson)) {
                        handler.sendMessage(handler.obtainMessage(10002, dataJson));
                    } else {
                        Log.e(TAG, "handlerData2: 数据丢失:" + dataJson.length());
                    }
                } else {// 出现粘包
                    stringBuilder.append(data.trim());
                }
            }
        } else {
            stringBuilder.append(data);
        }

    }


    public static boolean isSubPackage(String data) {
        return !String.valueOf(data.charAt(data.length())).equals(endSymbol);
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
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
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


    @SuppressLint("HandlerLeak")
    private static final Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 10001: // 连接成功
                    if (onServiceDataListener != null) onServiceDataListener.connect();
                    break;

                case 10002: // 收到数据
                    if (onServiceDataListener != null)
                        onServiceDataListener.receive((String) msg.obj);
                    break;

                case 10003: // 断开连接
                    if (onServiceDataListener != null) onServiceDataListener.offline();
                    break;

                case 10004: // 接收异常
                    if (onServiceDataListener != null)
                        onServiceDataListener.error((String) msg.obj);
                    break;
            }
        }
    };

    public static void connect() {
        if (thread == null) {
            thread = new Thread(SocketClient.net);
            thread.setPriority(10);
            thread.start();
        } else {
            if (!thread.isAlive()) thread.start();
        }

    }


    public interface OnServiceDataListener {
        void connect();

        void receive(String data);

        void offline();

        void error(String e);

    }


    /**
     * 获取IP地址
     *
     * @param context
     * @return
     */
    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
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
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + (ip >> 24 & 0xFF);
    }

    /**
     * 获取OBU数据类型
     *
     * @param dataKey
     * @return
     */
    public static String getOBUType(String dataKey) {
        try {

            JSONObject jsonObject = new JSONObject(dataKey);
//            JSONArray jsonArray = new JSONArray(dataKey);
//            JSONObject base = jsonObject.getJSONObject("base");
            Iterator<String> keys = jsonObject.keys();
            String next = keys.next();
            jsonObject = null;
            keys = null;
            Log.e(TAG, "getOBUType: " + next); // RSM

//            JsonParser jsonParser = new JsonParser();
//            JsonElement parse = jsonParser.parse(dataKey);
//            Set<String> strings1 = parse.getAsJsonObject().keySet();
//            Log.e(TAG, "getOBUType: " + strings1);  //[RSM]
//            if (parse.isJsonNull() || !parse.isJsonObject()) {
//                Log.e(TAG, "not json data");
//                return "not json data";
//            }

//            JsonObject jsonObject2 = new JsonObject();
//            jsonObject2.add("jsonKey", parse);
//            Set<String> strings2 = jsonObject2.keySet();
//            String next1 = strings2.iterator().next();
//            Log.e(TAG, "getOBUType: " + next1); // jsonKey
//
//            Map<String, JsonElement> stringJsonElementMap = jsonObject2.asMap();
//            Set<String> strings = stringJsonElementMap.keySet();
//            String next2 = strings.iterator().next();
//            Log.e(TAG, "getOBUType: " + next2); // jsonKey

            return next;
        } catch (JSONException e) {
            Log.e(TAG, "getOBUType:获取OBU数据类型错误 ---> JSONException:" + e);
            e.printStackTrace();
        }
        return "";
    }

    private static final JsonParser jsonParser = new JsonParser();

    /**
     * 判断是否为Json格式数据
     *
     * @param json
     * @return
     */
    private static boolean isJson(String json) {
        try {
            jsonParser.parse(json);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void setHostname(String ip) {
        SocketClient.hostname = ip;
    }

    public static void setPort(int port) {
        SocketClient.port = port;
    }

}
