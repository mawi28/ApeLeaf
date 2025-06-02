package com.example.apel

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Tentangapp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge for immersive experience
        enableEdgeToEdge()

        // Set the content view from XML layout
        setContentView(R.layout.activity_tentangapp)

        // Set up the toolbar as the action bar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Tentang Aplikasi"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Enable the up button in the toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Handle edge-to-edge insets, so that content is not hidden behind system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Handle the back button in the toolbar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()  // Go back to the previous screen
        return true
    }
}
