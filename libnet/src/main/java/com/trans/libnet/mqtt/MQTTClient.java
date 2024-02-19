package com.trans.libnet.mqtt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

/**
 * @author Tom灿
 * @description: MQTT通信客户端 要求在同一局域网下(同一网段)
 * @date :2023/5/25 9:24
 */
public class MQTTClient {
    private static final String TAG = "MQTTClient";
    private static Context context;
    private static Prefix prefix;
    private static final String serverURI = "ssl://z8ce6691.ala.cn-hangzhou.emqxsl.cn:8883"; // 连接成功-emqx使用服务地址
    //    private final String serverURI = "tcp://broker.emqx.io:1883"; // 连接成功-emqx测试地址
//    private final String serverURI = "ssl://broker.emqx.io:8883"; // 连接成功-emqx测试地址
    private static String hostname = "z8ce6691.ala.cn-hangzhou.emqxsl.cn"; // emqx地址
    //    private static String hostname = "z8ce6691.ala.cn-hangzhou.emqxsl.cn"; // emqx地址
    private static int port = 8883; // emqx测试端口
    private static String userName = "emqx_admin";
    private static String password = "123456";
    private static String clientId = "android_client_test";
    private static String subTopic = "topic_android_test";
    private static String endSymbol = "\0"; // 数据结束符
    private static int timeout = 10000; // 连接超时时间
    private static OnServiceDataListener onServiceDataListener;
    public static final Gson gson = new Gson();
    private static boolean isSubPackage = false;
    private static final StringBuilder stringBuilder = new StringBuilder(); // 高效处理分包数据
    private static Thread thread;
    private static LifecycleStatus lifecycleStatus; // 线程的生命周期状态
    private static boolean isReconnection = false; // 断开连接后,是否自动重新连接
    private static long lastTime = System.currentTimeMillis();
    private static int hz = 10; //ms 最低接收处理数据的频率，小于该频率的数据，直接丢弃
    private static boolean logEnabled = false;  // 是否开启日志
    private static BufferedWriter bufferedWriter;
    private static int bufferedIndex = 0;
    private static int diskWriteHz = 20; // log日志本地磁盘写入频率
    private static long millis = 1000; // 1s后重新连接服务端口
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Date date = new Date();
    private static MqttAndroidClient mqttAndroidClient;
    private static MqttConnectOptions mqttConnectOptions;

    private static final Runnable net = new Runnable() {
        @Override
        public void run() {
            try {
                handler.sendEmptyMessage(10007);
                lifecycleStatus = LifecycleStatus.Running;
                Log.e(TAG, "Init");
                initMQTTClient(context);
                Log.e(TAG, "Start客户端:正在与MQTT Service(" + hostname + ")建立连接,超时值" + timeout + "ms......");
                connectMQTT();
            } catch (Exception e) {
                Log.e(TAG, "MQTT Service连接错误:" + e);
                handler.sendMessage(handler.obtainMessage(10005, e));
                e.printStackTrace();
                // 重新连接服务器
                reconnection();
            }
        }
    };


    /**
     * 重新连接服务器
     */
    private static void reconnection() {
        if (thread == null) return;
        // reconnection
        if (isReconnection) {
            try {
                Log.e(TAG, "1s后重新连接服务器......");
                lifecycleStatus = LifecycleStatus.Waiting;
                thread.sleep(millis);
                Log.e(TAG, "开始重新连接服务器");
                net.run();   // reconnection
            } catch (InterruptedException e) {
                Log.e(TAG, "休眠线程错误:" + e);
            }
        } else {
            lifecycleStatus = LifecycleStatus.Terminated;
        }
    }

