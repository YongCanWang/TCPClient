package com.trans.tcpclient

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.trans.udpclinet.DatagramSocketClient

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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