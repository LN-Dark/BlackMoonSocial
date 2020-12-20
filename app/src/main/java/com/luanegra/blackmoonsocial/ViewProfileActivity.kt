package com.luanegra.blackmoonsocial

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import coil.load
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.auth.User
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.luanegra.blackmoonsocial.models.ASeguir
import com.luanegra.blackmoonsocial.models.Posts
import com.luanegra.blackmoonsocial.models.Users
import com.shashank.sony.fancytoastlib.FancyToast
import de.hdodenhof.circleimageview.CircleImageView

class ViewProfileActivity : AppCompatActivity() {
    private var firebaseUser: FirebaseUser?= null
    private var following_profile_visiti: TextView? = null
    private var posts_profile_visiti: TextView? = null
    private var follow_profile_visiti: TextView? = null
    private var idUserVisit: String? = ""
    var controlFollowID = ""
    var follow_visit: Button? = null
    var controlFollow = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_profile)
        setSupportActionBar(findViewById(R.id.toolbar_visit))
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_visit)
        val user_name_visit: TextView = findViewById(R.id.user_name_visit)
        val profile_image_visit: CircleImageView = findViewById(R.id.profile_image_visit)
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
        val cover_profile_visit: ImageView = findViewById(R.id.cover_profile_visit)
        val profileimage_profile_visit: CircleImageView = findViewById(R.id.profileimage_profile_visit)
        val aboutme_profile_visit: TextView = findViewById(R.id.aboutme_profile_visit)
        val sendmessage_visit: Button = findViewById(R.id.sendmessage_visit)
        follow_visit = findViewById(R.id.follow_visit)
        following_profile_visiti = findViewById(R.id.following_profile_visiti)
        posts_profile_visiti = findViewById(R.id.posts_profile_visiti)
        follow_profile_visiti = findViewById(R.id.follow_profile_visiti)
        idUserVisit = intent.getStringExtra("idUserVisit")
        var userVisit = Users()

        FirebaseDatabase.getInstance().reference.child("users").child(idUserVisit!!).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Users::class.java)
                profileimage_profile_visit.load(user!!.getprofile())
                profile_image_visit.load(user!!.getprofile())
                cover_profile_visit.load(user.getcover())
                aboutme_profile_visit.text = user.getaboutMe()
                user_name_visit.text = user.getusername()
                userVisit = user
            }

            override fun onCancelled(error: DatabaseError) {
                FancyToast.makeText(this@ViewProfileActivity,error.message, FancyToast.LENGTH_LONG, FancyToast.ERROR,true)
            }
        })
        sendmessage_visit.setOnClickListener{

        }
        FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid).child("following").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnapshot in snapshot.children){
                    val followingChilds: ASeguir? = datasnapshot.getValue(ASeguir::class.java)
                    if (followingChilds!!.getfollowingID() == idUserVisit){
                        follow_visit!!.text = "Unfollow"
                        controlFollow = true
                        controlFollowID = followingChilds.getuid()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        follow_visit!!.setOnClickListener {
            if (controlFollow){
                unfollow()
            }else{
                follow()
            }
        }
        getFollowers()
        getFollowing()
        getPosts()
    }

    fun follow(){
        val keyNewFallow =  FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid).push().key
        val map = HashMap<String, Any>()
        map["followingID"] = idUserVisit!!
        map["uid"] = keyNewFallow.toString()
        FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid).child("following").child(keyNewFallow!!).setValue(map)
        follow_visit!!.text = "Unfollow"
        controlFollow = true
        controlFollowID = keyNewFallow
    }

    fun unfollow(){
        FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid).child("following").child(controlFollowID).removeValue()
        follow_visit!!.text = "Follow"
        controlFollow = false
        followeersCount -= 1
        follow_profile_visiti!!.text = "Followed by $followeersCount"
        controlFollowID = ""

    }

    var followeersCount = 0
    fun getFollowers(){
        FirebaseDatabase.getInstance().reference.child("users").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                followeersCount = 0
                for (datasnapShot in snapshot.children){
                    val user: Users? = datasnapShot.getValue(Users::class.java)
                    if(user!!.getUid() != idUserVisit){
                        FirebaseDatabase.getInstance().reference.child("users").child(user!!.getUid()).child("following").addValueEventListener(object: ValueEventListener{
                            override fun onDataChange(snapshotFollowers: DataSnapshot) {
                                for (datasnapFollowers in snapshotFollowers.children){
                                    val follower: ASeguir? = datasnapFollowers.getValue(ASeguir::class.java)
                                    if (follower!!.getfollowingID() == idUserVisit){
                                        followeersCount += 1
                                        follow_profile_visiti!!.text = "Followed by $followeersCount"
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun getFollowing(){
        FirebaseDatabase.getInstance().reference.child("users").child(idUserVisit!!).child("following").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var followeersCount = -1
                for (datasnapShot in snapshot.children){
                    followeersCount += 1
                }
                following_profile_visiti!!.text = "Following $followeersCount"
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun getPosts(){
        FirebaseDatabase.getInstance().reference.child("Posts").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var postsCount = 0
                for (datasnapShot in snapshot.children){
                    val posts: Posts? = datasnapShot.getValue(Posts::class.java)
                    if (posts!!.getuserID() == idUserVisit){
                        postsCount += 1
                    }
                }
                posts_profile_visiti!!.text = "$postsCount Posts"
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}