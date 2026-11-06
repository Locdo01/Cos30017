package com.example.week06

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment

class TopFragment : Fragment() {

    private lateinit var txtFirstName: EditText
    private lateinit var txtLastName: EditText
    private lateinit var btnOk: Button
    private lateinit var mainActivity: Main_Fragment_Activity

    // 1. Fragment vừa gắn vào Activity: lưu lại Activity cha
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Main_Fragment_Activity) {
            mainActivity = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.top_fragment_layout, container, false)

        txtFirstName = view.findViewById(R.id.firstname)
        txtLastName = view.findViewById(R.id.lastname)
        btnOk = view.findViewById(R.id.button_ok)

        // 3. Bấm OK: lấy dữ liệu rồi nhờ Activity chuyển xuống BottomFragment
        btnOk.setOnClickListener {
            val firstName = txtFirstName.text.toString()
            val lastName = txtLastName.text.toString()
            mainActivity.showText(firstName, lastName)
        }

        return view
    }
}