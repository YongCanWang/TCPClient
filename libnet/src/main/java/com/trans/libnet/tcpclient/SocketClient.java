package com.trans.libnet.tcpclient;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.trans.libnet.utils.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Tom灿
 * @description: TCP通信客户端 要求在同一局域网下(同一网段)
 *               TCP网络传输协议特性
 *                          ①安全的（确认应答机制，重发控制）
 *                          ②有序的（顺序控制）
 *                          ③高效的（效率高）（流量控制&拥塞控制机制，提高网络利用率）
 * @mDate :2023/5/25 9:24
 */
public class SocketClient {
    private static final String TAG = "SocketClient";
    private String mHost = "192.168.10.123"; // obu设备IP
    private int mPort = 7130; // obu设备端口
    private String mEndSymbol = "\0"; // 数据结束符
    private int mTimeout = 3000; // 连接超时时间
    private Socket mSocket;
    private OnServiceDataListener mOnServiceDataListener;
    private boolean mIsSubPackage = false;
    private final StringBuilder stringBuilder = new StringBuilder(); // 高效处理分包数据
    private Lifecycle mLifecycle; // 线程的生命周期状态
    private boolean mIsDisconnect = false;
    private boolean mIsReconnection = false; // 断开连接后,是否自动重新连接
    private long mLastTime = System.currentTimeMillis();
    private int mHz = 10; //ms 最低接收处理数据的频率，小于该频率的数据，直接丢弃
    private boolean mLogEnabled = false;  // 是否开启日志
    private BufferedWriter mBufferedWriter;
    private InputStream mInputStream;
    private int mBufferedIndex = 0;
    private int mDiskWriteHz = 20; // log日志本地磁盘写入频率
    private long mMillis = 1000; // 1s后重新连接服务端口
    private String mDataHandlerThreadName = "data_handler_thread";
    private ExecutorService mConnectThread = Executors.newSingleThreadExecutor();
    private ExecutorService mDataHandlerThread = Executors.newSingleThreadExecutor();
    private ExecutorService mSendDataThread = Executors.newSingleThreadExecutor();
    private HandlerThread mDataDistributeThread;
    @SuppressLint("HandlerLeak")
    private Handler mDataDistributeHandler;
    private final Runnable mRun = () -> startConnect();

    SocketClient(Builder builder) {
        mHost = builder.mHost;
        mPort = builder.mPort;
        mHz = builder.mHz;
        mEndSymbol = builder.mEndSymbol;
        mOnServiceDataListener = builder.mOnServiceDataListener;
        mIsReconnection = builder.mIsReconnection;
        mMillis = builder.mMillis;
        mLogEnabled = builder.mLogEnabled;
        initLog();
    }

    /**
     * 开始链接server
     */
    private synchronized void startConnect() {
        try {
            mLifecycle = Lifecycle.Runnable;
            if (mOnServiceDataListener != null) mOnServiceDataListener.connecting();
            Log.d(TAG, "init");
            mSocket = new Socket();
            Log.d(TAG, "start");
            SocketAddress socAddress = new InetSocketAddress(mHost, mPort);
            Log.d(TAG, "启动client:正在与server("
                    + mHost + ")建立连接(超时值" + mTimeout + "ms)......");
            if (mIsDisconnect) {
                mLifecycle = Lifecycle.Terminated;
                Log.i(TAG, "task interrupt");
                return;
            }
            mSocket.connect(socAddress, mTimeout);//超时5秒
            mLifecycle = Lifecycle.Running;
            Log.d(TAG, "连接server成功");
            // 连接server成功，并进入阻塞状态...
            initHandlerThread();
            sendMes(10001);
            // 监听服务端
            listenerService();
        } catch (Exception e) {
            Log.e(TAG, "link server error: " + e);
            finishSocket();
            quitHandler(10005, e);
            // 重新连接server
            reconnection();
        }
    }

