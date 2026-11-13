package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private val imageViewModel: ImageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        val imageView = findViewById<ImageView>(R.id.imageView)
        val btnNext = findViewById<Button>(R.id.btnNext)

        imageViewModel.image.observe(this) { imageRes ->
            imageView.setImageResource(imageRes)
        }

        imageViewModel.imageName.observe(this) { name ->
            Log.d("ImageLiveData", "Current image: $name")
        }

        btnNext.setOnClickListener {
            imageViewModel.nextImage()
        }
    }
}