    /**
     * MQTT初始化
     */
    private static void initMQTTClient(Context context) {
        try {
            if (mqttAndroidClient == null) {
                String pref = "ssl://";
                switch (prefix) {
                    case SSL:
                        pref = "ssl://";
                        break;
                    case TCP:
                        pref = "tcp://";
                        break;
                }
                mqttAndroidClient = new MqttAndroidClient(context,
                        pref + hostname + ":" + port, clientId);
                mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                    @Override
                    public void connectComplete(boolean reconnect, String serverURI) {
                        if (reconnect) {
                            Log.e(TAG, "MQTT Service 重新连接成功: serverURI:" + serverURI);
                        } else {
                            Log.e(TAG, "MQTT Service 连接成功: serverURI:" + serverURI);
                        }
//                        handler.sendEmptyMessage(10001);  //两个成功回调，去掉一个
                        // 订阅所有主题
                        subscribeAllTopics();
                    }

                    @Override
                    public void connectionLost(Throwable cause) {
                        Log.e(TAG, "MQTT Service 连接断开:" + cause);
                        handler.sendEmptyMessage(10003);
//                        mqttAndroidClient.close();
                        reconnection();
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) {
                        Log.e(TAG, "收到MQTT消息: topic:" + topic + "---message:" + message.toString() + "---qos:" + message.getQos());
                        handler.sendMessage(handler.obtainMessage(10002, message.toString().trim()));
//                        handlerData2(message.toString()); // TODO 暂时不处理数据
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        try {
                            MqttMessage mqttMessage = token.getMessage();
                            Log.e(TAG, "消息发送成功:" + mqttMessage.toString());
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }


            // 新建连接设置
            if (mqttConnectOptions == null) {
                mqttConnectOptions = new MqttConnectOptions();
                mqttConnectOptions.setUserName(userName);
                mqttConnectOptions.setPassword(password.toCharArray());
                //断开后，是否自动连接
                mqttConnectOptions.setAutomaticReconnect(true);
                //是否清空客户端的连接记录。若为true，则断开后，broker将自动清除该客户端连接信息
                mqttConnectOptions.setCleanSession(true);
                //设置超时时间，单位为秒
                mqttConnectOptions.setConnectionTimeout(timeout);
                //心跳时间，单位为秒。即多长时间确认一次Client端是否在线
                mqttConnectOptions.setKeepAliveInterval(30);
                //允许同时发送几条消息（未收到broker确认信息）
                mqttConnectOptions.setMaxInflight(30);
            }
        } catch (Exception e) {
            Log.e(TAG, "MQTT客户端初始化错误:" + e);
            e.printStackTrace();
        }
    }


    /**
     * 连接MQTT
     */
    private static void connectMQTT() {
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.e(TAG, "MQTT Service 连接成功！！！");
                    handler.sendEmptyMessage(10001);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "MQTT Service 连接失败:" + exception);
                    handler.sendMessage(handler.obtainMessage(10005, new Exception(exception.toString())));
                    reconnection();
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, "MQTT Service 连接错误:" + e);
            handler.sendMessage(handler.obtainMessage(10005, e));
            e.printStackTrace();
            reconnection();
        }
    }


    /**
     * 订阅所有主题
     */
    private static void subscribeAllTopics() {
        //订阅主消息主题和更新消息主题
        subscribeToTopic(subTopic, 2);
    }


    /**
     * 订阅一个主主题
     *
     * @param subTopic 主题名称
     */
    private static void subscribeToTopic(String subTopic, int qos) {
        try {
            mqttAndroidClient.subscribe(subTopic, qos, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.e(TAG, "MQTT订阅消息成功:" + subTopic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "MQTT订阅消息失败:" + subTopic);
                }
            });
        } catch (MqttException ex) {
            Log.e(TAG, "MQTT订阅消息错误:" + ex);
            ex.printStackTrace();
        }
    }


    /**
     * 发布主题
     *
     * @param topic 主题
     * @param msg   内容
     * @param qos   qos
     */
    public static void publishMessage(String topic, String msg, int qos) {
        if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
            try {
                Log.e(TAG, "发布主题:topic:" + topic + "---message:" + msg + "---qos:" + qos);
                mqttAndroidClient.publish(topic, msg.getBytes(), qos, false);
            } catch (Exception e) {
                Log.e(TAG, "发布主题错误:" + e);
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "发布主题失败！！！ MQTT Service 未连接！！！");
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
    private static synchronized void handlerData2(byte[] buffer, int len) {
        String data = new String(buffer, 0, len);
        handlerData2(data);
    }


    /**
     * 通过结束符 /n 处理数据的分包、粘包
     *
     * @param data
     */
    private static synchronized void handlerData2(String data) {
        Log.e(TAG, "收到服务端数据:" + data);
        boolean contains = data.contains(endSymbol);
        if (contains) {
            String[] split = data.split(endSymbol);
            Log.i(TAG, "粘包个数:" + split.length);
            if (split.length == 1) { // 一条数据
                String firstDtaJson = split[0].trim();
                if (isJson(firstDtaJson)) {  // 完整一条数据
                    handler.sendMessage(handler.obtainMessage(10002, firstDtaJson));
                } else { // 分包数据
                    stringBuilder.append(firstDtaJson);
                    // 拼接后，验证一下拼接数据是否拼接完成，是否为一条完整的json数据
                    String firstStringBuilder = stringBuilder.toString().trim();
                    if (isJson(firstStringBuilder)) {
                        handler.sendMessage(handler.obtainMessage(10002, firstStringBuilder));
                        stringBuilder.delete(0, stringBuilder.length());
                    }
                }
            } else if (split.length >= 2) {  // 分包处理 + 分包中带粘包
                // 处理第一条数据
                String dataJson1 = split[0].trim();
                if (isJson(dataJson1)) {  // 第一条数据是完整数据，直接发送
                    handler.sendMessage(handler.obtainMessage(10002, dataJson1));
                    stringBuilder.delete(0, stringBuilder.length());
                } else {  // 分包数据，进行数据拼接
                    stringBuilder.append(dataJson1);
                    // 拼接后，验证一下拼接数据是否拼接完成，是否为一条完整的json数据
                    String dataJson = stringBuilder.toString().trim();
                    if (isJson(dataJson)) {
                        handler.sendMessage(handler.obtainMessage(10002, dataJson));
                        stringBuilder.delete(0, stringBuilder.length());
                    }
                }

                // 处理粘包数据
                for (int i = 1; i < split.length - 2; i++) {
                    String dataJson = split[i].trim();
                    if (isJson(dataJson)) {
                        handler.sendMessage(handler.obtainMessage(10002, dataJson));
                    } else {
                        Log.e(TAG, "handlerData2: 数据不完整,已丢弃:" + dataJson);
                    }
                }

                // 处理最后一条数据
                // 最后一条数据是否出现粘包
                String endData = split[split.length - 1].trim();
                if (isJson(endData)) { // 最后一条数据为完整数据，直接发送
                    handler.sendMessage(handler.obtainMessage(10002, endData));
                } else { // 最后一条数据为粘包数据，进行数据拼接
                    stringBuilder.append(endData);
                    // 拼接后，验证一下拼接数据是否拼接完成，是否为一条完整的json数据
                    String endStringBuilder = stringBuilder.toString().trim();
                    if (isJson(endStringBuilder)) {
                        handler.sendMessage(handler.obtainMessage(10002, endStringBuilder));
                        stringBuilder.delete(0, stringBuilder.length());
                    }
                }
            }
        }
    }


    private static boolean isSubPackage(String data) {
        return !String.valueOf(data.charAt(data.length())).equals(endSymbol);
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
                        onServiceDataListener.error((IOException) msg.obj);
                    break;

                case 10005: // 连接失败
                    if (onServiceDataListener != null)
                        onServiceDataListener.connectionFail((Exception) msg.obj);
                    break;

                case 10006: // 数据处理
                    handlerData2((String) msg.obj);
//                    lastTime = System.currentTimeMillis();
                    break;

                case 10007: // 正在链接
                    if (onServiceDataListener != null)
                        onServiceDataListener.connecting();
                    break;
            }
        }
    };

    private static void connect() {
        if (thread == null) {
            thread = new Thread(MQTTClient.net);
            lifecycleStatus = LifecycleStatus.New;
            thread.setPriority(Thread.MAX_PRIORITY);
            lifecycleStatus = LifecycleStatus.Runnable;
            thread.start();
        } else {
            switch (lifecycleStatus) {
                case New:
                    break;
                case Runnable:
                    break;
                case Running:
                    break;
                case Waiting:
                    break;
                case Terminated:
                    thread = null;
                    connect();
                    break;
            }
        }
    }

    private static void disconnect() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
            Log.e(TAG, "disconnect: 中断线程");
        }
    }


    public interface OnServiceDataListener {
        void connect();

        void connecting();

        void receive(String data);

        void offline();

        void error(IOException e);

        void connectionFail(Exception e);

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

    private static void setHostname(String ip) {
        MQTTClient.hostname = ip;
    }

    private static void setPort(int port) {
        MQTTClient.port = port;
    }

    private static void setHz(int hz) {
        MQTTClient.hz = hz;
    }


    /**
     * 是否开启本地日志记录: 需要动态申请本地读写权限
     *
     * @param enabled 本地日志路径:  /trans/record/communication_mqtt_log.txt
     */
    private static void logEnabled(boolean enabled) {
        logEnabled = enabled;
        if (enabled && bufferedWriter == null) {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/trans/record");
            if (!file.exists()) {
                try {
                    file.mkdirs();
                } catch (Exception e) {
                    Log.e(TAG, "创建日志文件夹错误:" + e);
                    throw new RuntimeException(e);
                }
            }

            File communicationFile = new File(file, "communication_mqtt_log.txt");
            try {
                if (!communicationFile.exists()) {
                    communicationFile.createNewFile();
                }
            } catch (IOException e) {
                Log.e(TAG, "创建日志文件错误:" + e);
            }

            try {
                bufferedWriter = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(communicationFile, true)));
            } catch (FileNotFoundException e) {
                Log.e(TAG, "找不到日志文件:" + e);
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * 断开连接后,是否重新连接服务器
     *
     * @param isReconnection
     */
    private static void isReconnection(boolean isReconnection) {
        MQTTClient.isReconnection = isReconnection;
    }


    public static class Builder {

        public Builder cont(Context cont) {
            MQTTClient.context = cont;
            return this;
        }

        public Builder prefix(Prefix prefix) {
            MQTTClient.prefix = prefix;
            return this;
        }

        public Builder host(String host) {
            MQTTClient.hostname = host;
            return this;
        }

        public Builder port(int port) {
            MQTTClient.port = port;
            return this;
        }

        public Builder name(String name) {
            MQTTClient.userName = name;
            return this;
        }

        public Builder password(String pwd) {
            MQTTClient.password = pwd;
            return this;
        }

        public Builder id(String id) {
            MQTTClient.clientId = id;
            return this;
        }

        public Builder topic(String topic) {
            MQTTClient.subTopic = topic;
            return this;
        }

        public Builder hz(int hz) {
            MQTTClient.hz = hz;
            return this;
        }

        public Builder reconnection(boolean isReconnection) {
            MQTTClient.isReconnection = isReconnection;
            return this;
        }

        public Builder log(boolean enabled) {
            try {
                MQTTClient.logEnabled(enabled);
            } catch (Exception e) {
                Log.e(TAG, "logEnabled:" + e);
                e.printStackTrace();
            }
            return this;
        }

        public Builder listener(OnServiceDataListener listener) {
            MQTTClient.onServiceDataListener = listener;
            return this;
        }


        public void connect() {
            MQTTClient.connect();
        }


        public void connect(OnServiceDataListener onServiceDataListener) {
            MQTTClient.onServiceDataListener = onServiceDataListener;
            MQTTClient.connect();
        }

        public void disconnect() {
            MQTTClient.disconnect();
        }

    }


    /**
     * 线程的生命周期状态
     */
    enum LifecycleStatus {
        New,
        Runnable,
        Running,
        Blocked,
        Waiting,
        TimedWaiting,
        Terminated,

    }

   public enum Prefix {
        SSL,
        TCP
    }

}
