package com.trans.libnet.tcpclient;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import java.text.SimpleDateFormat;
import java.util.Date;
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
    private static int timeout = 3000; // 连接超时时间
    private static Socket socket;
    private static OnServiceDataListener onServiceDataListener;
    public  static final Gson gson = new Gson();
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
    private static final Runnable net = new Runnable() {
        @Override
        public void run() {
            try {
                handler.sendEmptyMessage(10007);
                lifecycleStatus = LifecycleStatus.Running;
                //socket=new Socket("192.168.1.102", 12345);//注意这里
                Log.e(TAG, "Init");
                socket = new Socket();
                Log.e(TAG, "Start");
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
                handler.sendMessage(handler.obtainMessage(10005, e));
                e.printStackTrace();
                // 重新连接服务器
                reconnection();
            }
        }
    };


    /**
     * 监听服务端请求
     */
    private static synchronized void getServiceData() {
        InputStream inputStream;
        try {
            Log.e(TAG, "正在监听服务器(" + hostname + "):" + port + "端口......");
//            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            inputStream = socket.getInputStream();
            byte[] buffer = new byte[1024 * 2];
            int len = -1;
//            String datas = "";
            while ((len = inputStream.read(buffer)) != -1) {
//                if (System.currentTimeMillis() - lastTime < hz
//                        && stringBuilder.length() == 0) {   // TODO 有可能过滤掉重要数据,比如解除预警数据
//                    Log.e(TAG, "帧率过快(" + hz + "),数据被过滤:" +  new String(buffer, 0, len));
//                    continue;
//                }

                handlerData2(buffer, len);
                lastTime = System.currentTimeMillis();

                /**
                 *  TODO 数据放到队列去处理 (数据排队处理,是否还需要做过滤处理?)
                 *  TODO 数据放入了消息队列,handlerData2方法使用synchronized修饰,方法不执行
                 *  TODO 通过Handler发送到队列中,处理逻辑在主线程执行
                 */
//                 handler.sendMessage(handler.obtainMessage(10006,
//                        new String(buffer, 0, len)));
            }
//            Log.e(TAG, "datas:" + datas);
            Log.e(TAG, "客户端-服务器: 断开连接");
//            pw.close();
        } catch (IOException e) {
            Log.e(TAG, "接收服务端数据错误:" + e);
            handler.sendMessage(handler.obtainMessage(10004, e));
        } finally {
            Log.e(TAG, "End");
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                    bufferedWriter = null;
                } catch (IOException e) {
                    Log.e(TAG, "关闭io错误:" + e);
                    throw new RuntimeException(e);
                }
            }
            handler.sendEmptyMessage(10003);

            // 重新连接服务器
            reconnection();
        }
    }


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
                Log.e(TAG, "休眠线程出错:" + e);
            }
        } else {
            lifecycleStatus = LifecycleStatus.Terminated;
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
        writerLogInfo(data);
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

    /**
     * 数据信息写入本地
     *
     * @param data
     */
    private static void writerLogInfo(String data) {
        if (logEnabled && bufferedWriter != null) {
            try {
                date.setTime(System.currentTimeMillis());
                bufferedWriter.write(simpleDateFormat.format(date) + " ");
                bufferedWriter.write(data);
                bufferedWriter.write("\r\n");
                if (++bufferedIndex % diskWriteHz == 0) { // 避免高频率的访问本地磁盘，造成卡顿
                    bufferedWriter.flush();
                    bufferedIndex = 0;
                }
            } catch (IOException e) {
                Log.e(TAG, "写入日志错误:" + e);
                throw new RuntimeException(e);
            }
        }
    }


    private static boolean isSubPackage(String data) {
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

    public static void sendPathDataToService(String path) {
        File file = new File(path);
        if (file.exists()) {
            Log.e(TAG, "sendPathDataToService: 目标文件存在:" + path);
        } else {
            Log.e(TAG, "sendPathDataToService: 目标文件不存在:" + path);
        }
        if (socket != null && socket.isConnected()) {
            new Thread(() -> {
                try {
                    FileInputStream fileInputStream = new FileInputStream(path);
                    byte[] bytes = new byte[1024 / 2];
                    int readCount = 0;
                    while ((readCount = fileInputStream.read(bytes)) != -1) {
                        socket.getOutputStream().write(bytes, 0, readCount);
                        socket.getOutputStream().flush();
                        Log.e(TAG, "发送数据给服务端:" + new String(bytes, 0, readCount));
                    }
//                    socket.getOutputStream().write("END".getBytes());
//                    socket.getOutputStream().flush();
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
                        onServiceDataListener.error((IOException) msg.obj);
                    break;

                case 10005: // 连接失败
                    if (onServiceDataListener != null)
                        onServiceDataListener.connectionFail((Exception) msg.obj);
                    break;

                case 10006: // 数据处理
                    handlerData2((String) msg.obj);
                    lastTime = System.currentTimeMillis();
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
            thread = new Thread(SocketClient.net);
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

    private static void setHostname(String ip) {
        SocketClient.hostname = ip;
    }

    private static void setPort(int port) {
        SocketClient.port = port;
    }

    private static void setHz(int hz) {
        SocketClient.hz = hz;
    }


    /**
     * 是否开启本地日志记录: 需要动态申请本地读写权限
     *
     * @param enabled 本地日志路径:  /trans/record/communication_log.txt
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

            File communicationFile = new File(file, "communication_log.txt");
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
        SocketClient.isReconnection = isReconnection;
    }


    public static class Builder {

        public Builder hostname(String ip) {
            SocketClient.hostname = ip;
            return this;
        }

        public Builder port(int port) {
            SocketClient.port = port;
            return this;
        }

        public Builder hz(int hz) {
            SocketClient.hz = hz;
            return this;
        }

        public Builder reconnection(boolean isReconnection) {
            SocketClient.isReconnection = isReconnection;
            return this;
        }

        public Builder log(boolean enabled) {
            try {
                SocketClient.logEnabled(enabled);
            } catch (Exception e) {
                Log.e(TAG, "logEnabled:" + e);
                e.printStackTrace();
            }
            return this;
        }

        public Builder listener(OnServiceDataListener listener) {
            SocketClient.onServiceDataListener = listener;
            return this;
        }


        public void connect() {
            SocketClient.connect();
        }


        public void connect(OnServiceDataListener onServiceDataListener) {
            SocketClient.onServiceDataListener = onServiceDataListener;
            SocketClient.connect();
        }

        public void disconnect() {
            SocketClient.disconnect();
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

}
