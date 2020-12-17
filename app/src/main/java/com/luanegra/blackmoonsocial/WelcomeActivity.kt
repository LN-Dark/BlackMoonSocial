package com.luanegra.blackmoonsocial

import android.R.attr.bottom
import android.R.attr.textSize
import android.R.attr.top
import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Shader.TileMode
import android.os.Bundle
import android.text.BoringLayout
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.luanegra.blackmoonsocial.RSA.GenerateKeys
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class WelcomeActivity : AppCompatActivity() {
    var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //Firebase Auth instance
        mAuth = FirebaseAuth.getInstance()
        val sign_in_btn = findViewById<Button>(R.id.button_google_signin)
        sign_in_btn.setOnClickListener {
            signIn()
        }
    }

    override fun onStart() {
        super.onStart()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        if(firebaseUser != null){
            val intent = Intent(this@WelcomeActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    companion object {
        private const val RC_SIGN_IN = 120
    }

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserId: String = ""

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account)
                } catch (e: ApiException) {
                    Toast.makeText(
                        this@WelcomeActivity,
                        "getString(R.string.errormessage)" + e.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {

                    Toast.makeText(
                        this@WelcomeActivity,
                        "getString(R.string.errormessage)" + task.exception.toString(),
                        Toast.LENGTH_LONG
                    ).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                firebaseUserId = mAuth.currentUser!!.uid
                refUsers = FirebaseDatabase.getInstance().reference.child("users").child(firebaseUserId)
                var followingkey = refUsers.push().key
                val userHashMap = HashMap<String, Any>()
                userHashMap["uid"] = firebaseUserId
                userHashMap["username"] = account.displayName.toString()
                userHashMap["email"] = account.email.toString()
                userHashMap["profile"] = account.photoUrl.toString()
                userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/blackmoonsocial-79e70.appspot.com/o/coverdefault.jpg?alt=media&token=bc3f2111-f92e-43c4-87ca-994bb418f305"
                userHashMap["status"] = "offline"
                userHashMap["search"] = account.displayName.toString().toLowerCase(Locale.ROOT)
                userHashMap["aboutMe"] = "About me"
                userHashMap["following/$followingkey/followingID"] = firebaseUserId
                userHashMap["following/$followingkey/uid"] = followingkey!!
                val gerarChaves = GenerateKeys()
                var listKeys: List<String>?
                listKeys = ArrayList()
                listKeys = gerarChaves.generateKeys()
                userHashMap["publicKey"] = listKeys[0]
                val sharedPreference =  getSharedPreferences("RSA_CHAT", MODE_PRIVATE)
                val editor = sharedPreference.edit()
                editor.putString("privateKey", listKeys[1])
                editor.putString("publicKey", listKeys[0])
                editor.apply()
                refUsers.updateChildren(userHashMap).addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        val intent = Intent(this@WelcomeActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }else{
                        Toast.makeText(
                            this@WelcomeActivity,
                            "getString(R.string.errormessage)" + task.exception!!.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this@WelcomeActivity,
                    "getString(R.string.errormessage)" + task.exception!!.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    }
}