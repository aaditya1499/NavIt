package com.example.indoornavigation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BuildingsAdapter(val buildings:ArrayList<String>):RecyclerView.Adapter<BuildingsAdapter.ViewHolder>() {

    var onItemClick: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view:View = LayoutInflater.from(parent.context).inflate(R.layout.building_row,parent,false)
//        val view1:View = LayoutInflater.from(parent.context).inflate(R.layout.building_ticket,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount() = buildings.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = buildings[position]
//        holder.itemView.setOnClickListener {
//            for (i in 0 until buildings.size) {
//
//            }
//        }
    }

    inner class ViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView) {
        val name:TextView = itemView.findViewById(R.id.name)
        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(buildings[adapterPosition])
            }
        }
    }

    interface onItemClickListner {
        fun onItemClick(position: Int) {

        }
    }

//    override fun onClick(v: View?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }


}