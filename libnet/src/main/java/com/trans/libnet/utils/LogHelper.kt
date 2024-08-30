package com.trans.libnet.utils

import android.os.Environment
import android.util.Log
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * @author Tom灿
 * @description:
 * @date :2024/8/26 10:29
 */
class LogHelper {
    private val mTAG = "LogHelper"
    private val mRoot = Environment.getExternalStorageDirectory().absolutePath
    private val mLogInfoPath = "$mRoot/trans/v2/config"
    private val mLogFileName = "logcat.log"
    private val mLogZipName = "logcat.zip"
    private val mLogFilePath = "$mLogInfoPath/$mLogFileName"
    private val mLogZipPath = "$mLogInfoPath/$mLogZipName"
    private val mCommand = "logcat -d"
    private val mPID = android.os.Process.myPid()
    private val mLevel = "logcat *:e *:w | grep \"(" + mPID + ")\""

    fun getLogInfoForProcess() {
        val processBuilder = ProcessBuilder("logcat", "-d", "*:S", "com.trans.routepush:V");
        val process = processBuilder.start()
        writeLogInfo(process.inputStream)
        val exitCode = process.waitFor()
        if (exitCode == 0) {
            Log.d("LogCapture", "Logcat captured successfully.");
        } else {
            Log.e("LogCapture", "Failed to capture logcat.");
        }
    }

    fun getLogInfo() {
        Runtime.getRuntime().exec(mCommand).apply {
            writeLogInfo(inputStream)
            waitFor()
            destroy()
        }
        zipFile(ArrayList<String>().apply {
            add(mLogFilePath)
            add("$mLogInfoPath/RouteMatch2.txt")
        }, mLogZipPath)
    }

    private fun writeLogInfo(inputStream: InputStream) {
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val fileOutputStream = FileOutputStream(File(mLogInfoPath, mLogFileName))
        val bufferedWriter = BufferedWriter(OutputStreamWriter(fileOutputStream))
        val buffer = CharArray(512)
        var len = -1
        while (bufferedReader.read(buffer).also { len = it } != -1) {
            bufferedWriter.write(buffer, 0, len)
        }
        bufferedWriter.flush()
        bufferedWriter.close()
        bufferedReader.close()
    }

    private fun zipFile(filePath: String, zipFilePath: String) {
        val fileFile = File(filePath)
        ZipOutputStream(FileOutputStream(File(zipFilePath), true)).apply {
            putNextEntry(ZipEntry(fileFile.name))
            val bufferedReader = FileInputStream(fileFile)
            val buffer = ByteArray(512)
            var len = -1
            while (bufferedReader.read(buffer).also { len = it } != -1) {
                write(buffer, 0, len)
            }
            closeEntry()
            close()
            bufferedReader.close()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun zipFile(filePaths: ArrayList<String>, zipFilePath: String) {
        ZipOutputStream(FileOutputStream(File(zipFilePath), true)).apply {
            (0..<filePaths.size).forEach { i ->
                val fileFile = File(filePaths[i])
                if (!fileFile.exists()) return@forEach
                try {
                    putNextEntry(ZipEntry(fileFile.name))
                    val bufferedReader = FileInputStream(fileFile)
                    val buffer = ByteArray(512)
                    var len = -1
                    while (bufferedReader.read(buffer).also { len = it } != -1) {
                        write(buffer, 0, len)
                    }
                    closeEntry()
                    bufferedReader.close()
                } catch (e: Exception) {
                    Log.e(mTAG, "zipFile: $e")
                }
            }
            close()
        }
    }
}