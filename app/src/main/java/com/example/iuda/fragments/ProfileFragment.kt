package com.example.iuda.fragments

import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.iuda.R
import com.example.iuda.iuda.Companion.prefs
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage


class ProfileFragment : Fragment() {

    private lateinit var imageProfile: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var storageRef: StorageReference
    private lateinit var currentUser: FirebaseUser
    private lateinit var profileUsername: EditText
    private lateinit var profileName: EditText
    private lateinit var profileEmail: EditText
    private lateinit var profileNumberPhone: EditText
    private lateinit var profileImage: ImageView
    private lateinit var editButton: Button
    private lateinit var databaseReference: DatabaseReference


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Encuentra los elementos de tu diseño por sus ID
        profileUsername = view.findViewById(R.id.profileUsername)
        profileName = view.findViewById(R.id.profileName)
        profileEmail = view.findViewById(R.id.profileEmail)
        profileNumberPhone = view.findViewById(R.id.profileNumberPhone)
        profileImage = view.findViewById(R.id.profileImage)
        editButton = view.findViewById(R.id.editButton)
        storageRef = Firebase.storage.reference

        //Recupera el uid del usuario
        val uidProfile = prefs.getId()

        // Deshabilita los EditText al inicio
        profileName.isEnabled = false
        profileUsername.isEnabled = false
        getInformationUser(uidProfile, profileImage)
        setEditTextsEnabled(false)

        // Agrega un listener al botón para habilitar los EditText al hacer clic
        editButton.setOnClickListener {
            setEditTextsEnabled(true)
            editButton.text="Save"
        }
    }


    private fun setEditTextsEnabled(isEnabled: Boolean) {
        profileEmail.isEnabled = isEnabled
        profileNumberPhone.isEnabled = isEnabled
    }

    fun getInformationUser(uid: String, imageView: ImageView) {
        // Obtención de datos del usuario
        if (uid != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("Users")
            databaseReference.child(uid).get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val usernameValue = dataSnapshot.child("username").value
                    val nameValue = dataSnapshot.child("fullName").value
                    val emailValue = dataSnapshot.child("email").value
                    val phoneValue = dataSnapshot.child("phoneNumber").value
                    //Se manda a llamar la función que descarga la foto
                    downloadImage(uid, imageView)
                    if (usernameValue != null && nameValue != null && emailValue != null && phoneValue != null) {
                        val username = usernameValue.toString()
                        val name = nameValue.toString()
                        val email = emailValue.toString()
                        val phone = phoneValue.toString()
                        prefs.saveUserName(username)
                        // Convertir el String a Editable antes de asignarlo al EditText
                        val editableUsername: Editable =
                            Editable.Factory.getInstance().newEditable(username)
                        val editableName: Editable =
                            Editable.Factory.getInstance().newEditable(name)
                        val editableEmail: Editable =
                            Editable.Factory.getInstance().newEditable(email)
                        val editablePhone: Editable =
                            Editable.Factory.getInstance().newEditable(phone)
                        profileUsername.text = editableUsername
                        profileName.text = editableName
                        profileEmail.text = editableEmail
                        profileNumberPhone.text = editablePhone

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "El nombre de usuario está vacío",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Manejar la situación en la que el campo "username" está vacío en la base de datos
                    }
                } else {
                    Toast.makeText(requireContext(), "UID no encontrado", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Log.e("TAG", "Error al obtener los datos: $exception")
                Toast.makeText(
                    requireContext(),
                    "Error al obtener los datos del usuario",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Log.d("TAG", "El usuario no está autenticado")
            Toast.makeText(requireContext(), "El usuario no está autenticado", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun downloadImage(uid: String, imageView: ImageView) {
        val imageRef = storageRef.child("profile/images/${uid}.jpg")

        imageRef.downloadUrl.addOnCompleteListener { downloadTask ->
            if (downloadTask.isSuccessful) {
                val downloadURL = downloadTask.result.toString()
                Glide.with(this@ProfileFragment) // Reemplaza 'TuActividad' con el nombre de tu actividad
                    .load(downloadURL)
                    .circleCrop()
                    .into(imageView)
            } else {
                // Manejar el caso de falla al obtener la URL de descarga
                Toast.makeText(
                    requireContext(), // Reemplaza 'TuActividad' con el nombre de tu actividad
                    "Error al descargar la imagen",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.addOnFailureListener { exception ->
            val errorMessage = exception.message
            Toast.makeText(
                requireContext(), // Reemplaza 'TuActividad' con el nombre de tu actividad
                "Error: ${errorMessage.toString()}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

