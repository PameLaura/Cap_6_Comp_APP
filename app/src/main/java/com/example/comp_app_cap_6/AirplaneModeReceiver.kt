package com.example.comp_app_cap_6
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
class AirplaneModeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_AIRPLANE_MODE_CHANGED) {
            val message = if (intent.getBooleanExtra("state", false))
                "Modo avión activado: el mapa puede necesitar conexión" else "Modo avión desactivado: verifica tu conexión"
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}
