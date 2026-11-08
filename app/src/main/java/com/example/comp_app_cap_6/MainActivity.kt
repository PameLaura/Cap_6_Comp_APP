package com.example.comp_app_cap_6

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity

open class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LifecycleDebug", "onCreate")
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, FirstFragment()).commit()
    }
    override fun onStart() { super.onStart(); Log.d("LifecycleDebug", "onStart") }
    override fun onResume() { super.onResume(); Log.d("LifecycleDebug", "onResume") }
    override fun onPause() { Log.d("LifecycleDebug", "onPause"); super.onPause() }
    override fun onStop() { Log.d("LifecycleDebug", "onStop"); super.onStop() }
    override fun onRestart() { super.onRestart(); Log.d("LifecycleDebug", "onRestart") }
    override fun onDestroy() { Log.d("LifecycleDebug", "onDestroy"); super.onDestroy() }
}
