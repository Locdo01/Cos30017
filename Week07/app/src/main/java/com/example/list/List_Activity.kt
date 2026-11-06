package com.example.list

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class List_Activity : AppCompatActivity() {

    // 1. Declare ListView
    lateinit var lsStudents: ListView

    // 2. Student data
    var strList: Array<String> = arrayOf(
        "Nguyen Van Tien",
        "Bui Cong Minh",
        "Nguyen Pha Le",
        "Phan Tuan Sy",
        "Dang Tieu Binh"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 3. Load XML layout
        setContentView(R.layout.list_layout)

        // 4. Connect ListView
        lsStudents = findViewById(R.id.lsStudents)

        //strList.set(1, "Testing")
        // 5. Create Adapter
        var arr = ArrayAdapter(
            this,
            R.layout.template_layout,
            strList
        )

        // 6. Display data
        lsStudents.adapter = arr

        // 7. Handle item click
        lsStudents.setOnItemClickListener { parent, view, position, id ->
            val element = arr.getItem(position) as String
            Toast.makeText(
                this,
                element,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}