package com.example.comp_app_cap_6

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.MapaJuliacaFragment : Fragment(R.layout.fragment_mapa_juliaca) {
    private var map: MapView? = null
    private val receiver = AirplaneModeReceiver()
    private val photoPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) readPhoto() else toast("Permiso de fotos denegado")
    }
    private val notifications = registerForActivityResult(ActivityResultContracts.RequestPermission()) { startMusic() }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osm_pref", Context.MODE_PRIVATE))
            userAgentValue = "UNAJ-Juliaca-Capitulo6/1.0"
            // MAPNIK usa el User-Agent normalizado; osmdroid aplica estos encabezados después.
            additionalHttpRequestProperties["User-Agent"] = userAgentValue
            setOsmdroidBasePath(java.io.File(context.cacheDir, "osmdroid"))
            // Start a fresh cache so tiles from the old 403 responses are not reused.
            setOsmdroidTileCache(java.io.File(getOsmdroidBasePath(), "tiles-identified"))
        }
        Log.d("FragmentLifecycle", "onAttach")
    }
    override fun onCreate(state: Bundle?) { super.onCreate(state); Log.d("FragmentLifecycle", "onCreate") }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.d("FragmentLifecycle", "onCreateView")
        return inflater.inflate(R.layout.fragment_mapa_juliaca, container, false)
    }
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Log.d("FragmentLifecycle", "onViewStateRestored")
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("FragmentLifecycle", "onViewCreated")
        map = view.findViewById<MapView>(R.id.mapOsm).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(savedInstanceState?.getDouble("zoom", 16.0) ?: 16.0)
            controller.setCenter(GeoPoint(savedInstanceState?.getDouble("lat", -15.4967) ?: -15.4967,
                savedInstanceState?.getDouble("lon", -70.1333) ?: -70.1333))
            overlays.add(Marker(this).apply {
                position = GeoPoint(-15.4967, -70.1333)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Plaza de Armas de Juliaca"
            })
        }
        fun button(id: Int, action: () -> Unit) { view.findViewById<Button>(id).setOnClickListener { action() } }
        button(R.id.btnPlay) {
            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                notifications.launch(Manifest.permission.POST_NOTIFICATIONS)
            else startMusic()
        }
        button(R.id.btnStop) { requireContext().stopService(Intent(requireContext(), MusicService::class.java)) }
        button(R.id.btnExplicitIntent) { details(false) }
        button(R.id.btnAnimacion) { details(true) }
        button(R.id.btnImplicitIntent) {
            val geoUri = android.net.Uri.parse("geo:-15.4925,-70.1319?q=Plaza+de+Armas+Juliaca")
            val intent = Intent(Intent.ACTION_VIEW, geoUri)
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                try { startActivity(intent) }
                catch (_: ActivityNotFoundException) { toast("No se encontró una aplicación de mapas") }
            } else toast("No se encontró una aplicación de mapas")
        }
        button(R.id.btnMediaStore) {
            val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) readPhoto()
            else photoPermission.launch(permission)
        }
        view.findViewById<View>(R.id.controls).apply { alpha = 0f; animate().alpha(1f).setDuration(300).start() }
    }
    private fun startMusic() { ContextCompat.startForegroundService(requireContext(), Intent(requireContext(), MusicService::class.java)) }
    private fun details(animated: Boolean) {
        val options = if (animated) android.app.ActivityOptions.makeCustomAnimation(requireContext(), R.anim.slide_in_right, R.anim.slide_out_left).toBundle() else null
        startActivity(Intent(requireContext(), DetailActivity::class.java).putExtra("UBICACION", "Plaza de Armas de Juliaca"), options)
    }
    private fun readPhoto() {
        val resolver = requireContext().contentResolver
        viewLifecycleOwner.lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.SIZE), null, null,
                        "${MediaStore.Images.Media.DATE_ADDED} DESC")?.use {
                        if (it.moveToFirst()) "Última foto accesible: ${it.getString(0)} (${it.getLong(1) / 1024} KB)"
                        else "No hay fotos accesibles en la galería"
                    } ?: "No se pudo consultar la galería"
                } catch (_: Exception) { "No se pudo consultar la galería; revisa los permisos" }
            }
            toast(result)
        }
    }
    private fun toast(message: String) { context?.let { Toast.makeText(it, message, Toast.LENGTH_LONG).show() } }
    override fun onStart() { super.onStart(); ContextCompat.registerReceiver(requireContext(), receiver, IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED), ContextCompat.RECEIVER_NOT_EXPORTED); Log.d("FragmentLifecycle", "onStart") }
    override fun onResume() { super.onResume(); map?.onResume(); Log.d("FragmentLifecycle", "onResume") }
    override fun onPause() { map?.onPause(); Log.d("FragmentLifecycle", "onPause"); super.onPause() }
    override fun onStop() { requireContext().unregisterReceiver(receiver); Log.d("FragmentLifecycle", "onStop"); super.onStop() }
    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("FragmentLifecycle", "onSaveInstanceState")
        map?.let { outState.putDouble("lat", it.mapCenter.latitude); outState.putDouble("lon", it.mapCenter.longitude); outState.putDouble("zoom", it.zoomLevelDouble) }
        super.onSaveInstanceState(outState)
    }
    override fun onDestroyView() { map?.onDetach(); map = null; Log.d("FragmentLifecycle", "onDestroyView"); super.onDestroyView() }
    override fun onDestroy() { Log.d("FragmentLifecycle", "onDestroy"); super.onDestroy() }
    override fun onDetach() { Log.d("FragmentLifecycle", "onDetach"); super.onDetach() }
}

