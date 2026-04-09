//package com.example.myapplication.ui
package com.example.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
//import com.example.myapplication.SignUpActivity
//import com.example.myapplication.databinding.ActivityLoginBinding
import com.example.newsprojectpractice.databinding.ActivityLoginBinding
import com.example.newsprojectpractice.ui.SignUpActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser

                        if (user != null && user.isEmailVerified) {
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, NewsActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Please verify your email address first.", Toast.LENGTH_LONG).show()
                            auth.signOut()
                        }
                    } else {
                        Toast.makeText(this, "Invalid email or password. Please try again.", Toast.LENGTH_LONG).show()
                    }
                }
        }


        binding.tvForgotPassword.setOnClickListener {

            val email = binding.etEmail.text.toString().trim()


            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email first to reset password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        Toast.makeText(this, "Password reset link sent to your email.", Toast.LENGTH_LONG).show()
                    } else {

                        Toast.makeText(this, "Error: Could not send reset email.", Toast.LENGTH_LONG).show()
                    }
                }
        }

        binding.tvSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }


    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser

        if (currentUser != null && currentUser.isEmailVerified) {
            val intent = Intent(this, NewsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}