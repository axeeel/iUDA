package com.example.iuda.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.iuda.DataClass
import com.example.iuda.MyAdapter
import com.example.iuda.R
import com.example.iuda.databinding.FragmentAddFriendBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.appcompat.widget.SearchView
import com.example.iuda.iuda.Companion.prefs
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.example.iuda.AdapterSolicitud
import com.example.iuda.SolicitudesDataClass
import com.google.android.material.bottomsheet.BottomSheetDialog


class AddFriendFragment : Fragment() {
    private lateinit var binding: FragmentAddFriendBinding
    private lateinit var dataList: ArrayList<DataClass>
    private lateinit var adapter: MyAdapter
    private lateinit var usuariosLista:ArrayList<DataClass>
    private lateinit var adapterSolicitudes:AdapterSolicitud
     var databaseReference: DatabaseReference? = null
    var eventListener: ValueEventListener? = null
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var solicitudesEnviadas: List<String>
    private lateinit var dialog: BottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el diseño del fragmento usando el enlace de datos
        binding = FragmentAddFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Configurar el RecyclerView
        val gridLayoutManager = GridLayoutManager(requireContext(), 1)
        binding.recyclerView1.layoutManager = gridLayoutManager

        dataList = ArrayList() // Inicializar dataList como una lista vacía
        adapter = MyAdapter(requireContext(), dataList)
        binding.recyclerView1.adapter = adapter

        val itemAnimator = DefaultItemAnimator()
        binding.recyclerView1.itemAnimator = itemAnimator

