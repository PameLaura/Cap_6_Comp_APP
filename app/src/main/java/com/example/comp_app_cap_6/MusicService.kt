package com.example.comp_app_cap_6

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast

class MusicService : Service() {
    private var player: MediaPlayer? = null
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") { stopSelf(); return START_NOT_STICKY }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("music", "Reproducción de audio", NotificationManager.IMPORTANCE_LOW))
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val stop = PendingIntent.getService(this, 1, Intent(this, MusicService::class.java).setAction("STOP"), PendingIntent.FLAG_IMMUTABLE)
        startForeground(1, Notification.Builder(this, "music")
            .setSmallIcon(android.R.drawable.ic_media_play).setContentTitle("Capítulo 6 · Audio")
            .setContentText("Reproduciendo el tono del sistema").setContentIntent(open)
            .addAction(Notification.Action.Builder(null, "Detener", stop).build()).setOngoing(true).build())
        try {
            if (player == null) {
                player = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI)
                player?.isLooping = true
                player?.setOnErrorListener { _, _, _ -> stopSelf(); true }
            }
            if (player == null) error("No hay tono disponible")
            player?.start()
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo reproducir el tono del sistema", Toast.LENGTH_LONG).show()
            stopSelf()
        }
        return START_NOT_STICKY
    }
    override fun onDestroy() {
        player?.release(); player = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
}
