package com.example.comp_app_cap_6
import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import androidx.activity.ComponentActivity
class DetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        findViewById<TextView>(R.id.txtDetalleUbicacion).text = intent.getStringExtra("UBICACION") ?: "Juliaca"
        findViewById<Button>(R.id.btnVolver).setOnClickListener { finish() }
    }
}
