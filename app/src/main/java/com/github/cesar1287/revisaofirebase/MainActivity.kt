package com.github.cesar1287.revisaofirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.cesar1287.revisaofirebase.databinding.ActivityMainBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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
    }

    override fun onResume() {
        super.onResume()
        Firebase.auth.currentUser?.let {
            binding.tieLoginEmail.text = it.email?.getEditable()
            //Cesar Rodrigues Nascimento
            // name = [Cesar, Rodrigues, Nascimento]
            val name = it.displayName?.split(" ")
            binding.tieLoginName.text = name?.firstOrNull()?.getEditable()
            binding.tieLoginLastName.text = name?.lastOrNull()?.getEditable()
        } ?: run {
            signIn()
        }
    }

    private fun signIn() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}