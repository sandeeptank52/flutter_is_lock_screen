package com.chihimng.is_lock_screen

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.BinaryMessenger

class IsLockScreenPlugin: FlutterPlugin, MethodCallHandler {
  private lateinit var channel: MethodChannel
  private var applicationContext: Context? = null

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    setupChannel(flutterPluginBinding.binaryMessenger, flutterPluginBinding.applicationContext)
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    teardownChannel()
  }

  private fun setupChannel(messenger: BinaryMessenger, context: Context) {
    channel = MethodChannel(messenger, "com.chihimng.is_lock_screen")
    applicationContext = context
    channel.setMethodCallHandler(this)
  }

  private fun teardownChannel() {
    channel.setMethodCallHandler(null)
    applicationContext = null
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
      "isLockScreen" -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          val mActivityManager = applicationContext?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
          val tasks = mActivityManager.runningAppProcesses
          if (tasks != null && tasks.size > 0) {
            val processInfo = tasks[0]
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
              val lockScreenState = processInfo.importanceReasonCode
              result.success(lockScreenState == ActivityManager.RunningAppProcessInfo.REASON_UNKNOWN)
              return
            }
          }
          result.success(true)
        } else {
          // not supported, always return false
          result.success(false)
        }
      }
      else -> result.notImplemented()
    }
  }

  // This static function is optional and equivalent to onAttachedToEngine.
  // It supports the old plugin registration mechanism.
  companion object {
    @JvmStatic
    fun registerWith(registrar: io.flutter.plugin.common.PluginRegistry.Registrar) {
      val instance = IsLockScreenPlugin()
      instance.setupChannel(registrar.messenger(), registrar.context())
    }
  }
}