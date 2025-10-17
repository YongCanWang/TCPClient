package com.trans.libnet.udpclinet;

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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Tom灿
 * a@description: UDP通信客户端 要求在同一局域网下(同一网段)
 * UCP网络传输协议特性
 * ①实时性
 * ②高速传输
 * ③不安全（无重发机制，丢包需要应用做重发操作）
 * ④保证数据大小，不保证数据一定送达（丢失数据）
 * ⑤无连接特性：UDP无需建立连接，直接通过数据包（DatagramPacket）传输。
 * @mDate :2023/5/25 9:24
 */
public class UDPClient {
    private static final String TAG = "UDPClient";
    private String mHost = "192.168.10.123"; // obu设备IP
    private static int mServicePort = 8080; // 服务端端口
    private static int mClientPort = 12345; // 本地端口 客户端
    private DatagramSocket mSocket;
    private static final int TIMEOUT = 3000;  // 阻塞时长
    private OnServiceDataListener mOnServiceDataListener;
    private Lifecycle mLifecycle; // 线程的生命周期状态
    private boolean mIsDisconnect = false;
    private boolean mIsReListener = false; // 断开监听后,是否自动重新监听
    private long mLastTime = System.currentTimeMillis();
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
    private final Runnable mRun = () -> startListener();

    UDPClient(Builder builder) {
        mHost = builder.mHost;
        mServicePort = builder.mRemotePort;
        mClientPort = builder.mLocalPort;
        mOnServiceDataListener = builder.mOnServiceDataListener;
        mIsReListener = builder.mIsReListener;
        mMillis = builder.mMillis;
        mLogEnabled = builder.mLogEnabled;
        initLog();
    }

    /**
     * 开始监听本地端口
     */
    private synchronized void startListener() {
        try {
            mLifecycle = Lifecycle.Runnable;
            if (mOnServiceDataListener != null) mOnServiceDataListener.listenering();
            mSocket = new DatagramSocket(mClientPort); // Socket对象 绑定到指定本地的端口
//            int port = mSocket.getPort();
            Log.d(TAG, "init");
            // 设置接收数据时阻塞的最长时间
//            mSocket.setSoTimeout(TIMEOUT);

            if (mIsDisconnect) {
                mLifecycle = Lifecycle.Terminated;
                Log.i(TAG, "task interrupt");
                return;
            }

            mLifecycle = Lifecycle.Running;
            initHandlerThread();
            sendMes(10001);
            Log.d(TAG, "开始监听" + mClientPort + "端口......");
            // 响应数据包
            byte[] responseBytes = new byte[1024];
            while (!mIsDisconnect && mSocket != null) {
                DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);
                try {
                    // 阻塞等待接收数据
                    mSocket.receive(responsePacket);
                    // 截取实际接收到的数据
                    byte[] receivedData = Arrays.copyOfRange(
                            responseBytes, 0, responsePacket.getLength());
                    mOnServiceDataListener.receive(receivedData);

//                    String response = new String(responseBytes, 0, responsePacket.getLength());
//                    Log.i(TAG, "receive:" + response);
//                    if (!mDataHandlerThread.isShutdown()) {
//                        mDataHandlerThread.execute(() -> {
//                            handlerData(response);
//                        });
//                    }
                } catch (InterruptedIOException e) {
                    Log.i(TAG, "监听" + mClientPort + "端口超时:" + e);
                }
            }
            Log.i(TAG, "listener disconnect");
        } catch (Exception e) {
            Log.e(TAG, "port listener error: " + e);
            finishSocket();
            quitHandler(10005, e);
            // 重新监听端口
            reListener();
        } finally {
            Log.i(TAG, "end");
            finishSocket();
            quitHandler(10003, null);
            // 重新连接server
            reListener();
        }
    }

    /**
     * 重新监听端口
     */
    private synchronized void reListener() {
        if (!mIsReListener
                || mIsDisconnect || mConnectThread.isShutdown()) {  // 说明主动调用了disconnect方法
            mLifecycle = Lifecycle.Terminated;
            return;
        }
        try {
            Log.d(TAG, mMillis + "ms后重新监听端口......");
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
        Log.d(TAG, "start listener port");
        listener();
    }

    /**
     * 分发数据
     *
     * @param data
     */
    private void handlerData(String data) {
        if (data.isEmpty()) return;
        Log.d(TAG, "handler server message:" + data);
        writerLogInfo(data); // TODO 需要单独开线程处理
        sendMes(10002, data);
    }

    /**
     * 发送数据到服务端
     *
     * @param msg
     */
    public void sendMessage(String msg) {
        sendMessage(msg.getBytes());
    }

    /**
     * 发送数据到服务端
     *
     * @param dataBytes
     */
    public void sendMessage(byte[] dataBytes) {
        if (mSendDataThread.isShutdown()) {
            Log.i(TAG, "send message error: thread is shutdown");
            mSendDataThread = Executors.newSingleThreadExecutor();
        }
        mSendDataThread.execute(() -> sendData(dataBytes));
    }

    private void sendData(byte[] dataBytes) {
        if (isSocketClosed()) {
            Log.i(TAG, "socket is close");
            try {
                mSocket = new DatagramSocket(mClientPort); // Socket对象 绑定到指定本地的端口
                Log.i(TAG, "new socket");
            } catch (SocketException e) {
                Log.e(TAG, "sendData: " + e);
            }
        }

        try {
            InetAddress inetAddress = InetAddress.getByName(mHost);  // 设置IP

            DatagramPacket dataPacket = new DatagramPacket(dataBytes, dataBytes.length,
                    inetAddress, mServicePort); // 数据包: 设置ip、端口、数据
            mSocket.send(dataPacket);
        } catch (Exception e) {
            Log.e(TAG, "sendData: " + e);
        }
    }

    /**
     * 监听server
     */
    public void listener() {
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
     * socket是否关闭
     *
     * @return
     */
    private Boolean isSocketClosed() {
        return mSocket == null || mSocket.isClosed();
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
                            if (mOnServiceDataListener != null) mOnServiceDataListener.listener();
                            break;

                        case 10002: // 收到数据
//                            if (mOnServiceDataListener != null)
//                                mOnServiceDataListener.receive((String) msg.obj);
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
                                mOnServiceDataListener.listenerFail((Exception) msg.obj);
                            break;

                        case 10006: // 数据处理
//                            handlerData((String) msg.obj);
//                            mLastTime = System.currentTimeMillis();
                            break;

                        case 10007: // 正在链接
                            if (mOnServiceDataListener != null)
                                mOnServiceDataListener.listenering();
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
            mSocket.close();
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

    public interface OnServiceDataListener {
        void listener();

        void listenering();

        void receive(byte[] bytes);

        void offline();

        void error(IOException e);

        void listenerFail(Exception e);
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
        private int mRemotePort = 8080;
        private int mLocalPort = 8081;
        private long mMillis = 1000;
        private Boolean mIsReListener = false;
        private OnServiceDataListener mOnServiceDataListener;
        private Boolean mLogEnabled = false;

        public Builder host(String host) {
            mHost = host;
            return this;
        }

        public Builder localPort(int localPort) {
            mLocalPort = localPort;
            return this;
        }

        public Builder remotePort(int remotePort) {
            mRemotePort = remotePort;
            return this;
        }

        public Builder reListener(boolean reListener) {
            mIsReListener = reListener;
            return this;
        }

        public Builder reListener(boolean reListener, long millis) {
            mIsReListener = reListener;
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

        public UDPClient build() {
            return new UDPClient(this);
        }

        public UDPClient build(OnServiceDataListener listener) {
            return listener(listener).build();
        }
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
