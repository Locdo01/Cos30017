package com.example.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.login.AdminActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var btn1: Button
    lateinit var btn2: Button
    lateinit var txt1: EditText
    lateinit var txt2: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btn1 = findViewById(R.id.btn1)
        btn2 = findViewById(R.id.btn2)
        txt1 = findViewById(R.id.txtUsername)
        txt2 = findViewById(R.id.txtPassword)

        btn1.setOnClickListener ( this )
        btn2.setOnClickListener ( this )
    }

    override fun onClick(v: View?) {
        var a = txt1.text.toString()
        var b = txt2.text.toString()

        when(v?.id){
            R.id.btn1->{
                if(a == "Loc" && b=="123"){
                    Toast.makeText(this, "Login successful", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, AdminActivity::class.java)
                    startActivity(intent)
                }else {
                    Toast.makeText(this, "Login Failed", Toast.LENGTH_LONG).show()
                }
            }
            R.id.btn2->{
                txt1.setText("")
                txt2.setText("")
            }
        }
    }
}