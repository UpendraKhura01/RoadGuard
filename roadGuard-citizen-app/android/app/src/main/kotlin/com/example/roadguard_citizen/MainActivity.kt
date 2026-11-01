package com.example.roadguard_citizen

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val channelName = "roadguard/security"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channelName).setMethodCallHandler { call, result ->
            when (call.method) {
                "isVpnActive" -> result.success(isVpnActive())
                "openVpnSettings" -> {
                    try {
                        startActivity(Intent(Settings.ACTION_VPN_SETTINGS))
                        result.success(true)
                    } catch (exception: Exception) {
                        result.error("VPN_SETTINGS_UNAVAILABLE", exception.message, null)
                    }
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun isVpnActive(): Boolean {
        val connectivity = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivity.activeNetwork ?: return false
        val capabilities = connectivity.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
    }
}
