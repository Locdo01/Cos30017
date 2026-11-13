
package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val viewModel: CounterViewModel by viewModels()

    private lateinit var txtCounter: TextView
    private lateinit var txtStatus: TextView
    private lateinit var txtMessage: TextView
    private lateinit var btnCounter: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Views
        txtCounter = findViewById(R.id.txtCounter)
        txtStatus = findViewById(R.id.txtStatus)
        txtMessage = findViewById(R.id.txtMessage)
        btnCounter = findViewById(R.id.btnCounter)

        // Observe Counter
        viewModel.taps.observe(this) { count ->
            txtCounter.text = count.toString()
        }

        // Observe Button status
        viewModel.buttonEnabled.observe(this) { enabled ->
            btnCounter.isEnabled = enabled
        }

        // Observe processing status
        viewModel.status.observe(this) { message ->
            txtStatus.text = message

            if (message == "Processing...") {
                txtMessage.text = "Please wait 3 seconds..."
            } else {
                txtMessage.text = "Click the button to continue"
            }
        }

        // Button click listener
        btnCounter.setOnClickListener {
            viewModel.updateTaps()
        }
    }
}
