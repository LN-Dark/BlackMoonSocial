package com.luanegra.blackmoonsocial.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ddd.androidutils.DoubleClick
import com.ddd.androidutils.DoubleClickListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.luanegra.blackmoonsocial.ComentsActivity
import com.luanegra.blackmoonsocial.R
import com.luanegra.blackmoonsocial.models.*
import de.hdodenhof.circleimageview.CircleImageView

class ComentsAdapter(mContext: Context, mComentsList: List<Coments>) : RecyclerView.Adapter<ComentsAdapter.ViewHolder?>() {
    private val mContext = mContext
    private val mComentsList: List<Coments> = mComentsList
    var firebaseUser: FirebaseUser?= null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profile_image_coment: CircleImageView
        var coment: TextView
        var coment_date: TextView
        var username_coment: TextView
        var likesComent: TextView
        var image_view: ImageView

        init {
            profile_image_coment = itemView.findViewById(R.id.profile_image_coment)
            coment = itemView.findViewById(R.id.coment)
            coment_date = itemView.findViewById(R.id.coment_date)
            username_coment = itemView.findViewById(R.id.username_coment)
            likesComent = itemView.findViewById(R.id.likesComent)
            image_view = itemView.findViewById(R.id.image_view)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View?
        view = LayoutInflater.from(mContext).inflate(
            R.layout.coment_layout,
            parent,
            false
        )

        return ViewHolder(view!!)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val coment: Coments = mComentsList[position]
        holder.coment.text = coment.getcoment()
        holder.coment_date.text = coment.getdate()
        holder.likesComent.text = coment.getlikes().toString()
        FirebaseDatabase.getInstance().reference.child("users").child(coment.getuserComentID()).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: Users? = snapshot.getValue(Users::class.java)
                holder.profile_image_coment.load(user!!.getprofile())
                holder.username_coment.text = user!!.getusername()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        var userliked = false
        FirebaseDatabase.getInstance().reference.child("ComentsLikes").child(coment.getpostID()).child(coment.getuID()).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot in snapshot.children){
                    val comentlike: ComentsLikes? = snapshot.getValue(ComentsLikes::class.java)
                    if (comentlike!!.getuserID() == firebaseUser!!.uid){
                        userliked = true
                        holder.image_view.setImageResource(R.drawable.ic_heart_outline)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        holder.image_view.setOnClickListener {
            if (!userliked){
                userliked = true
                holder.image_view.setImageResource(R.drawable.ic_heart_outline)
                val newcomentLikeID = FirebaseDatabase.getInstance().reference.child("ComentsLikes").child(coment.getpostID()).child(coment.getuID()).push().key
                val map = HashMap<String, Any>()
                map["uid"] = newcomentLikeID.toString()
                map["userID"] = firebaseUser!!.uid
                map["postID"] = coment.getpostID()
                map["ComentID"] = coment.getuID()
                FirebaseDatabase.getInstance().reference.child("ComentsLikes").child(coment.getpostID()).child(coment.getuID()).setValue(map)
            }
        }
        holder.coment.setOnLongClickListener {

                if (coment.getuserComentID() == firebaseUser!!.uid){
                    val options = arrayOf<CharSequence>(
                        "Delete Coment",
                        "Cancel"
                    )
                    val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
                    builder.setTitle("Choose an option")
                    builder.setItems(options) { dialog, which ->
                        if (which == 0) {
                            deleteSentMessage(position)
                        }else if (which == 1) {
                            dialog.dismiss()
                        }
                    }
                    builder.show()
                }
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return mComentsList.size
    }

    private fun deleteSentMessage(position: Int){
        FirebaseDatabase.getInstance().reference.child("Coments").child(mComentsList[position].getpostID()).child(mComentsList[position].getuID()).removeValue()
        notifyDataSetChanged()
    }

}