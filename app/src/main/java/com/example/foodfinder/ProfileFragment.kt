package com.example.foodfinder

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.foodfinder.R

class ProfileFragment : Fragment() {

    private lateinit var tv_username: TextView
    private lateinit var tv_age: TextView
    private lateinit var tv_car_name: TextView
    private lateinit var btnBack: Button


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //  Das Layout für dieses Fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // TextViews mit der aufgeblähten Ansicht initialisieren
        tv_username = view.findViewById(R.id.tv_username)
        tv_age = view.findViewById(R.id.tv_age)
        tv_car_name = view.findViewById(R.id.tv_car_name)
        btnBack = view.findViewById(R.id.btn_back)


        // Den Benutzernamen aus den Argumenten abrufen
        val name = arguments?.getString("USER_NAME")
        val age = arguments?.getString("USER_AGE")
        val emailName = arguments?.getString("USER_EMAIL_NAME")

        //Die TextViews mit den abgerufenen Werten befüllen
        tv_username.text = "Benutzername: $name"
        tv_age.text = "Alter: $age"
        tv_car_name.text = "Email: $emailName"

        // OnClickListener setzen, um zur UserActivity zu wechseln

        btnBack.setOnClickListener {
            val intent = Intent(activity, UserFormActivity::class.java)
            startActivity(intent)
        }

        return view

    }
}