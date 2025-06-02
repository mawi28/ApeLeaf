package com.example.apel

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView


class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info) // Set layout for InfoActivity

        // Menemukan CardView berdasarkan ID
        val cardAppleScab: CardView = findViewById(R.id.cardAppleScab)
        val cardBlackRot: CardView = findViewById(R.id.cardBlackRot)
        val cardCedarAppleRust: CardView = findViewById(R.id.cardAppleCedarRust)

        // Set OnClickListener untuk setiap CardView
        cardAppleScab.setOnClickListener {
            // Intent untuk pindah ke Detail Activity dengan mengirimkan nama penyakit
            val intent = Intent(this@InfoActivity, DiseaseDetailActivity::class.java)
            intent.putExtra("disease_name", "Apple Scab")
            startActivity(intent)
        }

        cardBlackRot.setOnClickListener {
            // Intent untuk pindah ke Detail Activity dengan mengirimkan nama penyakit
            val intent = Intent(this@InfoActivity, DiseaseDetailActivity::class.java)
            intent.putExtra("disease_name", "Black Rot")
            startActivity(intent)
        }

        cardCedarAppleRust.setOnClickListener {
            // Intent untuk pindah ke Detail Activity dengan mengirimkan nama penyakit
            val intent = Intent(this@InfoActivity, DiseaseDetailActivity::class.java)
            intent.putExtra("disease_name", "Cedar Apple Rust")
            startActivity(intent)
        }
        val tentangAplikasiCard: CardView = findViewById(R.id.tentangAplikasiCard)
        tentangAplikasiCard.setOnClickListener {
            val intent = Intent(this@InfoActivity, Tentangapp::class.java)
            startActivity(intent)
        }


        val btnStart = findViewById<Button>(R.id.btnStartDetection)
        btnStart.setOnClickListener {
            // Navigasi ke halaman deteksi (DetectionActivity)
            startActivity(Intent(this, DetectionActivity::class.java))
        }
    }
}
