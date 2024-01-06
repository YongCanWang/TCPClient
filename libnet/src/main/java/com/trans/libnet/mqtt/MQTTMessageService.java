package com.trans.libnet.mqtt;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.trans.libnet.R;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * @author Tom灿
 * @description:
 * @date :2024/1/4 16:35
 */
public class MQTTMessageService extends Service {
    private static final String TAG = "MQTTMessageService";
    private MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mqttConnectOptions;
    private final String serverURI = "https://z8ce6691.ala.cn-hangzhou.emqxsl.cn:8883";
    private final String userName = "emqx_admin";
    private final char[] password = new char[6];
    private final String clientId = "test";
    private final String subTopic = "topic_test";
    private NotificationManager mNM;
//    private int NOTIFICATION = R.string.local_service_started;
    @Override
    public void onCreate() {
        super.onCreate();
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
        for (int i = 1; i < 7; i++) {
            Log.e(TAG, "密码: " + i);
            password[i-1] = (char) i;
        }
        initMQTT();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MQTTMessageBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        Log.e(TAG, "关闭MQTT");
        //断开mqtt连接
        try {
            if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
                mqttAndroidClient.disconnect();
                mqttAndroidClient.unregisterResources();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "断开MQTT错误:" + e);
        }
        super.onDestroy();
    }


    public class MQTTMessageBinder extends Binder {
        MQTTMessageService getService() {
            return MQTTMessageService.this;
        }
    }

    /**
     * mqtt初始化
     */
    private void initMQTT() {
        try {
            //写上自己的url
            mqttAndroidClient = new MqttAndroidClient(getApplicationContext(),
                    serverURI, clientId);
            mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    if (reconnect) {
                        Log.e(TAG, "MQTT重新连接成功！serverURI:" + serverURI);
                    } else {
                        Log.e(TAG, "MQTT连接成功！serverURI:" + serverURI);
                    }
                    subscribeAllTopics();
                }

                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "MQTT连接断开！" + cause.getCause());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    Log.e(TAG, "收到MQTT消息：" + topic + message.toString() + message.getQos());
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


            // 新建连接设置
            mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setUserName(userName);
            mqttConnectOptions.setPassword(password);
            //断开后，是否自动连接
            mqttConnectOptions.setAutomaticReconnect(true);
            //是否清空客户端的连接记录。若为true，则断开后，broker将自动清除该客户端连接信息
            mqttConnectOptions.setCleanSession(true);
            //设置超时时间，单位为秒
            mqttConnectOptions.setConnectionTimeout(15);
            //心跳时间，单位为秒。即多长时间确认一次Client端是否在线
            mqttConnectOptions.setKeepAliveInterval(30);
            //允许同时发送几条消息（未收到broker确认信息）
            mqttConnectOptions.setMaxInflight(30);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 订阅所有主题
     */
    private void subscribeAllTopics() {
        //订阅主消息主题和更新消息主题
        subscribeToTopic(subTopic, 2);
    }

    /**
     * 订阅一个主主题
     *
     * @param subTopic 主题名称
     */
    private void subscribeToTopic(String subTopic, int qos) {
        try {
            mqttAndroidClient.subscribe(subTopic, qos, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.e(TAG, "MQTT订阅消息成功：" + subTopic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "MQTT订阅消息失败！" + subTopic);
                }
            });
        } catch (MqttException ex) {
            Log.e(TAG, "subscribeToTopic: Exception whilst subscribing");
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
    private void publishMessage(String topic, String msg, int qos) {
        if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
            try {
                Log.e(TAG, "publishMessage: 发送" + msg);
                mqttAndroidClient.publish(topic, msg.getBytes(), qos, false);
            } catch (Exception e) {
                Log.e(TAG, "publishMessage: Error Publishing: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "publishMessage失败，MQTT未连接 ");
        }
    }

    public void publishMessage(String topic, String msg) {
        publishMessage(topic, msg, 2);
    }




    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
//        CharSequence text = getText(R.string.local_service_started);

        // The PendingIntent to launch our activity if the user selects this notification
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                new Intent(this, LocalServiceActivities.Controller.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
//                .setSmallIcon(R.mipmap.ic_launcher)  // the status icon
//                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("service")  // the label of the entry
//                .setContentText(text)  // the contents of the entry
//                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
//        mNM.notify(NOTIFICATION, notification);
    }


}
