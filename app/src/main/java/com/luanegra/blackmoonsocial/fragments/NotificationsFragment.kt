package com.luanegra.blackmoonsocial.fragments

import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.luanegra.blackmoonsocial.R
import com.luanegra.blackmoonsocial.adapters.MyPostsAdapter
import com.luanegra.blackmoonsocial.models.Likes
import com.luanegra.blackmoonsocial.models.Posts

class NotificationsFragment : Fragment() {
    var firebaseUser: FirebaseUser?= null
    private var postsAdapter: MyPostsAdapter? = null
    lateinit var recycler_myposts: RecyclerView
    private var postsList: List<Posts>? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        recycler_myposts = view.findViewById(R.id.recycler_myposts)
        recycler_myposts.setHasFixedSize(true)
        val display = requireActivity().windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val lLayout: GridLayoutManager
        val density = resources.displayMetrics.density
        val dpWidth = outMetrics.widthPixels / density
        val columns = Math.round(dpWidth / 200)
        lLayout = GridLayoutManager(activity, columns)
        recycler_myposts.layoutManager = lLayout
        postsList = ArrayList()
        postsAdapter = MyPostsAdapter(context!!, (postsList as ArrayList<Posts>))
        recycler_myposts.adapter = postsAdapter
        retrievePosts()
        return view
    }

    fun retrievePosts(){
        FirebaseDatabase.getInstance().reference.child("Posts").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (postsList as ArrayList).clear()
                for(datasnapshot in snapshot.children){
                    val post = datasnapshot.getValue(Posts::class.java)
                    if (post!!.getuserID() == firebaseUser!!.uid){
                        retrieveLikes(post)
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
                if (!postsList!!.contains(post)){
                    (postsList as ArrayList).add(post)
                    postsAdapter!!.notifyDataSetChanged()
                }else{
                    postsList!!.get(postsList!!.indexOf(post)).setlikes(listaLikes)
                    postsAdapter!!.notifyDataSetChanged()
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}