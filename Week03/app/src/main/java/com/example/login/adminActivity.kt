
package com.example.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_admin)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v, insets ->
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

        val username = intent.getStringExtra("a")
        val txtWelcome = findViewById<TextView>(R.id.txtWelcome)
        txtWelcome.text = "Welcome, $username!"
        Toast.makeText(this,username, Toast.LENGTH_LONG).show()

        val btnExit = findViewById<Button>(R.id.btnExit)
        btnExit.setOnClickListener {
            val result = Intent()
            result.putExtra("Test1", "Hello")
            result.putExtra("Test2", "Value2")
            setResult(RESULT_OK, result)
            finish()
        }


    }
}