        // Configurar el Listener para clics en elementos del RecyclerView
        adapter.setOnItemClickListener(object : MyAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                // Obtener el usuario seleccionado
                val selectedUser = dataList[position]
                val friendUid = selectedUser.id
                val friendUsername = selectedUser.username
                if(friendUid != null && friendUsername != null) {

                    AddFriendFB(friendUid,friendUsername){exito ->
                        if(exito){
                            Toast.makeText(
                                requireContext(),
                                "Se agrego correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            adapter.setButtonPressed(position)
                        } else{
                            Toast.makeText(
                                requireContext(),
                                "Ya existe una solicitud entre ustedes",
                                Toast.LENGTH_SHORT
                            ).show()
                            //codigo para borrar del listado
                            dataList.removeAt(position)
                            adapter.notifyItemRemoved(position)
                        }
                    }
                }

            }
        })

        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isNotEmpty()) {
                    // Realizar la búsqueda cuando el texto cambia
                    performSearch(newText)
                } else {
                    // Si el texto está vacío, dataList también debería estar vacío
                    dataList.clear()
                    adapter.notifyDataSetChanged()
                }
                return true
            }
        })



       val btnSolicitudes = binding.btnRequests
       btnSolicitudes.setOnClickListener {
           showBottomDialog()
       }
    }





    private fun showBottomDialog() {
        Log.d("Debug", "Entrando a showBottomDialog()")
        val dialogView = layoutInflater.inflate(R.layout.bottomsheetfriend, null)
        dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog.setContentView(dialogView)
        val recyclerViewSolicitudes = dialog.findViewById<RecyclerView>(R.id.recyclerViewSolicitudes)
        val gridLayoutManager2 = GridLayoutManager(requireContext(), 1)

        recyclerViewSolicitudes?.layoutManager = gridLayoutManager2
        usuariosLista = ArrayList()
        adapterSolicitudes = AdapterSolicitud(requireContext(), usuariosLista)
        recyclerViewSolicitudes?.adapter = adapterSolicitudes
        val itemAnimator2 = DefaultItemAnimator()
        recyclerViewSolicitudes?.itemAnimator = itemAnimator2

        // Llamar a la función para obtener y mostrar las solicitudes recibidas
        //obtenerSolicitudesRecibidas()
        val amigosRef = FirebaseDatabase.getInstance().getReference("Amigos")
        val userRef = FirebaseDatabase.getInstance().getReference("Users")
        val myUID = prefs.getId()

        amigosRef.child(myUID).child("SolicitudesRecibidas")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val solicitudesRecibidasLista = ArrayList<SolicitudesDataClass>()

                    // Paso 1: Obtener las solicitudes recibidas
                    for (solicitudSnapshot in dataSnapshot.children) {
                        Log.d("Debug", "DataSnapshot: $solicitudSnapshot")
                        val solicitud = solicitudSnapshot.getValue(SolicitudesDataClass::class.java)
                        if (solicitud != null) {
                            solicitudesRecibidasLista.add(solicitud)
                            //if(solicitudSnapshot.key == "SolicitudesRecibidas"){
                                Log.d("Debug", "Entró en la condición SolicitudesRecibidas")
                                // Paso 2: Obtener información de usuario para cada solicitud
                                userRef.child(solicitud.id ?: "")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(userSnapshot: DataSnapshot) {
                                            val userData = userSnapshot.getValue(DataClass::class.java)
                                            if (userData != null) {
                                                // Paso 3: Almacenar la información del usuario en tu lista final
                                                val simplifiedUserData = DataClass(
                                                    userData.id,
                                                    userData.username,
                                                    userData.fullName,
                                                    userData.gender,
                                                    userData.imgUrl
                                                )


                                                usuariosLista.add(simplifiedUserData)
                                                adapterSolicitudes.setDataList(usuariosLista)
                                                adapterSolicitudes.notifyDataSetChanged()

                                                // Puntos de interrupción y mensajes de registro
                                                Log.d("Debug", "Tamaño de usuariosLista: ${usuariosLista.size}")
                                                Log.d("Debug", "Usuario agregado a la lista: ${simplifiedUserData.username}")
                                            }
                                        }

                                        override fun onCancelled(userError: DatabaseError) {
                                            // Manejar errores de lectura de datos del usuario
                                            Log.e("Error", "Error al obtener datos del usuario: ${userError.message}")
                                        }
                                    })

                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores de lectura de datos
                    Log.e("Error", "Error al obtener solicitudes recibidas: ${databaseError.message}")
                }
            })
        dialog.show()

        adapterSolicitudes.setOnItemClickListener(object : AdapterSolicitud.OnItemClickListener{
            override fun onItemClick(position: Int, action: String) {
                val selectedUser = usuariosLista[position]
                val friendUid = selectedUser.id
                val friendUsername = selectedUser.username

                when(action){
                    "accept" -> {
                        if(friendUid != null && friendUsername != null){
                            AceptarSolicitudDeAmistad(friendUid,friendUsername){exitoso ->
                                if(exitoso){
                                    Toast.makeText(
                                        requireContext(),
                                        "Ya son amigos",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    usuariosLista.removeAt(position)
                                    adapterSolicitudes.notifyItemRemoved(position)
                                }

                            }
                        }
                    }
                    "decline" -> {
                        if(friendUid != null && friendUsername != null){
                            RechazarSolicitudDeAmistad(friendUid,friendUsername){exito ->
                                if(exito){
                                    Toast.makeText(
                                        requireContext(),
                                        "Solicitud Rechazada",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    usuariosLista.removeAt(position)
                                    adapterSolicitudes.notifyItemRemoved(position)
                                }
                            }
                        }


                    }
                }


            }
        })
    }


    private fun RechazarSolicitudDeAmistad(uidAmigo: String, usernameAmigo: String, onComplete: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val amigosRef = database.getReference("Amigos")
        val myUid = prefs.getId()
        val myUsername = prefs.getUsername()

        // Eliminar la solicitud de amistad enviada
        val solicitudesEnviadasRef = amigosRef.child(uidAmigo).child("SolicitudesEnviadas")
        solicitudesEnviadasRef.child(myUsername).removeValue()

        // Eliminar la solicitud de amistad recibida
        val solicitudesRecibidasRef = amigosRef.child(myUid).child("SolicitudesRecibidas")
        solicitudesRecibidasRef.child(usernameAmigo).removeValue()

        // Llamada de retorno indicando que la operación fue exitosa
        onComplete(true)
    }


    private fun AceptarSolicitudDeAmistad(uidAmigo: String, usernameAmigo: String, onComplete: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val amigosRef = database.getReference("Amigos")
        val myUid = prefs.getId()
        val myUsername = prefs.getUsername()

        // Crear referencia al objeto 'CloseFriends' para ambos usuarios
        val closeFriendsRefForUser = amigosRef.child(myUid).child("CloseFriends").child(usernameAmigo)
        val closeFriendsRefForFriend = amigosRef.child(uidAmigo).child("CloseFriends").child(myUsername)

        // Verificar si ya son amigos antes de agregar a 'CloseFriends'
        val amigosRefForUser = amigosRef.child(myUid).child("Amigos")
        amigosRefForUser.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild(usernameAmigo)) {
                    // Ya son amigos, no es necesario agregar a 'CloseFriends' nuevamente
                    onComplete(false)
                } else {
                    // Verificar si ya existe la solicitud de amistad antes de aceptarla
                    val solicitudEnvidadasRef = amigosRef.child(uidAmigo).child("SolicitudesEnviadas")
                    val solicitudesRecibidasRef = amigosRef.child(myUid).child("SolicitudesRecibidas")
                    solicitudesRecibidasRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(recibidasSnapshot: DataSnapshot) {
                            if (recibidasSnapshot.hasChild(usernameAmigo)) {
                                // Aceptar la solicitud de amistad
                                // Agregar a 'CloseFriends' para ambos usuarios
                                closeFriendsRefForUser.child("id").setValue(uidAmigo)
                                closeFriendsRefForFriend.child("id").setValue(myUid)

                                // Eliminar la solicitud recibida
                                solicitudesRecibidasRef.child(usernameAmigo).removeValue()
                                solicitudEnvidadasRef.child(myUsername).removeValue()

//                                // Agregar a la lista de amigos para ambos usuarios
//                                amigosRefForUser.child(usernameAmigo).child("id").setValue(uidAmigo)
//                                amigosRef.child(uidAmigo).child("Amigos").child(myUsername).child("id").setValue(myUid)

                                // Llamada de retorno indicando que la operación fue exitosa
                                onComplete(true)
                            } else {
                                // La solicitud de amistad no existe, no se puede aceptar
                                onComplete(false)
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Manejar errores de lectura de datos
                            onComplete(false)
                        }
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar errores de lectura de datos
                onComplete(false)
            }
        })
    }



    private fun AddFriendFB(uidAmigo: String, usernameAmigo: String, onComplete: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val amigosRef = database.getReference("Amigos")
        val myUid = prefs.getId()
        val myUsername = prefs.getUsername()

        // Verificar si ya son amigos en el objeto 'CloseFriends'
        val closeFriendsRef = amigosRef.child(myUid).child("CloseFriends")
        closeFriendsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(closeFriendsSnapshot: DataSnapshot) {
                if (closeFriendsSnapshot.hasChild(usernameAmigo)) {
                    // Ya son amigos, no se puede enviar la solicitud nuevamente
                    onComplete(false)
                } else {
                    // Verificar si la solicitud ya existe antes de insertar (Solicitudes Enviadas)
                    val solicitudesEnviadasRef = amigosRef.child(myUid).child("SolicitudesEnviadas")
                    solicitudesEnviadasRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.hasChild(usernameAmigo)) {
                                // Ya existe la solicitud enviada, no es necesario agregarla nuevamente
                                onComplete(false)
                            } else {
                                // Verificar si la solicitud ya existe antes de insertar (Solicitudes Recibidas)
                                val solicitudesRecibidasRef = amigosRef.child(uidAmigo).child("SolicitudesRecibidas")
                                solicitudesRecibidasRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(recibidasSnapshot: DataSnapshot) {
                                        if (recibidasSnapshot.hasChild(myUsername)) {
                                            // Ya existe la solicitud recibida, no es necesario agregarla nuevamente
                                            onComplete(false)
                                        } else {
                                            // Verificar si ya se envió una solicitud en sentido inverso
                                            val solicitudesRecibidasInversasRef = amigosRef.child(myUid).child("SolicitudesRecibidas")
                                            solicitudesRecibidasInversasRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(recibidasInversasSnapshot: DataSnapshot) {
                                                    if (recibidasInversasSnapshot.hasChild(usernameAmigo)) {
                                                        // Ya existe la solicitud inversa, no es necesario agregarla nuevamente
                                                        onComplete(false)
                                                    } else {
                                                        // Agregar la solicitud enviada
                                                        solicitudesEnviadasRef.child(usernameAmigo).child("id").setValue(uidAmigo)

                                                        // Agregar la solicitud recibida para el amigo
                                                        solicitudesRecibidasRef.child(myUsername).child("id").setValue(myUid)

                                                        // Llamada de retorno indicando que la operación fue exitosa
                                                        onComplete(true)
                                                    }
                                                }

                                                override fun onCancelled(databaseError: DatabaseError) {
                                                    // Manejar errores de lectura de datos
                                                    onComplete(false)
                                                }
                                            })
                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {
                                        // Manejar errores de lectura de datos
                                        onComplete(false)
                                    }
                                })
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Manejar errores de lectura de datos
                            onComplete(false)
                        }
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar errores de lectura de datos
                onComplete(false)
            }
        })
    }


    private fun performSearch(text: String) {
        val currentUser = prefs.getUsername()

        // Obtener las solicitudes enviadas por el usuario actual
        val solicitudesEnviadasRef = FirebaseDatabase.getInstance().reference
            .child("Solicitudes")
            .child(currentUser)

        solicitudesEnviadasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(solicitudesSnapshot: DataSnapshot) {
                val solicitudesEnviadas = solicitudesSnapshot.children.mapNotNull { it.key }

                // Realizar la búsqueda en Firebase y filtrar los resultados
                databaseReference?.orderByChild("username")
                    ?.startAt(text)
                    ?.endAt("$text\uf8ff")
                    ?.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val filteredList = snapshot.children.filter { itemSnapshot ->
                                val dataClass = itemSnapshot.getValue(DataClass::class.java)
                                dataClass != null &&
                                        dataClass.username?.lowercase()
                                            ?.contains(text.lowercase()) == true &&
                                        dataClass.username != currentUser
                            }.mapNotNull { it.getValue(DataClass::class.java) }

                            dataList.clear()
                            dataList.addAll(filteredList)
                            adapter.notifyDataSetChanged()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Manejar errores de Firebase
                        }
                    })
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores de Firebase
            }
        })
    }
}
