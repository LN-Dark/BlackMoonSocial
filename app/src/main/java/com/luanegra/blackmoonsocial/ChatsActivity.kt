package com.luanegra.blackmoonsocial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.luanegra.blackmoonsocial.adapters.ChatsAdapter
import com.luanegra.blackmoonsocial.models.ChatList
import com.luanegra.blackmoonsocial.models.Users
import com.luanegra.blackmoonsocial.notifications.Token
import java.util.*
import kotlin.collections.ArrayList

class ChatsActivity : AppCompatActivity() {
    private var userAdapter: ChatsAdapter? = null
    private var mUsers: List<Users>?= null
    private var usersChatList: List<ChatList>? = null
    lateinit var recycler_chats: RecyclerView
    private var firebaseUser: FirebaseUser? = null
    private var txt_search: TextInputEditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)
        recycler_chats = findViewById(R.id.recycler_chats)
        recycler_chats.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = false
        recycler_chats.layoutManager = linearLayoutManager
        firebaseUser = FirebaseAuth.getInstance().currentUser
        usersChatList = ArrayList()
        txt_search = findViewById(R.id.txt_search)
        val chatListsRef = FirebaseDatabase.getInstance().reference.child("ChatLists").child(firebaseUser!!.uid)
        chatListsRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (usersChatList as ArrayList).clear()
                for(datasnap in snapshot.children){
                    val chat = datasnap.getValue(ChatList::class.java)
                    (usersChatList as ArrayList).add(chat!!)
                }
                retrieveChatList()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        updateToken(FirebaseInstanceId.getInstance().token)
        txt_search!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                retrieveSearchUsers(s.toString().toLowerCase(Locale.ROOT))
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun updateToken(token: String?) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val token1 = Token(token.toString())
        ref.child(firebaseUser!!.uid).setValue(token1)
    }

    private val usersRef = FirebaseDatabase.getInstance().reference.child("users")
    private var retrieveEventListener: ValueEventListener? = null
    private fun retrieveChatList(){
        mUsers = ArrayList()
        retrieveEventListener = usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList).clear()
                for(datasnap in snapshot.children){
                    val user = datasnap.getValue(Users::class.java)
                    for(eachChatList in usersChatList!!){
                        if(user!!.getUid() == eachChatList.getid()){
                            (mUsers as ArrayList).add(user)
                        }
                    }
                }
                userAdapter = ChatsAdapter(this@ChatsActivity, (mUsers as ArrayList<Users>), true, 1)
                recycler_chats.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun retrieveSearchUsers(searchname: String) {
        if(searchname != ""){
            val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
            val queryUsers = FirebaseDatabase.getInstance().reference.child("users").orderByChild("search").startAt(
                searchname
            ).endAt(searchname + "\uf8ff")
            queryUsers.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    (mUsers as ArrayList<Users>).clear()
                    recycler_chats?.adapter?.notifyDataSetChanged()
                    if (snapshot.exists()) {
                        for (user in snapshot.children) {
                            val newuser: Users? = user.getValue(Users::class.java)
                            if ((newuser!!.getUid()) != firebaseUser) {
                                (mUsers as ArrayList<Users>).add(newuser)
                            }
                        }
                        userAdapter = ChatsAdapter(this@ChatsActivity, mUsers!!, false, 0)
                        recycler_chats.adapter = userAdapter
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }else{
            retrieveChatList()
        }
    }

    override fun onPause() {
        super.onPause()
        usersRef.removeEventListener(retrieveEventListener!!)
    }
}