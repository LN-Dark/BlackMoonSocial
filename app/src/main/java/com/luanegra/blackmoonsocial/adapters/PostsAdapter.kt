package com.luanegra.blackmoonsocial.adapters

import android.content.Context
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
import com.luanegra.blackmoonsocial.R
import com.luanegra.blackmoonsocial.models.Likes
import com.luanegra.blackmoonsocial.models.Posts
import com.luanegra.blackmoonsocial.models.Users
import de.hdodenhof.circleimageview.CircleImageView

class PostsAdapter(mContext: Context, mPostsList: List<Posts>) : RecyclerView.Adapter<PostsAdapter.ViewHolder?>() {
    private val mContext = mContext
    private val mPostsList: List<Posts> = mPostsList
    var firebaseUser: FirebaseUser?= null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var user_profileimage_card_home: CircleImageView
        var txt_username_card_home: TextView
        var txt_date_card_home: TextView
        var description_post_home: TextView
        var img_card_home: ImageView
        var txt_likes_home_card: TextView
        var img_likes_card_home: ImageView
        var img_coment_card_home: ImageView
        var img_sendmessage_card_home: ImageView

        init {
            user_profileimage_card_home = itemView.findViewById(R.id.user_profileimage_card_home)
            txt_username_card_home = itemView.findViewById(R.id.txt_username_card_home)
            txt_date_card_home = itemView.findViewById(R.id.txt_date_card_home)
            img_card_home = itemView.findViewById(R.id.img_card_home)
            txt_likes_home_card = itemView.findViewById(R.id.txt_likes_home_card)
            img_likes_card_home = itemView.findViewById(R.id.img_likes_card_home)
            img_coment_card_home = itemView.findViewById(R.id.img_coment_card_home)
            img_sendmessage_card_home = itemView.findViewById(R.id.img_sendmessage_card_home)
            description_post_home = itemView.findViewById(R.id.description_post_home)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view: View?
        view = LayoutInflater.from(mContext).inflate(
                R.layout.home_item_layout,
                parent,
                false
        )

        return ViewHolder(view!!)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val post: Posts = mPostsList[position]
        if (post.getlikes() != null){
            holder.txt_likes_home_card.text = post.getlikes()!!.size.toString()
        }else{
            holder.txt_likes_home_card.text = "0"
        }
        holder.txt_date_card_home.text = post.getdate()
        holder.img_card_home.load(post.getphotoURL())
        holder.description_post_home.text = post.getdescription()
        FirebaseDatabase.getInstance().reference.child("users").child(post.getuserID()).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: Users? = snapshot.getValue(Users::class.java)
                holder.txt_username_card_home.text = user!!.getusername()
                holder.user_profileimage_card_home.load(user.getprofile())
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        var userliked: Likes? = null
        FirebaseDatabase.getInstance().reference.child("Likes").child(post.getUid()).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                    for (datasnapshot in snapshot.children){
                        val like: Likes? = datasnapshot.getValue(Likes::class.java)
                        if (like!!.getuserID() == firebaseUser!!.uid){
                            userliked = like
                            holder.img_likes_card_home.setImageResource(R.drawable.ic_heart_outline)
                        }
                    }
                val doubleClick = DoubleClick(object : DoubleClickListener {
                    override fun onSingleClickEvent(view: View?) {
                        // DO STUFF SINGLE CLICK
                    }

                    override fun onDoubleClickEvent(view: View?) {
                        if (userliked != null){
                            FirebaseDatabase.getInstance().reference.child("Likes").child(post.getUid()).child(userliked!!.getUid()).removeValue()
                            holder.img_likes_card_home.setImageResource(R.drawable.ic_unheart_outline)
                            userliked = null

                        }else{
                            val likeHashMap = HashMap<String, Any?>()
                            val keyNewLike = FirebaseDatabase.getInstance().reference.child("Likes").child(post.getUid()).push().key
                            likeHashMap["uid"] = keyNewLike
                            likeHashMap["userID"] = firebaseUser!!.uid
                            FirebaseDatabase.getInstance().reference.child("Likes").child(post.getUid()).child(keyNewLike.toString()).setValue(likeHashMap)
                            userliked = Likes(keyNewLike!!, firebaseUser!!.uid)
                            holder.img_likes_card_home.setImageResource(R.drawable.ic_heart_outline)

                        }
                    }
                })
                holder.img_card_home.setOnClickListener(doubleClick)

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        holder.img_coment_card_home.setOnClickListener {

        }

        holder.img_sendmessage_card_home.setOnClickListener {

        }

        holder.img_likes_card_home.setOnClickListener {
            if (userliked != null){
                FirebaseDatabase.getInstance().reference.child("Likes").child(post.getUid()).child(userliked!!.getUid()).removeValue()
                holder.img_likes_card_home.setImageResource(R.drawable.ic_unheart_outline)
                userliked = null

            }else{
                val likeHashMap = HashMap<String, Any?>()
                val keyNewLike = FirebaseDatabase.getInstance().reference.child("Likes").child(post.getUid()).push().key
                likeHashMap["uid"] = keyNewLike
                likeHashMap["userID"] = firebaseUser!!.uid
                FirebaseDatabase.getInstance().reference.child("Likes").child(post.getUid()).child(keyNewLike.toString()).setValue(likeHashMap)
                userliked = Likes(keyNewLike!!, firebaseUser!!.uid)
                holder.img_likes_card_home.setImageResource(R.drawable.ic_heart_outline)

            }
        }



    }

    override fun getItemCount(): Int {
        return mPostsList.size
    }

}