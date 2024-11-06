package com.vuca.xbcad7319_vucadigital.Activites

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.vuca.xbcad7319_vucadigital.R
import com.vuca.xbcad7319_vucadigital.db.SupabaseHelper
import com.google.firebase.FirebaseApp
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {
    private lateinit var supabaseHelper: SupabaseHelper
    private lateinit var pb: ProgressBar
    private var counter : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)
        supabaseHelper = SupabaseHelper()

        progressBar()
    }
    private fun progressBar(){
        pb = findViewById(R.id.progressBar)

        val t = Timer()
        val tt = object : TimerTask() {
            override fun run() {
                counter++

                pb.progress = counter

                if(counter == 100){
                    t.cancel()

                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                }
            }
        }

        t.schedule(tt, 0, 10)

    }
}