    /**
     * 监听server请求
     */
    private synchronized void listenerService() {
        try {
            Log.i(TAG, "正在监听server(" + mHost + "):" + mPort + "端口......");
//            PrintWriter pw = new PrintWriter(mSocket.getOutputStream());
            mInputStream = mSocket.getInputStream();
            byte[] buffer = new byte[1024 * 2];
            int len = -1; //数据长度
//            String datas = "";
            while (!mIsDisconnect && mInputStream != null
                    && (len = mInputStream.read(buffer)) != -1) {
                // TODO 有可能过滤掉重要数据,比如解除预警数据
//                if (System.currentTimeMillis() - mLastTime < mHz
//                        && stringBuilder.length() == 0) {
//                    Log.i(TAG, "帧率过快(" + mHz + "),数据被过滤:" +  new String(buffer, 0, len));
//                    continue;
//                }
                String data = new String(buffer, 0, len);
                if (!mDataHandlerThread.isShutdown()) {
                    mDataHandlerThread.execute(() -> {
                        handlerData(data);
//                        mLastTime = System.currentTimeMillis();
                    });
                }
                /**
                 *  TODO 数据放到队列去处理 (数据排队处理,是否还需要做过滤处理?)
                 *  TODO 数据放入了消息队列,handlerData方法使用synchronized修饰,方法不执行
                 *  TODO 通过Handler发送到队列中,处理逻辑在主线程执行
                 */
//                sendMes(10006, new String(buffer, 0, len));
            }
//            Log.i(TAG, "datas:" + datas);
            Log.i(TAG, "client server disconnect");
        } catch (IOException e) {
            if (mIsDisconnect) {
                Log.i(TAG, "client server disconnect");
            } else {
                Log.e(TAG, "get server data error:" + e);
                sendMes(10004, e);
            }
        } finally {
            Log.i(TAG, "end");
            finishSocket();
            quitHandler(10003, null);
            // 重新连接server
            reconnection();
        }
    }

    /**
     * 重新连接server
     */
    private synchronized void reconnection() {
        if (!mIsReconnection
                || mIsDisconnect || mConnectThread.isShutdown()) {  // 说明主动调用了disconnect方法
            mLifecycle = Lifecycle.Terminated;
            return;
        }
        try {
            Log.d(TAG, mMillis + "ms后重新连接server......");
            mLifecycle = Lifecycle.Waiting;
            /**
             * mConnectThread线程等待
             * 在线程等待时，disconnect方法可能会被调用，此时会抛出异常InterruptedException
             */
            mConnectThread.awaitTermination(mMillis, TimeUnit.MILLISECONDS);
            restart();
        } catch (InterruptedException e) {
            Log.e(TAG, "wait error: " + e); // 在等待时，线程被Interrupt
            restart();
        }
    }

    /**
     * 开始重新连接
     */
    private void restart() {
        if (mIsDisconnect) {
            mLifecycle = Lifecycle.Terminated;
            return;
        }
        Log.d(TAG, "start reconnect server");
        connect();
    }

    /**
     * 通过结束符 /n 处理数据的分包、粘包
     *
     * @param data
     */
    private void handlerData(String data) {
        if (data.isEmpty()) return;
        Log.d(TAG, "handler server message:" + data);
        writerLogInfo(data); // TODO 需要单独开线程处理
        boolean contains = data.contains(mEndSymbol);
        if (contains) {
            String[] split = data.split(mEndSymbol);
            Log.d(TAG, "粘包个数:" + split.length);
            if (split.length == 1) { // 一条数据
                String firstDtaJson = split[0].trim();
                if (Utils.Companion.isJson(firstDtaJson)) {  // 完整一条数据
                    sendMes(10002, firstDtaJson);
                } else { // 分包数据
                    stringBuilder.append(firstDtaJson);
                    // 拼接后，验证一下拼接数据是否拼接完成，是否为一条完整的json数据
                    String firstStringBuilder = stringBuilder.toString().trim();
                    if (Utils.Companion.isJson(firstStringBuilder)) {
                        sendMes(10002, firstStringBuilder);
                        stringBuilder.delete(0, stringBuilder.length());
                    }
                }
            } else if (split.length >= 2) {  // 分包处理 + 分包中带粘包
                // 处理第一条数据
                String dataJson1 = split[0].trim();
                if (Utils.Companion.isJson(dataJson1)) {  // 第一条数据是完整数据，直接发送
                    sendMes(10002, dataJson1);
                    stringBuilder.delete(0, stringBuilder.length());
                } else {  // 分包数据，进行数据拼接
                    stringBuilder.append(dataJson1);
                    // 拼接后，验证一下拼接数据是否拼接完成，是否为一条完整的json数据
                    String dataJson = stringBuilder.toString().trim();
                    if (Utils.Companion.isJson(dataJson)) {
                        sendMes(10002, dataJson);
                        stringBuilder.delete(0, stringBuilder.length());
                    }
                }

                // 处理粘包数据
                for (int i = 1; i < split.length - 2; i++) {
                    String dataJson = split[i].trim();
                    if (Utils.Companion.isJson(dataJson)) {
                        sendMes(10002, dataJson);
                    } else {
                        Log.d(TAG, "handlerData: message不完整,被丢弃:" + dataJson);
                    }
                }

                // 处理最后一条数据
                // 最后一条数据是否出现粘包
                String endData = split[split.length - 1].trim();
                if (Utils.Companion.isJson(endData)) { // 最后一条数据为完整数据，直接发送
                    sendMes(10002, endData);
                } else { // 最后一条数据为粘包数据，进行数据拼接
                    stringBuilder.append(endData);
                    // 拼接后，验证一下拼接数据是否拼接完成，是否为一条完整的json数据
                    String endStringBuilder = stringBuilder.toString().trim();
                    if (Utils.Companion.isJson(endStringBuilder)) {
                        sendMes(10002, endStringBuilder);
                        stringBuilder.delete(0, stringBuilder.length());
                    }
                }
            }
        }
    }

