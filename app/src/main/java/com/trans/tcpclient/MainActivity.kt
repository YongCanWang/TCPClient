package com.trans.tcpclient

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.trans.libnet.mqtt.MQTTClient
import com.trans.libnet.tcpclient.SocketClient
import com.trans.libnet.tcpclient.SocketClient.OnServiceDataListener
import com.trans.libnet.tcpclient.obu.Constants
import com.trans.libnet.tcpclient.obu.OBU_BSM
import com.trans.libnet.tcpclient.obu.OBU_HEART
import com.trans.libnet.tcpclient.obu.OBU_MAP
import com.trans.libnet.tcpclient.obu.OBU_RSI
import com.trans.libnet.tcpclient.obu.OBU_RSM
import com.trans.libnet.tcpclient.obu.OBU_SPAT
import com.trans.libnet.tcpclient.obu.OBU_TM
import com.trans.libnet.tcpclient.obu.OBU_TPM
import com.trans.libnet.tcpclient.obu.OBU_VIM
import com.trans.libnet.udpclinet.DatagramSocketClient
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val ipv4Address = SocketClient.getIPAddress(this)
        Log.e(TAG, "IP地址:$ipv4Address")
        findViewById<TextView>(R.id.tv_ip).text = "IP: $ipv4Address"
    }

    fun onStartTCPClient(view: View) {
        /**
         * 启动客户端前必须先启动服务端，否则会连接失败，报错误:
         * java.net.ConnectException: failed to connect to /172.19.250.161 (port 12345)
         * from /172.19.250.104 (port 47878) after 3000ms:
         * isConnected failed: ECONNREFUSED (Connection refused)
         */
//        Thread(SocketClient.net).start()

//        SocketClient.onServiceDataListener =
//        SocketClient.setHostname("172.19.250.85")
//        SocketClient.setHz(30)
//        SocketClient.logEnabled(true)
//        SocketClient.isReconnection(false)
//        SocketClient.connect()

        /**
         * 构建者模式
         */
        SocketClient.Builder()
            .listener(object : OnServiceDataListener {
                override fun connect() {
                    Log.e(TAG, "连接成功")
                }

                override fun connecting() {
                    Log.e(TAG, "正在连接")
                }

                override fun receive(data: String?) {
                    Log.e(TAG, "收到服务端数据:$data")
                    try {
                        when (SocketClient.getOBUType(data)) {
                            Constants.HEART -> {
                                val heart = SocketClient.gson.fromJson(data, OBU_HEART::class.java)
                                Log.e(TAG, "receive: HEART:" + heart.heart.id)
                            }

                            Constants.BSM -> {
                                val bsm = SocketClient.gson.fromJson(data, OBU_BSM::class.java)
                                Log.e(TAG, "receive: BSM:$bsm")
                            }

                            Constants.MAP -> {
                                val map = SocketClient.gson.fromJson(data, OBU_MAP::class.java)
                                Log.e(TAG, "receive: MAP:$map")
                            }

                            Constants.RSI -> {
                                val rsi = SocketClient.gson.fromJson(data, OBU_RSI::class.java)
                                Log.e(TAG, "receive: RSI:$rsi")
                            }

                            Constants.RSM -> {
                                val rsm = SocketClient.gson.fromJson(data, OBU_RSM::class.java)
                                Log.e(TAG, "receive: RSM:$rsm")
                            }

                            Constants.SPAT -> {
                                val spat = SocketClient.gson.fromJson(data, OBU_SPAT::class.java)
                                Log.e(TAG, "receive: SPAT:$spat")
                            }

                            Constants.TPM -> {
                                val tpm = SocketClient.gson.fromJson(data, OBU_TPM::class.java)
                                Log.e(TAG, "receive: TPM:$tpm")
                            }

                            Constants.VIM -> {
                                val vim = SocketClient.gson.fromJson(data, OBU_VIM::class.java)
                                Log.e(TAG, "receive: VIM:$vim")
                            }

                            Constants.TM -> {
                                val tm = SocketClient.gson.fromJson(data, OBU_TM::class.java)
                                Log.e(TAG, "receive: TM:$tm")
                            }

                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "receive: 数据解析错误:$e")
                    }
                }

                override fun offline() {
                    Log.e(TAG, "断开连接")
                }

                override fun error(e: IOException?) {
                    Log.e(TAG, "接受数据错误:$e")
                }

                override fun connectionFail(e: Exception?) {
                    Log.e(TAG, "连接服务器错误:$e")
                }
            })
//            .hostname("192.168.10.123")
//            .port(7130)
            .hostname("172.19.251.46")
            .port(8888)
            .hz(30)
            .log(true)
            .reconnection(false)
            .connect()
    }

    fun onSendTCPDataToService(view: View) {
        SocketClient.sendDataToService("Hello,我是TCP数据,我来自客户端")
    }

    fun onSendUDPDataToService(view: View) {
        Thread(DatagramSocketClient.net).start()
    }

    fun onConnectMQTTService(view: View) {
        MQTTClient.Builder()
            .cont(this)
            .prefix(MQTTClient.Prefix.TCP)
            .reconnection(true)
//            .host("117.135.58.188")  // 上海华为金桥项目-上研院-mqtt下发服务
//            .port(1883)  // 上海华为金桥项目-上研院-mqtt下发端口
//            .name("maintain")
//            .password("Cino#2018#")
            .host("47.101.168.108")  // 上海华为金桥项目-上研院-mqtt下发服务-测试时延上报
            .port(1883)  // 上海华为金桥项目-上研院-mqtt下发端口-测试时延上报
            .name("admin")
            .password("admin")
            .connect(object : MQTTClient.OnServiceDataListener {
                override fun connect() {
                    Log.e(TAG, "连接成功")
                }

                override fun connecting() {
                    Log.e(TAG, "正在连接")
                }

                override fun receive(data: String?) {
                    Log.e(TAG, "收到服务端数据:$data")
                }

                override fun offline() {
                    Log.e(TAG, "断开连接")
                }

                override fun error(e: IOException?) {
                    Log.e(TAG, "接受数据错误:$e")
                }

                override fun connectionFail(e: Exception?) {
                    Log.e(TAG, "连接服务器错误:$e")
                }

            })
    }

    fun onPublishMessageToMQTTService(view: View) {
//        MQTTClient.publishMessage("topic_android_test", "你好，我来自Android客户端！！！", 2)
        MQTTClient.publishMessage(
            "jinqiao/siwei/timedelay",
            "\"{\"timeDelay\":10,\"timestamp\":1700000000000}\"",
            2
        )
    }
}