package com.trans.tcpclient

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.trans.udpclinet.DatagramSocketClient

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
        Thread(SocketClient.net).start()
    }

    fun onSendTCPDataToService(view: View) {
        SocketClient.sendDataToService("Hello,我是TCP数据,我来自客户端")
    }

    fun onSendUDPDataToService(view: View) {
        Thread(DatagramSocketClient.net).start()
    }
}