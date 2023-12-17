package com.example.handsight

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

private class LoginActivity : AppCompatActivity (){
    lateinit var btnGoogle: Button
    lateinit var googleSignInClient : GoogleSignInClient
    lateinit var progressDialog : ProgressDialog

    var firebaseAuth = FirebaseAuth.getInstance()

    companion object{
        private const val RC_SIGN_IN = 10001
    }

    override fun onStart() {
        super.onStart()

        // Periksa apakah pengguna sudah masuk (signed in)
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // Pengguna sudah masuk, buka MainActivity
            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)
            finish() // Optional: Menutup aktivitas saat ini agar tidak dapat kembali ke halaman login
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnGoogle = findViewById(R.id.btn_google)
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Logging")
        progressDialog.setMessage("Wait the minute")

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN){
            //MENANGANI PROSES LOGIN GOOGLE
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                //JIKA Berhasil
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException){
                e.printStackTrace()
                Toast.makeText(applicationContext, e.localizedMessage, LENGTH_SHORT).show()
            }
        }
    }

    fun firebaseAuthWithGoogle(idToken: String){
        progressDialog.show()
        val credentian = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credentian)
            .addOnSuccessListener {
                startActivity(Intent(this, MainActivity::class.java))
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, error.localizedMessage, LENGTH_SHORT).show()
            }
            .addOnCompleteListener{
                progressDialog.dismiss()
            }
    }
}