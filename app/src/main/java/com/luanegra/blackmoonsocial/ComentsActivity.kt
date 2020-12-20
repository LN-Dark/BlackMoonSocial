package com.luanegra.blackmoonsocial

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.luanegra.blackmoonsocial.adapters.ComentsAdapter
import com.luanegra.blackmoonsocial.adapters.PostsAdapter
import com.luanegra.blackmoonsocial.adapters.UserAdapter
import com.luanegra.blackmoonsocial.models.*
import com.shashank.sony.fancytoastlib.FancyToast
import de.hdodenhof.circleimageview.CircleImageView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ComentsActivity : AppCompatActivity() {
    private var firebaseUser: FirebaseUser?= null
    private var mComentsList: List<Coments>?= null
    private var recycler_coments: RecyclerView? = null
    private var txt_send_coment: TextInputEditText? = null
    private var comentsAdapter: ComentsAdapter? = null
    private var send_coment: ImageView? = null
    private var postID: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coments)
        setSupportActionBar(findViewById(R.id.toolbar_coments))
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_coments)
        val user_name_newpost: TextView = findViewById(R.id.user_name_coments)
        val profile_image_newpost: CircleImageView = findViewById(R.id.profile_image_coments)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        firebaseUser = FirebaseAuth.getInstance().currentUser
        FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                    val currentUser = snapshot.getValue(Users::class.java)
                user_name_newpost.text = currentUser!!.getusername()
                profile_image_newpost.load(currentUser.getprofile())
            }

            override fun onCancelled(error: DatabaseError) {
                FancyToast.makeText(this@ComentsActivity,error.message ,FancyToast.LENGTH_LONG,FancyToast.ERROR,true)
            }

        })


        postID = intent.getStringExtra("postID")
        txt_send_coment = findViewById(R.id.write_comets)
        send_coment = findViewById(R.id.send_coment)
        mComentsList = ArrayList()
        recycler_coments = findViewById(R.id.recycler_coments)
        recycler_coments!!.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = false
        recycler_coments!!.layoutManager = linearLayoutManager
        comentsAdapter = ComentsAdapter(this, (mComentsList as ArrayList<Coments>))
        recycler_coments!!.adapter = comentsAdapter
        send_coment!!.setOnClickListener {
            if (!txt_send_coment!!.text.isNullOrEmpty()){
                sendComent()
            }else{
                FancyToast.makeText(this@ComentsActivity,"Write a coment first!",FancyToast.LENGTH_LONG,FancyToast.WARNING,true)
            }
        }
        getAllComents()
    }

    fun sendComent(){
        val newcomentID = FirebaseDatabase.getInstance().reference.child("Coments").child(postID!!).push().key
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
        val formatted = current.format(formatter)
        val map = HashMap<String, Any>()
        map["uID"] = newcomentID.toString()
        map["userComentID"] = firebaseUser!!.uid
        map["coment"] = txt_send_coment!!.text.toString()
        map["date"] = formatted
        map["postID"] = postID!!
        FirebaseDatabase.getInstance().reference.child("Coments").child(postID!!).child(newcomentID.toString()).setValue(map)
        txt_send_coment!!.setText("")
    }

    fun getAllComents(){
        FirebaseDatabase.getInstance().reference.child("Coments").child(postID!!).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (mComentsList as ArrayList<Coments>).clear()
                recycler_coments?.adapter?.notifyDataSetChanged()
                    if (snapshot.exists()) {
                        for (datasnapshot in snapshot.children) {
                            val newComent: Coments? = datasnapshot.getValue(Coments::class.java)
                            getLikes(newComent!!)
                        }
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                FancyToast.makeText(this@ComentsActivity,error.message ,FancyToast.LENGTH_LONG,FancyToast.ERROR,true)
            }
        })
    }

    fun getLikes(coment: Coments){
        FirebaseDatabase.getInstance().reference.child("ComentsLikes").child(coment.getpostID()).child(coment.getuID()).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var likesCounter = 0
                    for (datasnapshot in snapshot.children) {
                        likesCounter +=1
                    }
                    if (!mComentsList!!.contains(coment)){
                        coment.setlikes(likesCounter)
                        (mComentsList as ArrayList<Coments>).add(coment)
                    }else{
                        mComentsList!!.get(mComentsList!!.indexOf(coment)).setlikes(likesCounter)
                    }
                    comentsAdapter!!.notifyDataSetChanged()
                }else{
                    (mComentsList as ArrayList<Coments>).add(coment)
                    comentsAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                FancyToast.makeText(this@ComentsActivity,error.message ,FancyToast.LENGTH_LONG,FancyToast.ERROR,true)
            }
        })
    }
}