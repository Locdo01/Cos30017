package com.example.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewAdapter(
    private val mList: List<DataClass>
) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolderClass>() {

    // 1. Create ViewHolder
    class ViewHolderClass(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val image: ImageView = itemView.findViewById(R.id.imgPro)

        val textView: TextView = itemView.findViewById(R.id.txtName)
    }

    // 2. Create item layout
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolderClass {

        val itemView = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.template_re_layout,
                parent,
                false
            )

        return ViewHolderClass(itemView)
    }

    // 3. Bind data
    override fun onBindViewHolder(
        holder: ViewHolderClass,
        position: Int
    ) {
        val element = mList[position]

        holder.textView.text = element.studentName
        holder.image.setImageResource(element.imgProfile)

        holder.itemView.setOnClickListener {

            Toast.makeText(
                holder.itemView.context,
                element.studentName,
                Toast.LENGTH_SHORT
            ).show()

        }
    }

    // 4. Count items
    override fun getItemCount(): Int {
        return mList.size
    }
}