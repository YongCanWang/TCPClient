package com.trans.libnet.mqtt;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

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
 * @date :2024/1/4 16:34
 */
public class MQTTMate {
    private static final String TAG = "MQTTMate";
    public static final MQTTMate INSTANCE = new MQTTMate();
    private MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mqttConnectOptions;
    //    private final String serverURI = "tcp://broker.emqx.io:1883"; // 连接成功
//    private final String serverURI = "ssl://broker.emqx.io:8883"; // 连接成功
    private final String serverURI = "ssl://z8ce6691.ala.cn-hangzhou.emqxsl.cn:8883"; // 连接成功

    private final String userName = "emqx_admin";
    private final String password = "123456";
    private final String clientId = "android_client_test";
    private final String subTopic = "topic_android_test";
    private MQTTMessageService mBoundService;

    public void startMQTT(Context context) {
        Intent intent = new Intent(context, MQTTMessageService.class);
//        context.startService(intent);
        context.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
//                mBoundService = ((MQTTMessageService.MQTTMessageBinder) service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);
    }


    /**
     * 发布
     *
     * @param topic
     * @param msg
     */
    public void publish(String topic, String msg) {
//        mBoundService.publishMessage(topic, msg);
        publishMessage(topic, msg, 2);
    }


    /**
     * mqtt初始化
     */
    public void initMQTT(Context context) {
        try {
            mqttAndroidClient = new MqttAndroidClient(context,
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
                    Log.e(TAG, "MQTT连接断开:" + cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    Log.e(TAG, "收到MQTT消息: topic:" + topic + "---message:" + message.toString() + "---qos:" + message.getQos());
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
            mqttConnectOptions.setPassword(password.toCharArray());
            //断开后，是否自动连接
            mqttConnectOptions.setAutomaticReconnect(true);
            //是否清空客户端的连接记录。若为true，则断开后，broker将自动清除该客户端连接信息
            mqttConnectOptions.setCleanSession(true);
            //设置超时时间，单位为秒
            mqttConnectOptions.setConnectionTimeout(10);
            //心跳时间，单位为秒。即多长时间确认一次Client端是否在线
            mqttConnectOptions.setKeepAliveInterval(30);
            //允许同时发送几条消息（未收到broker确认信息）
            mqttConnectOptions.setMaxInflight(30);
        } catch (Exception e) {
            Log.e(TAG, "MQTT客户端初始化错误:" + e);
            e.printStackTrace();
        }
    }


    /**
     * 连接mqtt
     */
    public void connectMQTT() {
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.e(TAG, "MQTT连接成功！！！");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "MQTT连接失败:" + exception);

//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Log.e(TAG,"30s后，尝试重新连接" );
//                            try {
//                                Thread.sleep(1000*30);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            connectMqtt();
//                        }
//                    }).start();
                }
            });
        } catch (MqttException e) {
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
    private void publishMessage(String topic, String msg, int qos) {
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


}
