package com.luanegra.blackmoonsocial.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import com.luanegra.blackmoonsocial.R
import com.luanegra.blackmoonsocial.ViewProfileActivity
import com.luanegra.blackmoonsocial.models.*
import de.hdodenhof.circleimageview.CircleImageView

class MyPostsAdapter(mContext: Context, mPostsList: List<Posts>) : RecyclerView.Adapter<MyPostsAdapter.ViewHolder?>() {
    private val mContext = mContext
    private val mPostsList: List<Posts> = mPostsList
    var firebaseUser: FirebaseUser?= null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var img_myposts: ImageView

        init {
            img_myposts = itemView.findViewById(R.id.img_myposts)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View?
        view = LayoutInflater.from(mContext).inflate(
            R.layout.myposts_layout,
            parent,
            false
        )
        return ViewHolder(view!!)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val post: Posts = mPostsList[position]
        holder.img_myposts.load(post.getphotoURL())
        holder.img_myposts.setOnClickListener {
            val options = arrayOf<CharSequence>(
                "View full Post",
                "Delete Post",
                "Cancel"
            )
            val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
            builder.setTitle("Choose an option")
            builder.setItems(options) { dialog, which ->
                if (which == 0) {
                    val mDialogView = LayoutInflater.from(mContext).inflate(
                        R.layout.home_item_layout,
                        null
                    )
                    val mBuilder = AlertDialog.Builder(mContext)
                        .setView(mDialogView)

                    val  mAlertDialog = mBuilder.show()
                    FirebaseDatabase.getInstance().reference.child("users").child(post.getuserID()).addValueEventListener(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val postUser: Users? = snapshot.getValue(Users::class.java)
                            mDialogView.findViewById<ImageView>(R.id.img_card_home).load(post.getphotoURL())
                            mDialogView.findViewById<TextView>(R.id.description_post_home).text = post.getdescription()
                            mDialogView.findViewById<CircleImageView>(R.id.user_profileimage_card_home).load(postUser!!.getprofile())
                            mDialogView.findViewById<TextView>(R.id.txt_username_card_home).text = postUser.getusername()
                            mDialogView.findViewById<TextView>(R.id.txt_date_card_home).text = post.getdate()
                            if (post.getlikes() != null){
                                mDialogView.findViewById<TextView>(R.id.txt_likes_home_card).text = post.getlikes()!!.size.toString()
                            }else{
                                mDialogView.findViewById<TextView>(R.id.txt_likes_home_card).text = "0"
                            }
                            mDialogView.findViewById<ImageView>(R.id.img_coment_card_home).setOnClickListener {

                            }
                            mDialogView.findViewById<ImageView>(R.id.img_sendmessage_card_home).visibility = View.GONE
                            var userliked: Likes? = null
                            FirebaseDatabase.getInstance().reference.child("Likes").child(post.getUid()).addValueEventListener(object: ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (datasnapshot in snapshot.children){
                                        val like: Likes? = datasnapshot.getValue(Likes::class.java)
                                        if (like!!.getuserID() == firebaseUser!!.uid){
                                            userliked = like
                                            mDialogView.findViewById<ImageView>(R.id.img_likes_card_home).setImageResource(R.drawable.ic_heart_outline)
                                        }
                                    }
                                    val doubleClick = DoubleClick(object : DoubleClickListener {
                                        override fun onSingleClickEvent(view: View?) {
                                            // DO STUFF SINGLE CLICK
                                        }

                                        override fun onDoubleClickEvent(view: View?) {
                                            if (userliked != null){
                                                FirebaseDatabase.getInstance().reference.child("Likes").child(post.getUid()).child(userliked!!.getUid()).removeValue()
                                                mDialogView.findViewById<ImageView>(R.id.img_likes_card_home).setImageResource(R.drawable.ic_unheart_outline)
                                                userliked = null
                                                if (post.getlikes() != null){
                                                    mDialogView.findViewById<TextView>(R.id.txt_likes_home_card).text = post.getlikes()!!.size.toString()
                                                }else{
                                                    mDialogView.findViewById<TextView>(R.id.txt_likes_home_card).text = "0"
                                                }
                                            }else{
                                                val likeHashMap = HashMap<String, Any?>()
                                                val keyNewLike = FirebaseDatabase.getInstance().reference.child("Likes").child(post.getUid()).push().key
                                                likeHashMap["uid"] = keyNewLike
                                                likeHashMap["userID"] = firebaseUser!!.uid
                                                FirebaseDatabase.getInstance().reference.child("Likes").child(post.getUid()).child(keyNewLike.toString()).setValue(likeHashMap)
                                                userliked = Likes(keyNewLike!!, firebaseUser!!.uid)
                                                mDialogView.findViewById<ImageView>(R.id.img_likes_card_home).setImageResource(R.drawable.ic_heart_outline)
                                                if (post.getlikes() != null){
                                                    mDialogView.findViewById<TextView>(R.id.txt_likes_home_card).text = post.getlikes()!!.size.toString()
                                                }else{
                                                    mDialogView.findViewById<TextView>(R.id.txt_likes_home_card).text = "0"
                                                }
                                            }
                                        }
                                    })
                                    mDialogView.findViewById<ImageView>(R.id.img_card_home).setOnClickListener(doubleClick)

                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })
                            mDialogView.findViewById<ImageView>(R.id.img_likes_card_home).setOnClickListener {
                                if (userliked != null){
                                    FirebaseDatabase.getInstance().reference.child("Likes").child(post.getUid()).child(userliked!!.getUid()).removeValue()
                                    mDialogView.findViewById<ImageView>(R.id.img_likes_card_home).setImageResource(R.drawable.ic_unheart_outline)
                                    userliked = null
                                    if (post.getlikes() != null){
                                        mDialogView.findViewById<TextView>(R.id.txt_likes_home_card).text = post.getlikes()!!.size.toString()
                                    }else{
                                        mDialogView.findViewById<TextView>(R.id.txt_likes_home_card).text = "0"
                                    }
                                }else{
                                    val likeHashMap = HashMap<String, Any?>()
                                    val keyNewLike = FirebaseDatabase.getInstance().reference.child("Likes").child(post.getUid()).push().key
                                    likeHashMap["uid"] = keyNewLike
                                    likeHashMap["userID"] = firebaseUser!!.uid
                                    FirebaseDatabase.getInstance().reference.child("Likes").child(post.getUid()).child(keyNewLike.toString()).setValue(likeHashMap)
                                    userliked = Likes(keyNewLike!!, firebaseUser!!.uid)
                                    mDialogView.findViewById<ImageView>(R.id.img_likes_card_home).setImageResource(R.drawable.ic_heart_outline)
                                    if (post.getlikes() != null){
                                        mDialogView.findViewById<TextView>(R.id.txt_likes_home_card).text = post.getlikes()!!.size.toString()
                                    }else{
                                        mDialogView.findViewById<TextView>(R.id.txt_likes_home_card).text = "0"
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })

                } else if (which == 1) {
                    deleteSentMessage(position)
                } else if (which == 2) {
                    dialog.dismiss()
                }
            }
            builder.show()
        }
    }

    override fun getItemCount(): Int {
        return mPostsList.size
    }

    private fun deleteSentMessage(position: Int){
        FirebaseDatabase.getInstance().reference.child("Posts").child(
            mPostsList[position].getUid()!!
        ).removeValue()

    }
}