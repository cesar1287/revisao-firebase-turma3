package com.github.cesar1287.revisaofirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.github.cesar1287.revisaofirebase.databinding.ActivityMainBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val db by lazy {
        Firebase.firestore
    }

    private val auth by lazy {
        Firebase.auth
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservables()
    }

    private fun setupObservables() {
        binding.btLoginLogout.setOnClickListener {
            Firebase.auth.signOut()
            signIn()
        }

        binding.btLoginSave.setOnClickListener {
            val userData = hashMapOf(
                "name" to binding.tieLoginName.text.toString(),
                "email" to binding.tieLoginEmail.text.toString(),
                "last_name" to binding.tieLoginLastName.text.toString(),
                "phone" to binding.tieLoginPhone.text.toString()
            )

            db.collection(FIREBASE_COLLECTION_USERS)
                .document(auth.currentUser?.uid ?: "")
                .set(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Dados salvos com sucesso", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onResume() {
        super.onResume()
        auth.currentUser?.let {
            val documentReference = db.collection(FIREBASE_COLLECTION_USERS).document(it.uid)
            documentReference.get()
                .addOnSuccessListener { snapshot ->
                    val userData = snapshot.data
                    binding.tieLoginEmail.text = (userData?.get("email") as String).getEditable()
                    //Cesar Rodrigues Nascimento
                    // name = [Cesar, Rodrigues, Nascimento]
                    val name = userData["name"] as String
                    val lastName = userData["last_name"] as String
                    binding.tieLoginName.text = name.getEditable()
                    binding.tieLoginLastName.text = lastName.getEditable()
                    binding.tieLoginPhone.text = (userData["phone"] as String).getEditable()
                }.addOnFailureListener {
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            signIn()
        }
    }

    private fun signIn() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    companion object {
        private const val FIREBASE_COLLECTION_USERS = "users"
    }
}