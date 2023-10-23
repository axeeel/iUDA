package com.example.iuda

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.iuda.databinding.ActivityMainBinding
import com.example.iuda.fragments.AddFriendFragment
import com.example.iuda.fragments.CircleFriendsFragment
import com.example.iuda.fragments.ContactEmergencyFragment
import com.example.iuda.fragments.HealthFragment
import com.example.iuda.fragments.LogOutFragment
import com.example.iuda.fragments.MapFragment
import com.example.iuda.fragments.NotificationsFragment
import com.example.iuda.fragments.ProfileFragment
import com.example.iuda.iuda.Companion.prefs
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

enum class ProviderType{
    BASIC
}
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var fragmentManager: FragmentManager
    private lateinit var binding: ActivityMainBinding
    private lateinit var email: String
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialización de variables y obtención de datos de usuario
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        email = prefs.getEmail()
        getInformationUser()

        // Inflar la interfaz de usuario y configurar la vista de contenido.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la barra de herramientas.
        setSupportActionBar(binding.toolbar)

        // Configurar el toggle para el cajón de navegación.
        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.nav_open, R.string.nav_close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Establecer un oyente para el cajón de navegación.
        binding.navigationDrawer.setNavigationItemSelectedListener(this)

        // Configurar el menú de navegación inferior.
        binding.bottomNavigation.background = null
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_profile -> openFragment(ProfileFragment())
                R.id.bottom_add_friend -> openFragment(AddFriendFragment())
                R.id.bottom_location -> openFragment(MapFragment())
            }

            // Cambiar el estado del ítem seleccionado
            item.isChecked = true

            true
        }

        // Inicializar el administrador de fragmentos y cargar el fragmento inicial.
        fragmentManager = supportFragmentManager

        // Obtener el ID del elemento seleccionado en el menú inferior
        val selectedItemId = R.id.bottom_location

        // Seleccionar el fragmento inicial basado en el ID del elemento
        val initialFragment: Fragment = when (selectedItemId) {
            R.id.bottom_profile -> ProfileFragment()
            R.id.bottom_add_friend -> AddFriendFragment()
            R.id.bottom_location -> MapFragment()
            else -> MapFragment() // Fragmento predeterminado en caso de ID no válido
        }

        openFragment(initialFragment)
    }

    fun getInformationUser() {
        // Obtención de datos del usuario
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("Users")
            databaseReference.child(uid).get().addOnSuccessListener {
                if(it.exists()){
                    val fullname = it.child("fullName").value
                    prefs.saveName(fullname.toString())
                } else {
                    Toast.makeText(this, "UID no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Log.d("TAG", "El usuario no está autenticado")
            Toast.makeText(this, "El usuario no está autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Manejar eventos de selección en el cajón de navegación.
        when (item.itemId) {
            R.id.friends_circle -> openFragment(CircleFriendsFragment())
            R.id.medical_records -> openFragment(HealthFragment())
            R.id.contactos_emergencia -> openFragment(ContactEmergencyFragment())
            R.id.notifications -> openFragment(NotificationsFragment())
            R.id.nav_logout -> {
                // Iniciar LoginActivity
                prefs.wipe()
                val intent = Intent(this, LogInActivity::class.java)
                startActivity(intent)
                finish() // Opcional: Finalizar la actividad actual si es necesario
            }
        }

        // Cerrar el cajón de navegación y retornar verdadero para indicar que se manejó el evento.
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        // Manejar el comportamiento personalizado del botón de retroceso.
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun openFragment(fragment: Fragment) {
        // Función para reemplazar el fragmento actual con uno nuevo en el contenedor.
        val fragmentTransition: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransition.replace(R.id.fragment_container, fragment)
        fragmentTransition.commit()
    }
}