    /**
     * 发送数据到服务端
     *
     * @param msg
     */
    public void sendMessage(String msg) {
        if (isSocketConnect() && !mSendDataThread.isShutdown()) {
            mSendDataThread.execute(() -> sendData(msg));
        } else {
            Log.i(TAG, "send message error:socket unlink");
        }
    }

    private void sendData(String msg) {
        OutputStream outputStream = null;
        try {
            if (!isSocketConnect()) {
                Log.i(TAG, "socket unlink");
                return;
            }
            outputStream = mSocket.getOutputStream();
            outputStream.write(msg.getBytes());
            outputStream.flush();
            outputStream.write(mEndSymbol.getBytes());  // 结束符
            outputStream.flush();
            Log.d(TAG, "数据发送完毕");
        } catch (IOException e) {
            Log.e(TAG, "send message error:" + e);
        }
//        finally {
//            if (outputStream != null) {
//                try {
//                    outputStream.close();   // socket的输出流不能关闭，调用close方法后，socket链接会断开
//                } catch (IOException e) {
//                    Log.e(TAG, "close" + e);
//                }
//            }
//        }
    }

    /**
     * 发送本地File
     *
     * @param path 路径
     */
    public void sendPathMessage(String path) {
        File file = new File(path);
        if (!file.exists()) {
            Log.i(TAG, "sendPathData: path不存在:" + path);
            return;
        }
        if (!mSendDataThread.isShutdown()) {
            mSendDataThread.execute(() -> sendFileData(file));
        } else {
            Log.i(TAG, "send error");
        }
    }

