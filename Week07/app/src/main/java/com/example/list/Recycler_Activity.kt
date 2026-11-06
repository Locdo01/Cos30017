package com.example.list

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class Recycler_Activity : AppCompatActivity() {

    lateinit var lsStudents: RecyclerView

    var strList: Array<String> = arrayOf(
        "Nguyen Van Tien",
        "Bui Cong Minh",
        "Nguyen Pha Le",
        "Phan Tuan Sy",
        "Dang Tieu Binh"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load layout
        setContentView(R.layout.recyclerview_layout)

        // Connect RecyclerView
        lsStudents = findViewById(R.id.lsStudents)

        // Set LayoutManager
        lsStudents.layoutManager = LinearLayoutManager(this)

        lsStudents.setHasFixedSize(true)

        // Create data list
        val data = ArrayList<DataClass>()

        for (i in 0 until strList.size) {
            val imageId = when (i) {
                0 -> R.drawable.student1
                1 -> R.drawable.student2
                2 -> R.drawable.student3
                3 -> R.drawable.student4
                else -> R.drawable.student5
            }

            data.add(
                DataClass(imageId, strList[i])
            )
        }

        // Create Adapter
        val adapter = RecyclerViewAdapter(data)

        // Display data
        lsStudents.adapter = adapter
    }
}