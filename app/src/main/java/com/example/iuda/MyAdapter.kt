package com.example.iuda

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MyAdapter(private val context: Context, private var dataList:List<DataClass>):RecyclerView.Adapter<MyAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item,parent,false)
        return MyViewHolder(view)

    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }


    override fun getItemCount(): Int {
        return dataList.size
    }
    private var buttonPressedPositions = HashSet<Int>()

    fun setButtonPressed(position: Int) {
        buttonPressedPositions.add(position)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(dataList[position].imgUrl).into(holder.recImg)
        holder.recUsername.text = dataList[position].username
        holder.recName.text = dataList[position].fullName
        holder.recGender.text = dataList[position].gender
        holder.recUid.text = dataList[position].id

        if (buttonPressedPositions.contains(position)) {
            // Cambiar el icono y el fondo del botón permanentemente
            holder.btnAddFriend.setImageResource(R.drawable.baseline_check_circle_outline_24)
            holder.btnAddFriend.setBackgroundResource(R.drawable.backgroung_button_pressed)
        }

    }

    fun searchDataList(searchList: List<DataClass>){
        dataList = searchList
        notifyDataSetChanged()
    }



    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var recUid: TextView = itemView.findViewById(R.id.recUid)
        var recImg: ImageView = itemView.findViewById(R.id.recImg)
        var recUsername: TextView = itemView.findViewById(R.id.recUsername)
        var recName: TextView = itemView.findViewById(R.id.recName)
        var recGender: TextView = itemView.findViewById(R.id.recGender)
        var recCard: CardView = itemView.findViewById(R.id.recCard)
        var btnAddFriend: ImageButton = itemView.findViewById(R.id.btnAddFriend)

        init {
            // Asignar el clic del botón en el ViewHolder
            btnAddFriend.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.onItemClick(position)
                }
            }
        }

    }
}






