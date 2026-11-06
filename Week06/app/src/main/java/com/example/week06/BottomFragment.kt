package com.example.week06

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import java.util.zip.Inflater

class BottomFragment : Fragment {
    constructor()
    private lateinit var txtFullname : TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        saveInstanceState: Bundle?
    ): View? {
        val view : View = inflater.inflate(R.layout.botton_fragment_layout, container, false)
        txtFullname = view.findViewById(R.id.fullname)
        return view
    }

    fun showText(firstName: String, lastName: String){
        txtFullname.setText(firstName + "" + lastName)
    }
}