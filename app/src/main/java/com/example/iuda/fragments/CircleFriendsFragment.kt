package com.example.iuda.fragments

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.iuda.APIservice
import com.example.iuda.AdapterFriends
import com.example.iuda.AmigosDataClass
import com.example.iuda.DataClass
import com.example.iuda.Direction
import com.example.iuda.LocationData
import com.example.iuda.MedicalData
import com.example.iuda.R
import com.example.iuda.SolicitudesDataClass
import com.example.iuda.databinding.FragmentCircleFriendsBinding
import com.example.iuda.iuda.Companion.prefs
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.Exception
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume


class CircleFriendsFragment : Fragment() {
    private lateinit var binding: FragmentCircleFriendsBinding
    private lateinit var amigosList: ArrayList<AmigosDataClass>
    private lateinit var adapter: AdapterFriends
    private lateinit var dataLoc:LocationData
    private lateinit var dialog: BottomSheetDialog
    private var medicalDataListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCircleFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadData()
    }

    private fun setupRecyclerView() {
        val gridLayoutManager = GridLayoutManager(requireContext(), 1)
        binding.recyclerAmigos.layoutManager = gridLayoutManager
        amigosList = ArrayList()
        adapter = AdapterFriends(requireContext(), amigosList)
        binding.recyclerAmigos.adapter = adapter
        val itemAnimator = DefaultItemAnimator()
        binding.recyclerAmigos.itemAnimator = itemAnimator

        clickItem()
    }

    private fun loadData() {
        val pb: ProgressBar = binding.progressBar
        lifecycleScope.launch {
            try {
                pb.visibility = View.VISIBLE
                val result = withContext(Dispatchers.IO) {
                    // Asumiendo que se refactorizó fetchFriendsData para usar las nuevas funciones
                    fetchFriendsData()
                }
                amigosList.clear()
                amigosList.addAll(result)
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Log.e("Error", "Ocurrió un error inesperado: ${e.message}")
            } finally {
                pb.visibility = View.GONE
            }
        }
    }


    // Función principal para obtener datos de amigos
    private suspend fun fetchFriendsData(): List<AmigosDataClass> = suspendCoroutine { continuation ->
        val currentUserID = prefs.getId()
        val amigosRef = FirebaseDatabase.getInstance().getReference("Amigos")
        var amigosList = mutableListOf<AmigosDataClass>()

        amigosRef.child(currentUserID).child("CloseFriends")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val amigosExistentes = ArrayList<SolicitudesDataClass>()

                    for (amigosSnapshot in dataSnapshot.children) {
                        val amigo = amigosSnapshot.getValue(SolicitudesDataClass::class.java)
                        if (amigo != null) {
                            amigosExistentes.add(amigo)
                            fetchUserData(amigo.id ?: "", amigosExistentes, amigosList, amigosRef, currentUserID, continuation)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Error", "Error al obtener solicitudes recibidas: ${databaseError.message}")
                }
            })
    }

    // Función para obtener datos de usuario de Firebase
    private fun fetchUserData(userId: String, amigosExistentes: ArrayList<SolicitudesDataClass>, amigosList: MutableList<AmigosDataClass>, amigosRef: DatabaseReference, currentUserID: String, continuation: Continuation<List<AmigosDataClass>>) {
        val userRef = FirebaseDatabase.getInstance().getReference("Users")

        userRef.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(friendSnapshot: DataSnapshot) {
                    val userData = friendSnapshot.getValue(AmigosDataClass::class.java)
                    if (userData != null) {
                        fetchMedicalData(userData, amigosExistentes, amigosList, amigosRef, currentUserID, continuation)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Error", "Error al obtener datos del usuario: ${error.message}")
                }
            })
    }

    // Función para obtener datos médicos de Firebase
    private fun fetchMedicalData(userData: AmigosDataClass, amigosExistentes: ArrayList<SolicitudesDataClass>, amigosList: MutableList<AmigosDataClass>, amigosRef: DatabaseReference, currentUserID: String, continuation: Continuation<List<AmigosDataClass>>) {
        val medicalDataRef = FirebaseDatabase.getInstance().getReference("MedicalData")
        // Crear una referencia explícita al ValueEventListener.
        medicalDataListener = object : ValueEventListener {
            override fun onDataChange(medicalDataSnapshot: DataSnapshot) {
                val medicalData = medicalDataSnapshot.getValue(MedicalData::class.java)
                if (medicalData != null) {
                    val simplifiedUserData = createSimplifiedUserData(userData, medicalData)
                    // Iniciar una corutina para llamar a la función suspendida.
                    CoroutineScope(Dispatchers.IO).launch {

                        updateLocationData(simplifiedUserData, amigosExistentes, amigosList, amigosRef, currentUserID, continuation)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Error", "Error al obtener datos médicos: ${error.message}")
            }
        }.also {
            medicalDataRef.child(userData.id ?: "").addListenerForSingleValueEvent(it)
        }
    }

    // Función para crear un objeto simplificado con información de amigos y médica
    private fun createSimplifiedUserData(userData: AmigosDataClass, medicalData: MedicalData): AmigosDataClass {
        return AmigosDataClass(
            userData.id,
            userData.username,
            userData.fullName,
            userData.phoneNumber,
            userData.longitude,
            userData.latitude,
            userData.city,
            userData.town,
            userData.road,
            userData.postcode,
            userData.imgUrl,
            userData.gender,
            userData.neighbourhood,
            medicalData.insuranceNumber,
            medicalData.allergies,
            medicalData.medications,
            medicalData.bloodType
        )
    }

    private suspend fun updateLocationData(simplifiedUserData: AmigosDataClass, amigosExistentes: ArrayList<SolicitudesDataClass>, amigosList: MutableList<AmigosDataClass>, amigosRef: DatabaseReference, currentUserID: String, continuation: Continuation<List<AmigosDataClass>>) {
        Log.d("UpdateLocationData", "Inicio de actualización de datos de ubicación para el usuario: ${simplifiedUserData.username}")

        // Intenta buscar la ubicación y captura cualquier posible error.
        val locationData = try {
            searchLocation(simplifiedUserData.longitude.toString(), simplifiedUserData.latitude.toString())
        } catch (e: Exception) {
            Log.e("UpdateLocationData", "Error al buscar la ubicación: ${e.message}")
            null // Retorna null en caso de error.
        }

        if (locationData != null) {
            // Actualiza los datos del usuario con la nueva ubicación.
            simplifiedUserData.city = locationData.city
            simplifiedUserData.town = locationData.townOrSuburb
            simplifiedUserData.road = locationData.road
            simplifiedUserData.postcode = locationData.postcode
            simplifiedUserData.neighbourhood = locationData.neighbourhood

            Log.d("UpdateLocationData", "Datos de ubicación actualizados para ${simplifiedUserData.username}: Ciudad=${locationData.city}, Barrio=${locationData.neighbourhood}")

            amigosList.add(simplifiedUserData)
            Log.d("UpdateLocationData", "Tamaño de la lista de amigos después de agregar: ${amigosList.size}")
            Log.d("UpdateLocationData", "Usuario agregado a la lista: ${simplifiedUserData.username}")

            if (amigosList.size == amigosExistentes.size) {
                Log.d("UpdateLocationData", "Todos los datos de ubicación han sido actualizados. Guardando lista en preferencias.")
                prefs.saveAmigosList(amigosList)
                continuation.resume(amigosList)
                medicalDataListener?.let {
                    Log.d("UpdateLocationData", "Removiendo listener de datos médicos.")
                    amigosRef.child(currentUserID).child("CloseFriends").removeEventListener(it)
                }
            }
        } else {
            Log.e("UpdateLocationData", "Los datos de ubicación no pudieron ser actualizados para el usuario: ${simplifiedUserData.username}")
        }
    }


// Nota: Asumiendo que searchLocation es una función existente que busca la localización basada en longitud y latitud y devuelve un objeto con los datos de la localización.


    private fun clickItem(){
        adapter.setOnItemClickListenner(object :AdapterFriends.OnItemClickListener{
            override fun onItemClick(position: Int, action:String) {
                val selectedUser = amigosList[position]
                val friendId = selectedUser.id
                val friendUser = selectedUser.username

                if(friendId != null && friendUser!= null && selectedUser!= null) {
                    when (action) {
                        "delete" -> {
                            try {
                                DeleteFriend(friendId, friendUser) { exito ->
                                    if (exito) {
                                        Toast.makeText(
                                            requireContext(),
                                            "Amigo Eliminado",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        amigosList.removeAt(position)
                                        adapter.notifyItemRemoved(position)
                                    }
                                }
                            }catch (e:Exception){
                                Toast.makeText(
                                    requireContext(),
                                    "Error inesperado: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }



                        }
                        "details" -> {
                            Log.i("Click","Details")
                            showInfoUser(position)
                        }
                    }
                }
            }
        })
    }


    private fun addFavorito(position: Int) {
        val selectedUser = amigosList[position]
        selectedUser.fullName?.let { nonNullFullName ->
            prefs.saveNameContact(nonNullFullName)
        }
        selectedUser.phoneNumber?.let { nonNullPhoneNumber ->
            prefs.saveNumberContact(nonNullPhoneNumber)
        }
        Log.d("Prefs","Se guradaron las prefs ${prefs.getNameContact()} y ${prefs.getPhoneContact()}")
    }


    private fun showInfoUser(position:Int) {
        val selectedUser = amigosList[position]
        Log.d("DialogInfo","Seleccion: ${selectedUser.username}")
        Log.d("DialogInfo","Ubicación: ${selectedUser.city}")
        val dialogView = layoutInflater.inflate(R.layout.friend_detail,null)
        dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val txtUsername:TextView = dialogView.findViewById(R.id.usernameDetail)
        val txtGender:TextView = dialogView.findViewById(R.id.genderDetail)
        val txtName: TextView = dialogView.findViewById(R.id.nameDetail)
        val txtPhone: TextView = dialogView.findViewById(R.id.phoneDetail)
        val txtCP: TextView = dialogView.findViewById(R.id.cpDetail)
        val txtCity: TextView = dialogView.findViewById(R.id.CityDetail)
        val txtTown: TextView = dialogView.findViewById(R.id.TownDetail)
        val txtRoad: TextView = dialogView.findViewById(R.id.RoadDetail)
        val txtneighbourhood: TextView = dialogView.findViewById(R.id.neighbourhood)
        val imgUser: ImageView = dialogView.findViewById(R.id.profileImageDetail)
        val txtAlergias:TextView = dialogView.findViewById(R.id.AlergiasDetail)
        val txtNSS:TextView = dialogView.findViewById(R.id.NSSDetail)
        val txtMedications:TextView = dialogView.findViewById(R.id.MedicationsDetail)
        val txtTipoSangre:TextView = dialogView.findViewById(R.id.TipoSangre)
        val btnFav:ImageButton = dialogView.findViewById(R.id.btnFavorito)
        val namePrefsCon = prefs.getNameContact()
        val phonePrefsCon = prefs.getPhoneContact()
        selectedUser.imgUrl?.let { imegUrl ->
            Glide.with(requireContext())
                .load(imegUrl)
                .override(100,100)
                .into(imgUser)
        }
        txtGender.text = selectedUser.gender
        txtneighbourhood.text = selectedUser.neighbourhood
        txtUsername.text = selectedUser.username
        txtName.text = selectedUser.fullName
        txtPhone.text = selectedUser.phoneNumber
        txtCP.text = selectedUser.postcode
        txtCity.text = selectedUser.city
        txtRoad.text = selectedUser.road
        txtTown.text = selectedUser.town
        txtAlergias.text = selectedUser.allergies
        txtNSS.text = selectedUser.insuranceNumber
        txtMedications.text = selectedUser.medications
        txtTipoSangre.text = selectedUser.bloodType

        if(namePrefsCon != null && phonePrefsCon!=null){
            btnFav.visibility = View.GONE
        }
        if(namePrefsCon == "" && phonePrefsCon == ""){
            btnFav.visibility = View.VISIBLE
        }
        btnFav.setOnClickListener {
                addFavorito(position)
                btnFav.visibility = View.GONE
            Toast.makeText(requireContext(), "Llenar la informacion faltante en: Contactos de emergencia", Toast.LENGTH_SHORT).show()

        }


        dialog.setContentView(dialogView)
        dialog.show()
    }

    private fun getRetrofit(): Retrofit {
        val gson = GsonBuilder().setLenient().create()
        return Retrofit.Builder()
            .baseUrl("https://us1.locationiq.com/v1/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private suspend fun searchLocation(long: String, lat: String): LocationData {
        return withContext(Dispatchers.IO) {
             // Espera por 1 segundo antes de realizar la solicitud a la API
            try {
                val call = getRetrofit().create(APIservice::class.java)
                    .getInformation("reverse?key=pk.c49e17073bc2d9feecff3c69bd14d63b&lat=$lat&lon=$long&format=json")

                if (call.isSuccessful) {
                    val responseString = call.body()
                    if (responseString != null) {
                        return@withContext formatLocation(responseString)
                    } else {
                        handleUnexpectedError("Respuesta nula desde la API")
                        return@withContext LocationData("", "", "", "", "") // Valor predeterminado en caso de error
                    }
                } else {
                    // Manejar errores HTTP específicos
                    val errorBody = call.errorBody()?.string()
                    handleHttpError(call.code(), errorBody)
                    return@withContext LocationData("", "", "", "", "") // Valor predeterminado en caso de error
                }
            } catch (e: Exception) {
                // Manejar excepciones generales
                e.printStackTrace()
                handleUnexpectedError(e.message ?: "Excepción desconocida")
                return@withContext LocationData("", "", "", "", "") // Valor predeterminado en caso de error
            }
        }
    }


    private fun handleHttpError(code: Int, errorBody: String?) {
        Log.e("HTTPError", "Código de error: $code")
        Log.e("HTTPError", "Cuerpo del error: $errorBody")
        // Puedes mostrar un Toast aquí si lo deseas
    }

    private fun handleUnexpectedError(errorMessage: String) {
        Log.e("Error", "Excepción en la función searchLocation: $errorMessage")
        // Puedes mostrar un Toast aquí si lo deseas
    }


    private fun DeleteFriend (uidFriend: String, usernameFriend: String, onComplete: (Boolean) -> Unit){
        val database = FirebaseDatabase.getInstance()
        val amigosRef = database.getReference("Amigos")
        val myUid = prefs.getId()
        val myUsername = prefs.getUsername()

        val myCF = amigosRef.child(myUid).child("CloseFriends")
        myCF.child(usernameFriend).removeValue()

        val friendCF = amigosRef.child(uidFriend).child("CloseFriends")
        friendCF.child(myUsername).removeValue()

        onComplete(true)
    }

    private fun formatLocation(data: Direction): LocationData {
        val city = data.address.city ?: ""
        val townOrSuburb = data.address.town ?: data.address.suburb ?: ""
        val road = data.address.road ?: ""
        val postcode = data.address.postcode ?: ""
        val neighbourhood = data.address.neighbourhood ?:""

        return LocationData(
            city,
            townOrSuburb,
            road,
            postcode,
            neighbourhood
        )
    }

}



