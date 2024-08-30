package com.trans.libnet.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import org.json.JSONException
import org.json.JSONObject
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.text.SimpleDateFormat
import java.util.Date

/**
 * @author Tom灿
 * @description:
 * @date :2024/8/28 11:23
 */
class Utils {
    companion object {
        private val TAG = "Utils"
        val mGson = Gson()
        private val mJsonParser = JsonParser()
        val mSimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val mDate = Date()

        /**
         * 判断是否为Json格式数据
         * @param json
         * @return
         */
        fun isJson(json: String): Boolean {
            try {
                mJsonParser.parse(json)
            } catch (e: Exception) {
                return false
            }
            return true
        }

        /**
         * 获取IP地址
         *
         * @param context
         * @return
         */
        fun getDeviceAddress(context: Context): String? {
            val info =
                (context.getSystemService(Context.CONNECTIVITY_SERVICE)
                        as ConnectivityManager).activeNetworkInfo
            if (info != null && info.isConnected) {
                if (info.type == ConnectivityManager.TYPE_MOBILE) { //当前使用2G/3G/4G网络
                    try {
                        //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                        val en = NetworkInterface.getNetworkInterfaces()
                        while (en.hasMoreElements()) {
                            val intf = en.nextElement()
                            val enumIpAddr = intf.inetAddresses
                            while (enumIpAddr.hasMoreElements()) {
                                val inetAddress = enumIpAddr.nextElement()
                                if (!inetAddress.isLoopbackAddress
                                    && inetAddress is Inet4Address
                                ) {
                                    return inetAddress.getHostAddress()
                                }
                            }
                        }
                    } catch (e: SocketException) {
                        e.printStackTrace()
                    }
                } else if (info.type == ConnectivityManager.TYPE_WIFI) { //当前使用无线网络
                    val wifiManager =
                        context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    val wifiInfo = wifiManager.connectionInfo
                    return intIP2StringIP(wifiInfo.ipAddress)
                }
            } else {
                //当前无网络连接,请在设置中打开网络
                Log.i(TAG, "无网络，请先连接网络")
            }
            return null
        }

        /**
         * 将得到的int类型的IP转换为String类型
         *
         * @param ip
         * @return
         */
        private fun intIP2StringIP(ip: Int): String? {
            return ((ip and 0xFF).toString() + "." + (ip shr 8 and 0xFF) + "."
                    + (ip shr 16 and 0xFF) + "." + (ip shr 24 and 0xFF))
        }

        /**
         * 获取OBU数据类型
         * @param dataKey
         * @return
         */
        fun getOBUType(dataKey: String?): String? {
            try {
                var jsonObject: JSONObject? = JSONObject(dataKey)
                //            JSONArray jsonArray = new JSONArray(dataKey);
//            JSONObject base = jsonObject.getJSONObject("base");
                var keys = jsonObject!!.keys()
                val next = keys.next()
                jsonObject = null
                keys = null
                Log.i(TAG, "getOBUType: $next") // RSM
                return next
            } catch (e: JSONException) {
                Log.i(TAG, "getOBUType:获取OBU数据类型错误 ---> JSONException:$e")
                e.printStackTrace()
            }
            return ""
        }
    }
}