package com.example.iuda

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.iuda.databinding.ActivityLoginBinding
import com.example.iuda.fragments.HealthFragment
import com.example.iuda.iuda.Companion.prefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LogInActivity : AppCompatActivity() {
    // Declaración de variables
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {

        // Inicialización de la instancia de FirebaseDatabase y obtención de una referencia a la ubicación "Users"
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("Users")

        // Llamada al método onCreate de la superclase
        super.onCreate(savedInstanceState)

        // Inflado del diseño de la actividad y establecimiento como contenido de la actividad
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración del listener para redirigir a la actividad de registro
        binding.signupRedirectText.setOnClickListener {
            val intent = Intent(this@LogInActivity, UploadActivity::class.java)
            startActivity(intent)
            finish()
        }


        if(prefs.getEmail().isNotEmpty()){
            startActivity(Intent(this,MainActivity::class.java))
        }else{
            //AuthLogIn(email, password)
            binding.loginButton.setOnClickListener{
                val email = binding.loginEmail.text.toString()
                val password = binding.loginPassword.text.toString()
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                accessToDetail(email)
                                showHome(it.result?.user?.email?: "", ProviderType.BASIC)
                            }else{
                                showAlert()
                            }
                        }
                } else {
                    // Mostrar mensaje de advertencia si los campos están vacíos
                    Toast.makeText(
                        this@LogInActivity,
                        "Todos los campos son requeridos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun accessToDetail(email: String){
        //Guardar correo
        prefs.saveEmail(email)
    }

    //funcion de alerta
    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando el usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //funcion de navegacion
    private fun showHome(email: String, provider: ProviderType){
        val homeIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
    }
}
