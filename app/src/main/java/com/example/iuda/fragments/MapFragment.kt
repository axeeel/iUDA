package com.example.iuda.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.iuda.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap

    // Definición de una constante para identificar la solicitud de permisos de ubicación
    companion object {
        const val REQUEST_CODE_LOCATION = 0
    }

    // onCreateView se llama cuando se crea la vista del fragmento
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla el diseño del fragmento llamado R.layout.fragment_map
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    // onViewCreated se llama después de que se ha creado la vista
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Llama a la función createFragment
        createFragment()
    }

    // Esta función busca un fragmento de mapa y lo asocia con Google Maps
    private fun createFragment() {
        val mapFragment: SupportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // Callback que se llama cuando el mapa de Google está listo
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        // Llama a la función enableLocation para habilitar la ubicación
        enableLocation()
    }

    // Agrega un marcador en una ubicación específica en el mapa
    private fun createMarker() {
        val coordinates = LatLng(19.330411456010133, -99.11199722759162)
        map.addMarker(MarkerOptions().position(coordinates).title("ESIME"))
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(coordinates, 10f),
            2500,
            null
        )
    }

    // Verifica si se han concedido permisos de ubicación
    private fun isLocationPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    // Habilita la capa de ubicación en el mapa o solicita permisos si no se han concedido
    private fun enableLocation() {
        if (isLocationPermissionsGranted()) {
            map.isMyLocationEnabled = true
            // Llama a la función focusOnMyLocation para centrar el mapa en la ubicación del dispositivo
            focusOnMyLocation()
        } else {
            // Llama a la función requestLocationPermission para solicitar permisos
            requestLocationPermission()
        }
    }

    // Centra el mapa en la ubicación del dispositivo
    private fun focusOnMyLocation() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val myLatLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 13f), 2500, null)
            }
        }
    }

    // Solicita permisos de ubicación al usuario
    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            Toast.makeText(requireContext(), "Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
        }
    }

    // Callback que se llama cuando el usuario responde a la solicitud de permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                map.isMyLocationEnabled = true
            } else {
                Toast.makeText(
                    requireContext(),
                    "Para activar la localización ve a ajustes y acepta los permisos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
