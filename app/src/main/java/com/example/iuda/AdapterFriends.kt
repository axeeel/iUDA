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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class AdapterFriends(private val context: Context, private var amigosList: List<AmigosDataClass>):
    RecyclerView.Adapter<AdapterFriends.ViewHolder>() {
    var nameCon: String? = null
    var phoneCon: String? = null
     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
         val view: View =
             LayoutInflater.from(parent.context).inflate(R.layout.recycler_friends, parent, false)
         return ViewHolder(view)
     }

     override fun onBindViewHolder(holder: ViewHolder, position: Int) {
         Glide.with(context).load(amigosList[position].imgUrl).into(holder.recImg)
         holder.recUsername.text = amigosList[position].username
         holder.recUid.text = amigosList[position].id


     }

     fun setDataList(newDataList: List<AmigosDataClass>) {
         this.amigosList = newDataList
     }




     override fun getItemCount(): Int {
         return amigosList.size
     }

     interface OnItemClickListener {
         fun onItemClick(position: Int, action: String)
     }

     private var onItemClickListener:OnItemClickListener? = null

     fun setOnItemClickListenner(listener: OnItemClickListener){
         this.onItemClickListener = listener
     }

     inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         var recUid: TextView = itemView.findViewById(R.id.recUid)
         var recImg: ImageView = itemView.findViewById(R.id.recImg)
         var recUsername: TextView = itemView.findViewById(R.id.recUsername)
         var btnDelete: ImageButton = itemView.findViewById(R.id.btnAccept)

         init {
             btnDelete.setOnClickListener {
                 val position = adapterPosition
                 if(position != RecyclerView.NO_POSITION){
                     onItemClickListener?.onItemClick(position, "delete")
                 }
             }
             itemView.setOnClickListener{
                 val position = adapterPosition
                 if(position != RecyclerView.NO_POSITION){
                     onItemClickListener?.onItemClick(position, "details")
                 }
             }

         }
     }
 }