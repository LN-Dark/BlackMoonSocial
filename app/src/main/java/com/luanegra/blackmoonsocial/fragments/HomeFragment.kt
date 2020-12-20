package com.luanegra.blackmoonsocial.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.luanegra.blackmoonsocial.R
import com.luanegra.blackmoonsocial.adapters.PostsAdapter
import com.luanegra.blackmoonsocial.models.ASeguir
import com.luanegra.blackmoonsocial.models.Likes
import com.luanegra.blackmoonsocial.models.Posts

class HomeFragment : Fragment() {
    var firebaseUser: FirebaseUser?= null
    private var postsAdapter: PostsAdapter? = null
    lateinit var recycler_home: RecyclerView
    private var postsList: List<Posts>? = null
    private var followingList: List<ASeguir>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        recycler_home = view.findViewById(R.id.recycler_home)
        recycler_home.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.stackFromEnd = false
        recycler_home.layoutManager = linearLayoutManager
        postsList = ArrayList()
        followingList = ArrayList()
        postsAdapter = PostsAdapter(context!!, (postsList as ArrayList<Posts>))
        recycler_home.adapter = postsAdapter
        FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid).child("following").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (followingList as ArrayList).clear()
                for(datasnapshot in snapshot.children){
                    val following = datasnapshot.getValue(ASeguir::class.java)
                    (followingList as ArrayList).add(following!!)
                }
                retrievePosts()

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


        return view
    }

    fun retrievePosts(){
        FirebaseDatabase.getInstance().reference.child("Posts").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (postsList as ArrayList).clear()
                for(datasnapshot in snapshot.children){
                    val post = datasnapshot.getValue(Posts::class.java)
                    for (follow in followingList!!){
                        if (post!!.getuserID() == follow.getfollowingID()){
                            retrieveLikes(post)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun retrieveLikes(post: Posts){
        FirebaseDatabase.getInstance().reference.child("Likes").child(post.getUid()).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaLikes: List<Likes>
                listaLikes = ArrayList()
                for(datasnapshot1 in snapshot.children){
                    val like = datasnapshot1.getValue(Likes::class.java)
                    (listaLikes as ArrayList).add(like!!)
                }
                post.setlikes(listaLikes)
                reArrangeArray(post)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    fun reArrangeArray(post: Posts){
        if (!postsList!!.contains(post)){
            (postsList as ArrayList).reverse()
            (postsList as ArrayList).add(post)
            (postsList as ArrayList).reverse()
            postsAdapter!!.notifyDataSetChanged()
        }else{
            postsList!!.get(postsList!!.indexOf(post)).setlikes(post.getlikes())
            postsAdapter!!.notifyDataSetChanged()
        }
    }
}