package com.example.studylog

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studylog.model.ZzimListItem
import com.example.studylog.navigation.CommentActivity
import com.google.firebase.auth.FirebaseAuth

class UserZzimListAdapter(var context: Context, private val itemList: ArrayList<ZzimListItem>) : RecyclerView.Adapter<UserZzimListAdapter.ViewHolder>() {

    lateinit var auth: FirebaseAuth

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView!!){
        val PostImage :ImageView = itemView.findViewById(R.id.PostImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.activity_user_zzim_list_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        Glide.with(context).load(itemList.get(position).PostImageUrl).into(holder.PostImage)

        holder.PostImage.setOnClickListener {
            val intent = Intent(context, UserPostActivity::class.java)
            intent.putExtra("fromUser", false)
            context.startActivity(intent)
        }
    }

}