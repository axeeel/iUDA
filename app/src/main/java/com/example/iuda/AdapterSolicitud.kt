package com.example.iuda

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import com.example.iuda.fragments.AddFriendFragment

class AdapterSolicitud(private val context: Context, private var usuariosLista:List<DataClass>):
    RecyclerView.Adapter<AdapterSolicitud.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.recycler_request,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).load(usuariosLista[position].imgUrl).into(holder.recImg)
        holder.recUsername.text = usuariosLista[position].username
        holder.recName.text = usuariosLista[position].fullName
        holder.recGender.text = usuariosLista[position].gender
        holder.recUid.text = usuariosLista[position].id
    }



    override fun getItemCount(): Int {
        return usuariosLista.size
    }

    fun setDataList(newDataList: List<DataClass>) {
        this.usuariosLista = newDataList
    }


    interface OnItemClickListener {
        fun onItemClick(position: Int, action: String)
    }

    private var onItemClickListener:OnItemClickListener? = null


    fun setOnItemClickListener(listener: OnItemClickListener){
        this.onItemClickListener = listener
    }




    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var recUid: TextView = itemView.findViewById(R.id.recUid)
        var recImg: ImageView = itemView.findViewById(R.id.recImg)
        var recUsername: TextView = itemView.findViewById(R.id.recUsername)
        var recName: TextView = itemView.findViewById(R.id.recName)
        var recGender: TextView = itemView.findViewById(R.id.recGender)
        var btnAccept: ImageButton = itemView.findViewById(R.id.btnAccept)
        var btnDecline: ImageButton = itemView.findViewById(R.id.btnDecline)

        init{
            btnAccept.setOnClickListener {
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION){
                    onItemClickListener?.onItemClick(position,"accept")
                }
            }

            btnDecline.setOnClickListener {
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION){
                    onItemClickListener?.onItemClick(position,"decline")
                }
            }
        }
    }
}