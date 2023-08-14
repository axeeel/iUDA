package com.example.iuda_template_n1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.iuda_template_n1.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() ,NavigationView.OnNavigationItemSelectedListener{
    private lateinit var fragmentManager: FragmentManager
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val toggle = ActionBarDrawerToggle(this,binding.drawerLayout,binding.toolbar, R.string.nav_open, R.string.nav_close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navigationDrawer.setNavigationItemSelectedListener(this)

        binding.bottomNavigation.background = null
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.bottom_profile -> openFragment(ProfileFragment())
                R.id.bottom_add_friend -> openFragment(AddFriendFragment())
                R.id.bottom_location -> openFragment(MapFragment())
            }
            true
        }
        fragmentManager = supportFragmentManager
        openFragment(MapFragment())

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.friends_circle -> openFragment(CircleFriendsFragment())
            R.id.medical_records -> openFragment(HealthFragment())
            R.id.contactos_emergencia -> openFragment(ContactEmergencyFragment())
            R.id.notifications -> openFragment(NotificationsFragment())
            R.id.nav_logout -> openFragment(LogOutFragment())
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if(binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.getOnBackPressedDispatcher().onBackPressed()
        }
    }

    private fun openFragment(fragment: Fragment){
        val fragmentTransition: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransition.replace(R.id.fragment_container, fragment)
        fragmentTransition.commit()
    }
}