    /**
     * 发送File
     *
     * @param file
     */
    private void sendFileData(File file) {
        if (!file.exists()) {
            Log.i(TAG, "file不存在:" + file.getPath());
            return;
        }
        if (!isSocketConnect()) {
            Log.i(TAG, "socket unlink");
            return;
        }
        FileInputStream fileInputStream = null;
        OutputStream outputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            byte[] bytes = new byte[1024 * 1024];
            int readCount = 0;
            outputStream = mSocket.getOutputStream();
            while ((readCount = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, readCount);
                outputStream.flush();
            }
            outputStream.write(mEndSymbol.getBytes());  // 结束符
            outputStream.flush();
            Log.d(TAG, "send data finish");
        } catch (IOException e) {
            Log.e(TAG, "send message error:" + e);
        } finally {
//            if (outputStream != null) {
//                try {
//                    outputStream.close();  // socket的输出流不能关闭，调用close方法后，socket链接会断开
//                } catch (IOException e) {
//                    Log.e(TAG, "close:" + e);
//                }
//            }
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "close:" + e);
                }
            }
        }
    }

    /**
     * 发送回执数据
     */
    private void sendACKData() {
        //发送给服务端的消息
        String msg = "Hello,我来自client(ACK)";
        try {
            Log.d(TAG, "发送回执数据......");
            //获取输出流并实例化
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(mSocket.getOutputStream()));
            out.write(msg + "\n");//防止粘包
            out.flush();
        } catch (Exception e) {
            Log.e(TAG, "发送回执数据错误:" + e);
        } finally {
            //关闭Socket
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close client error:" + e);
            }
            Log.i(TAG, "client close");
        }
    }

    /**
     * 连接server
     */
    public void connect() {
        if (mSocket != null && !mSocket.isClosed()) return;
        mLifecycle = Lifecycle.New;
        initThreadExecutor();
        mIsDisconnect = false;
        mConnectThread.execute(mRun);
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        mIsDisconnect = true;
        shutdownNowThreadExecutor();
        finishSocket();
        Log.i(TAG, "disconnect: interrupt");
    }

    /**
     * socket是否处于连接状态
     *
     * @return
     */
    private Boolean isSocketConnect() {
        return mSocket != null && mSocket.isConnected() && !mSocket.isClosed()
                && !mSocket.isOutputShutdown();
    }

    /**
     * 断开连接后,是否重新连接server
     *
     * @param isReconnection
     */
    private void isReconnection(boolean isReconnection) {
        mIsReconnection = isReconnection;
    }

    private boolean mIsSubPackage(String data) {
        return !String.valueOf(data.charAt(data.length())).equals(mEndSymbol);
    }

    /**
     * 初始化Handler
     */
    private void initHandlerThread() {
        if (mDataDistributeThread == null
                || mDataDistributeThread.getState() == Thread.State.TERMINATED
                || !mDataDistributeThread.isAlive()
                || mDataDistributeThread.isInterrupted()) {
            mDataDistributeThread = new HandlerThread(mDataHandlerThreadName);
            mDataDistributeThread.start();
        }
        if (mDataDistributeHandler == null) {
            mDataDistributeHandler = new Handler(mDataDistributeThread.getLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case 10001: // 连接成功
                            if (mOnServiceDataListener != null) mOnServiceDataListener.connect();
                            break;

                        case 10002: // 收到数据
                            if (mOnServiceDataListener != null)
                                mOnServiceDataListener.receive((String) msg.obj);
                            break;

                        case 10003: // 断开连接
                            if (mOnServiceDataListener != null) mOnServiceDataListener.offline();
                            break;

                        case 10004: // 接收异常
                            if (mOnServiceDataListener != null)
                                mOnServiceDataListener.error((IOException) msg.obj);
                            break;

                        case 10005: // 连接失败
                            if (mOnServiceDataListener != null)
                                mOnServiceDataListener.connectionFail((Exception) msg.obj);
                            break;

                        case 10006: // 数据处理
//                            handlerData((String) msg.obj);
//                            mLastTime = System.currentTimeMillis();
                            break;

                        case 10007: // 正在链接
                            if (mOnServiceDataListener != null)
                                mOnServiceDataListener.connecting();
                            break;
                    }
                }
            };
        }
    }

    /**
     * 释放Handler
     */
    private void quitHandlerThread() {
        if (mDataDistributeHandler != null) {
            mDataDistributeHandler.removeCallbacksAndMessages(null);
            mDataDistributeHandler.getLooper().quitSafely();
        }
        if (mDataDistributeThread != null && mDataDistributeThread.isAlive()) {
            mDataDistributeThread.interrupt();
        }
        mDataDistributeThread = null;
        mDataDistributeHandler = null;
    }

    private void sendMes(int what) {
        if (mDataDistributeHandler != null)
            mDataDistributeHandler.sendEmptyMessage(what);
    }

    private void sendMes(int what, Object obj) {
        if (mDataDistributeHandler != null)
            mDataDistributeHandler.sendMessage(Message.obtain(mDataDistributeHandler, what, obj));
    }

    /**
     * 初始化线程池
     */
    private void initThreadExecutor() {
        if (mConnectThread.isShutdown()) {
            mConnectThread = Executors.newSingleThreadExecutor();
        }
        if (mDataHandlerThread.isShutdown()) {  // TODO待优化 初始化时机滞后到socket连接成功之后
            mDataHandlerThread = Executors.newSingleThreadExecutor();
        }
        if (mSendDataThread.isShutdown()) {  // TODO待优化 初始化时机滞后到socket连接成功之后
            mSendDataThread = Executors.newSingleThreadExecutor();
        }
    }

    /**
     * 关闭线程池
     */
    private void shutdownNowThreadExecutor() {
        if (!mConnectThread.isShutdown()) {
            mConnectThread.shutdownNow();
        }
        if (!mDataHandlerThread.isShutdown()) {
            mDataHandlerThread.shutdownNow();
        }
        if (!mSendDataThread.isShutdown()) {
            mSendDataThread.shutdownNow();
        }
    }

    /**
     * 关闭io
     */
    private void closeStream() {
        if (mBufferedWriter != null) {
            try {
                mBufferedWriter.close();
                mBufferedWriter = null;
            } catch (IOException e) {
                Log.e(TAG, "close:" + e);
            }
        }
        if (mInputStream != null) {
            try {
                mInputStream.close();
                mInputStream = null;
            } catch (IOException e) {
                Log.e(TAG, "close:" + e);
            }
        }
    }

    /**
     * 关闭Socket
     */
    private void closeSocket() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close:" + e);
            }
        }
    }

    /**
     * 释放socket
     */
    private void finishSocket() {
        closeStream();
        closeSocket();
    }

    /**
     * 退出handler
     *
     * @param what TODO当MessageQueue中的Message阻塞排队时，该消息可能会被remove
     * @param obj
     */
    private void quitHandler(int what, Object obj) {
        // TODO当MessageQueue中的Message阻塞排队时，该消息可能会被remove
        if (obj == null) {
            sendMes(what);
        } else {
            sendMes(what, obj);
        }
        quitHandlerThread();
    }

    /**
     * 是否开启本地日志记录: 需要动态申请本地读写权限
     * 本地日志路径:  /trans/record/communication_log.txt
     */
    private void initLog() {
        if (mLogEnabled && mBufferedWriter == null) {
            File file = new File(
                    Environment.getExternalStorageDirectory().getAbsolutePath()
                            + "/trans/record");
            if (!file.exists()) {
                try {
                    file.mkdirs();
                } catch (Exception e) {
                    Log.e(TAG, "创建日志文件夹错误:" + e);
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
                mBufferedWriter = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(communicationFile, true)));
            } catch (FileNotFoundException e) {
                Log.e(TAG, "找不到日志文件:" + e);
            }
        }
    }

    /**
     * 数据信息写入本地
     *
     * @param data
     */
    private void writerLogInfo(String data) {
        if (mLogEnabled && mBufferedWriter != null) {
            try {
                Date date = Utils.Companion.getMDate();
                date.setTime(System.currentTimeMillis());
                mBufferedWriter.write(Utils.Companion.getMSimpleDateFormat().format(date) + " ");
                mBufferedWriter.write(data);
                mBufferedWriter.write("\r\n");
                if (++mBufferedIndex % mDiskWriteHz == 0) { // 避免高频率的访问本地磁盘，造成卡顿
                    mBufferedWriter.flush();
                    mBufferedIndex = 0;
                }
            } catch (IOException e) {
                Log.e(TAG, "日志写入错误:" + e);
            }
        }
    }

    /**
     * 建造者模式
     */
    public static class Builder {
        private String mHost = "192.168.10.123";
        private int mPort = 8080;
        private int mHz = 10;
        private long mMillis = 1000;
        private String mEndSymbol = "\0";
        private Boolean mIsReconnection = false;
        private OnServiceDataListener mOnServiceDataListener;
        private Boolean mLogEnabled = false;

        public Builder host(String host) {
            mHost = host;
            return this;
        }

        public Builder port(int port) {
            mPort = port;
            return this;
        }

        public Builder hz(int hz) {
            mHz = hz;
            return this;
        }

        public Builder endSymbol(String symbol) {
            mEndSymbol = symbol;
            return this;
        }

        public Builder reconnection(boolean isReconnection) {
            mIsReconnection = isReconnection;
            return this;
        }

        public Builder reconnection(boolean isReconnection, long millis) {
            mIsReconnection = isReconnection;
            mMillis = millis < 1000 ? 1000 : millis;
            return this;
        }

        public Builder log(boolean enabled) {
            mLogEnabled = enabled;
            return this;
        }

        public Builder listener(OnServiceDataListener listener) {
            mOnServiceDataListener = listener;
            return this;
        }

        public SocketClient build() {
            return new SocketClient(this);
        }

        public SocketClient build(OnServiceDataListener listener) {
            return listener(listener).build();
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
     * 线程的生命周期状态
     */
    enum Lifecycle {
        New,
        Runnable,
        Running,
        Blocked,
        Waiting,
        TimedWaiting,
        Terminated,
    }
}
