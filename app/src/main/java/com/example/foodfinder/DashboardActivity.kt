package com.example.foodfinder

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.foodfinder.R
import com.example.foodfinder.databinding.ActivityDashboardBinding


class DashboardActivity : AppCompatActivity(), FilterFragment.OnFilterChangedListener {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // View Binding initialisieren
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Den Benutzernamen aus dem Intent abrufen
        val name = intent.getStringExtra("USER_NAME")
        val age = intent.getStringExtra("USER_AGE")
        val emailName = intent.getStringExtra("USER_EMAIL_NAME")

        // Ersetze das Fragment im Container mit ProfileFragment
        replaceFragment(createProfileFragment(name, age, emailName))

        // Setze den Listener für die BottomNavigationView
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_profile -> {
                    replaceFragment(createProfileFragment(name, age, emailName))
                    true
                }

                R.id.navigation_map -> {
                    replaceFragment(MapFragment())
                    true
                }

                R.id.navigation_filter -> {
                    Log.d("DashboardActivity", "FilterFragment ausgewählt")
                    replaceFragment(FilterFragment())
                    true
                }

                else -> false
            }
        }
    }

    override fun onFilterChanged(isVegetarian: Boolean, isVegan: Boolean) {
        // Hier wird das MapFragment gefunden und die Methode zur Aktualisierung aufgerufen
        val mapFragment = supportFragmentManager.findFragmentById(R.id.frame_layout) as? MapFragment
        mapFragment?.updateMapFilters(isVegetarian, isVegan)
    }

    private fun createProfileFragment(name: String?, age: String?, emailName: String?): Fragment {
        return ProfileFragment().apply {
            arguments = Bundle().apply {
                putString("USER_NAME", name)
                putString("USER_AGE", age)
                putString("USER_EMAIL_NAME", emailName)
            }
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }
}