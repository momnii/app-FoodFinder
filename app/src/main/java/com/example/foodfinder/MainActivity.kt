package com.example.foodfinder

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.foodfinder.R


class MainActivity : AppCompatActivity() {
    //Variablen
    private lateinit var topAnim: Animation
    private lateinit var bottomAnim: Animation
    private lateinit var image: ImageView
    private lateinit var title: TextView
    private lateinit var slogan: TextView

    private val splashScreen: Long = 5000;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Hier wird das Layout gesetzt

        //Animationen
        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this,R.anim.bottom_animation);

        //Hooks
        image = findViewById(R.id.imageViewFood);
        title  = findViewById(R.id.textViewTitle);
        slogan = findViewById(R.id.sloganTextView);

        image.setAnimation(topAnim);
        title.setAnimation(bottomAnim);
        slogan.setAnimation(bottomAnim);

        // Wechsel zum nächsten Screen nach der Splashscreen-Dauer
        Handler().postDelayed({
            // Start der nächsten Activity
            val intent = Intent(this, UserFormActivity::class.java)
            startActivity(intent)
            finish() // Schließe die aktuelle Activity, um sie nicht im Stack zu behalten
        }, splashScreen)

    }

}


