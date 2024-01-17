package com.wifive.ypasslibtest

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.wifive.ypasslib.YPass
//import com.wifive.ypasslib.YPass
import java.lang.Exception

class MainActivity :  AppCompatActivity(){
    val ypass = YPass();
    var startScan = false
    private var intentVar: Intent? = null


    private val SYSTEM_ALERT_WINDOW_PERMISSION_CODE = 1001
    private val IGNORE_BATTERY_OPTIMIZATIONS_REQUEST_CODE = 1002

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)


        requestBatteryOptimizations()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 안드로이드 6.0 이상에서는 권한을 동적으로 요청
            if (!Settings.canDrawOverlays(this)) {
                requestOverlayPermission()
            }
        }


        checkBluetoothAdvertisePermission()

//
        val testBtn = findViewById<Button>(R.id.testBtn)
//        Info.cidView = findViewById<TextView>(R.id.cid)
//        Info.rssiView = findViewById<TextView>(R.id.rssi)

        testBtn.setOnClickListener {
            if(startScan) {
                startScan = false
//                Info.isScaning = false
                ypass.stopScan()
                Thread.sleep(1000)
                testBtn.text = "스캔 시작"
                findViewById<TextView>(R.id.cid).text = "cloberID : "
                findViewById<TextView>(R.id.rssi).text = "rssi : "
            } else {
                testBtn.text = "스캔 정지"

                ypass.startScan(applicationContext, MainActivity::class.java)
                startScan()
                startScan = true
            }
        }

        findViewById<Button>(R.id.maxBtn).setOnClickListener {
            try {
                ypass.setMaxRssi(findViewById<EditText>(R.id.maxText).text.toString().toDouble())
                findViewById<EditText>(R.id.maxText).setText(null)
            } catch (e: Exception) {
                Log.d("TAG", "숫자만 입력")
            }
        }


        findViewById<Button>(R.id.minBtn).setOnClickListener {
            try {
                ypass.setMinRssi(findViewById<EditText>(R.id.minText).text.toString().toDouble())
                findViewById<EditText>(R.id.minText).setText(null)
            } catch (e: Exception) {
                Log.d("TAG", "숫자만 입력")
            }
        }


        ypass.setMinRssi(-75.0)
        ypass.setMaxRssi(0.0)

    }

    private fun startScan() {
        val handler = Handler(Looper.getMainLooper())
        Thread {
            while (startScan) {

                val cloberScanData = ypass.getProxyInfo()


                Log.d("TAG", "cloberID : " + cloberScanData[0]);
                Log.d("TAG", "rssi: " + cloberScanData[1]);


                handler.post {
//                    if (openCheck) {
//                        findViewById<TextView>(R.id.cid).text = "출입문 Opeen"
//                        findViewById<TextView>(R.id.rssi).text = ""
//                    } else {
                    findViewById<TextView>(R.id.cid).text = "cloberID : " + cloberScanData[0]
                    findViewById<TextView>(R.id.rssi).text = "rssi : " + cloberScanData[1]
//                    }
                }

                if(!cloberScanData[0].equals("null")) {
                    if(ypass.reqOpenDoor(cloberScanData[0] as String)) {
                        Log.d("TAG", "출입문 Opeen 성공")

                    } else {

                        Log.d("TAG", "출입문 Opeen 실패")
                    }
                }


                Thread.sleep(1000)
            }


        }.start()
    }




    private val REQUEST_PERMISSION_CODE = 123
    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkBluetoothAdvertisePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW)
            != PackageManager.PERMISSION_GRANTED) {
            // 권한이 부여되지 않았을 경우 권한 요청
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.SYSTEM_ALERT_WINDOW),
                REQUEST_PERMISSION_CODE
            )
        }
    }


    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION_CODE)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == SYSTEM_ALERT_WINDOW_PERMISSION_CODE) {
//            if (Settings.canDrawOverlays(this)) {
//                Log.d("TAG", "onActivityResult: 11111")
//                // 권한이 부여되었음
//                // 권한이 부여된 후에 원하는 동작 수행
//            } else {
//                Log.d("TAG", "onActivityResult: 22222")
//                // 사용자가 권한을 부여하지 않음
//                // 사용자에게 안내 메시지 표시 등의 처리 수행
//            }
//        }
//    }

    private fun requestBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = packageName
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:$packageName")

            if (!isIgnoringBatteryOptimizations()) {
                startActivityForResult(intent, IGNORE_BATTERY_OPTIMIZATIONS_REQUEST_CODE)
            }
        }
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager?
        return powerManager?.isIgnoringBatteryOptimizations(packageName) == true
    }


}