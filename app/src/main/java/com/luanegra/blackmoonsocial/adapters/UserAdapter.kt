package com.luanegra.blackmoonsocial.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.preference.PreferenceManager
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.luanegra.blackmoonsocial.R
import com.luanegra.blackmoonsocial.ViewProfileActivity
import com.luanegra.blackmoonsocial.models.Blocked
import com.luanegra.blackmoonsocial.models.Users
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream


class UserAdapter(mContext: Context, mUserList: List<Users>, isChatCheck: Boolean, type: Int) : RecyclerView.Adapter<UserAdapter.ViewHolder?>() {
    private val mContext = mContext
    private val mUserList: List<Users> = mUserList
    private var isChatCheck: Boolean
    private var lastMsg: String = ""
    var visualType: Int = type

    init {
        this.isChatCheck = isChatCheck
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txt_userName: TextView = itemView.findViewById(R.id.user_name_search)
        var image_profile: CircleImageView
        var image_online: CircleImageView
        var image_offline: CircleImageView

        init {
            image_profile = itemView.findViewById(R.id.profile_image_search)
            image_online = itemView.findViewById(R.id.image_online_search)
            image_offline = itemView.findViewById(R.id.image_offline_search)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view: View?
            view = LayoutInflater.from(mContext).inflate(
                R.layout.usersearch_item_layout,
                parent,
                false
            )

        return ViewHolder(view!!)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user: Users = mUserList[position]
        holder.txt_userName.text = user.getusername()
        holder.image_profile.load(user.getprofile())
        holder.itemView.setOnClickListener {

            val mDialogView = LayoutInflater.from(mContext).inflate(
                R.layout.alertdialog_profile_options,
                null
            )

            val mBuilder = AlertDialog.Builder(mContext)
                .setView(mDialogView)
            val  mAlertDialog = mBuilder.show()

            val dialogimageview: CircleImageView = mDialogView.findViewById(R.id.profile_dialog)
            dialogimageview.load(user.getprofile())

            mDialogView.findViewById<Button>(R.id.perfil_dialog_show).setOnClickListener {
                val intent = Intent(mContext, ViewProfileActivity::class.java)
                intent.putExtra("idUserVisit", user.getUid())
                mContext.startActivity(intent)
                mAlertDialog.dismiss()
            }
            var controllerBlock = 0
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val refblockedUsers = FirebaseDatabase.getInstance().reference.child("BlockedUsers").child(
                firebaseUser!!.uid
            )
            refblockedUsers.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (dataSnapshot in snapshot.children) {
                            val userID: Blocked? = dataSnapshot.getValue(Blocked::class.java)
                            if (userID!!.getuserID() == user.getUid()) {
                                controllerBlock = 1
                            }
                        }
                        if (controllerBlock == 0) {
                            mDialogView.findViewById<Button>(R.id.chat_block).text ="Block"

                        } else {
                            mDialogView.findViewById<Button>(R.id.chat_block).text = "Unblock"

                        }
                    } else {
                        mDialogView.findViewById<Button>(R.id.chat_block).text = "Block"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
            mDialogView.findViewById<Button>(R.id.chat_block).setOnClickListener {
                blockUser(user.getUid(), user.getusername())
                mAlertDialog.dismiss()
            }
            mDialogView.findViewById<Button>(R.id.chat_dialog_show).setOnClickListener {
//                val intent = Intent(mContext, MessageChatActivity::class.java)
//                intent.putExtra("reciever_id", user.getUid())
//                intent.putExtra("reciever_profile", user.getprofile())
//                intent.putExtra("reciever_username", user.getusername())
//                intent.putExtra("publicKeyVisit", user.getpublicKey())
//                intent.putExtra("resultAUTH", "true")
//                mContext.startActivity(intent)
//                mAlertDialog.dismiss()
            }
        }

        if(user.getstatus() == "online"){
            holder.image_online.visibility = View.VISIBLE
            holder.image_offline.visibility = View.GONE
        }else{
            holder.image_online.visibility = View.GONE
            holder.image_offline.visibility = View.VISIBLE
        }
    }

    private fun blockUser(usertoblock: String, username: String){
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val refblockedUsers = FirebaseDatabase.getInstance().reference.child("BlockedUsers").child(
            firebaseUser!!.uid
        )
        var controllerBlock = 0
        refblockedUsers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val userID: Blocked? = dataSnapshot.getValue(Blocked::class.java)
                        if (userID!!.getuserID() == usertoblock) {
                            FirebaseDatabase.getInstance().reference.child("BlockedUsers").child(
                                firebaseUser.uid
                            ).child(userID.getuid()).removeValue()
                            controllerBlock = 1
                            Toast.makeText(
                                mContext,
                                "Unblocked user " + username,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    if (controllerBlock == 0) {
                        val userHashMap = HashMap<String, Any>()
                        val idBlock = refblockedUsers.push().key.toString()
                        userHashMap["uid"] = idBlock
                        userHashMap["userID"] = usertoblock
                        userHashMap["conditionBlock"] = "true"
                        FirebaseDatabase.getInstance().reference.child("BlockedUsers").child(
                            firebaseUser.uid
                        ).child(idBlock).updateChildren(userHashMap)
                        Toast.makeText(
                            mContext,
                            "Blocked user " + username,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    val userHashMap = HashMap<String, Any>()
                    val idBlock = refblockedUsers.push().key.toString()
                    userHashMap["uid"] = idBlock
                    userHashMap["userID"] = usertoblock
                    userHashMap["conditionBlock"] = "true"
                    FirebaseDatabase.getInstance().reference.child("BlockedUsers").child(
                        firebaseUser.uid
                    ).child(idBlock).updateChildren(userHashMap)
                    Toast.makeText(
                        mContext,
                        "Blocked user " + username,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    override fun getItemCount(): Int {
        return mUserList.size
    }

}