
package com.example.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var btn1: Button
    private lateinit var btn2: Button
    private lateinit var txt1: EditText
    private lateinit var txt2: EditText

    private lateinit var viewModel: SampleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)
            .get(SampleViewModel::class.java)

        observeViewModel()

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

        Log.d("Check", "Run onCreate")

        btn1 = findViewById(R.id.btn1)
        btn2 = findViewById(R.id.btn2)
        txt1 = findViewById(R.id.txtUsername)
        txt2 = findViewById(R.id.txtPassword)

        btn1.setOnClickListener(this)
        btn2.setOnClickListener(this)
    }

    private fun observeViewModel() {
        viewModel.badgeCount.observe(this) { value ->
            showToast(value)
        }
    }

    private fun showToast(value: Int) {
        Toast.makeText(
            this,
            value.toString(),
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onClick(v: View?) {
        val a = txt1.text.toString()
        val b = txt2.text.toString()

        when (v?.id) {
            R.id.btn1 -> {
                if (a == "Loc" && b == "123") {
                    viewModel.incrementBadgeCount()
                    Toast.makeText(
                        this,
                        "Login successful",
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent(this, AdminActivity::class.java)
                    intent.putExtra("a", a)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this,
                        "Login Failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            R.id.btn2 -> {
                txt1.text.clear()
                txt2.text.clear()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("Check", "Run onStart")
    }
}
