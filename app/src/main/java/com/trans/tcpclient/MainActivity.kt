package com.trans.tcpclient

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.trans.libnet.tcpclient.SocketClient
import com.trans.libnet.tcpclient.SocketClient.OnServiceDataListener
import com.trans.libnet.tcpclient.obu.Constants
import com.trans.libnet.tcpclient.obu.OBU_BSM
import com.trans.libnet.tcpclient.obu.OBU_HEART
import com.trans.libnet.tcpclient.obu.OBU_MAP
import com.trans.libnet.tcpclient.obu.OBU_RSI
import com.trans.libnet.tcpclient.obu.OBU_RSM
import com.trans.libnet.tcpclient.obu.OBU_SPAT
import com.trans.libnet.tcpclient.obu.OBU_TPM
import com.trans.libnet.tcpclient.obu.OBU_VIM
import com.trans.libnet.udpclinet.DatagramSocketClient

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

        SocketClient.onServiceDataListener = object : OnServiceDataListener {
            override fun connect() {
                Log.e(TAG, "连接成功")
            }

            override fun receive(data: String?) {
                Log.e(TAG, "收到服务器数据:$data")
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
                }
            }

            override fun offline() {
                Log.e(TAG, "断开连接")
            }

            override fun error(e: String?) {
                Log.e(TAG, "接受数据错误:$e")
            }
        }
        SocketClient.connect()
    }

    fun onSendTCPDataToService(view: View) {
        SocketClient.sendDataToService("Hello,我是TCP数据,我来自客户端")
    }

    fun onSendUDPDataToService(view: View) {
        Thread(DatagramSocketClient.net).start()
    }
}