package com.example.iuda.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.iuda.AmigosDataClass
import com.example.iuda.R
import com.example.iuda.iuda.Companion.prefs
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var myButton: ImageButton
    private val userMarkers = HashMap<String, Marker>()

    companion object {
        const val REQUEST_CODE_LOCATION = 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createFragment()
        val name = prefs.getName()
        val welcomeUserTextView: TextView = view.findViewById(R.id.txt_welcomeName)
        welcomeUserTextView.text = "Hola, $name"
        myButton = view.findViewById(R.id.btnMyLocation)
        myButton.setOnClickListener {
            Log.d("Debug","Ver mi localización")
            focusOnMyLocation()
        }
    }

    private fun createFragment() {
        val mapFragment: SupportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // Nuevo método para ajustar el botón de "Mi ubicación"

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        fetchAndShowFriendsOnMap() // Nuevo: Escuchar los cambios de usuarios para actualizar los marcadores
        obtenerLocalizacion()
        enableLocation()
    }



    private fun fetchAndShowFriendsOnMap() {
        // Obtener la referencia al nodo "Amigos" del usuario actual
        val currentUserFriendsRef = FirebaseDatabase.getInstance().getReference("Amigos/${prefs.getId()}/CloseFriends")

        // Escuchar cambios específicos en los amigos cercanos
        currentUserFriendsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // Esta sección se ejecuta cuando se añade un nuevo amigo a la base de datos
                val friendId = dataSnapshot.child("id").getValue(String::class.java) ?: return
                fetchAndDisplayFriendLocation(friendId)
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                // Esta sección se ejecuta cuando se elimina un amigo de la base de datos
                // Aquí deberías eliminar el marcador correspondiente del mapa
                val friendId = dataSnapshot.child("id").getValue(String::class.java) ?: return
                // Suponiendo que guardas una referencia de los marcadores por ID del amigo
                userMarkers[friendId]?.remove() // Eliminar marcador del mapa
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Esta sección se ejecuta cuando la información de un amigo existente cambia
                // Aquí podrías actualizar la ubicación del marcador si es necesario
                val friendId = snapshot.child("id").getValue(String::class.java) ?: return
                fetchAndDisplayFriendLocation(friendId)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Este método se llama si uno de los amigos cambia de posición en la lista
                // No es comúnmente necesario para la lógica de marcadores
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar errores de la base de datos
                Log.e("Firebase", "Error al obtener amigos cercanos: ${databaseError.message}")
            }

            private fun fetchAndDisplayFriendLocation(friendId: String) {
                val friendUserRef = FirebaseDatabase.getInstance().getReference("Users/$friendId")
                friendUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(friendDataSnapshot: DataSnapshot) {

                        if (!isAdded || context == null) {
                            return
                        }
                        // Obtener la información del amigo
                        val friendUsername = friendDataSnapshot.child("username").getValue(String::class.java)
                        val friendLatitude = friendDataSnapshot.child("latitude").getValue(Double::class.java)
                        val friendLongitude = friendDataSnapshot.child("longitude").getValue(Double::class.java)
                        val friendImgUrl = friendDataSnapshot.child("imgUrl").getValue(String::class.java)

                        // Validar que la latitud y longitud no sean nulas
                        if (friendLatitude != null && friendLongitude != null) {
                            // Convertir la latitud y longitud a un objeto LatLng
                            val friendLocation = LatLng(friendLatitude, friendLongitude)

                            // Si deseas utilizar una imagen personalizada para el marcador, primero debes cargarla
                            // Por ejemplo, usando Glide para cargar la imagen desde una URL
                            if (friendImgUrl != null) {
                                Glide.with(requireContext()).asBitmap().load(friendImgUrl)
                                    .apply(RequestOptions.circleCropTransform().override(120,120))
                                    .into(object : CustomTarget<Bitmap>() {
                                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                            // Una vez la imagen está cargada y lista, crea el marcador con la imagen personalizada
                                            val markerOptions = MarkerOptions()
                                                .position(friendLocation)
                                                .title(friendUsername)
                                                .icon(BitmapDescriptorFactory.fromBitmap(resource)) // Usa la imagen cargada como ícono
                                            map.addMarker(markerOptions)
                                        }

                                        override fun onLoadCleared(placeholder: Drawable?) {
                                            // Aquí puedes manejar si necesitas hacer algo cuando la imagen es removida o no disponible
                                        }
                                    })
                            } else {
                                // Si no hay URL de imagen, simplemente añade el marcador sin imagen personalizada
                                val markerOptions = MarkerOptions()
                                    .position(friendLocation)
                                    .title(friendUsername)
                                map.addMarker(markerOptions)
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("Firebase", "Error al obtener datos del amigo: ${databaseError.message}")
                    }
                })
            }

        })
    }


    private fun isLocationPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun enableLocation() {
        if (isLocationPermissionsGranted()) {
            map.isMyLocationEnabled = true
            focusOnMyLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun focusOnMyLocation() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val myLatLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 10f),2500,null)
            }
        }
    }

    private fun obtenerLocalizacion() {
        val locationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userRef = FirebaseDatabase.getInstance().getReference("Users")
                val myUID = prefs.getId()
                val updates = mapOf(
                    "latitude" to location.latitude,
                    "longitude" to location.longitude
                )

                userRef.child(myUID).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            userRef.child(myUID).updateChildren(updates)
                        } else {
                            Log.d("Debug", "No se encontró el usuario en la base de datos")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            requireContext(),
                            "Algo salio mal",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
        }
    }


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