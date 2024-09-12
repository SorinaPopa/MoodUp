package com.example.moodup.ui.login

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.example.moodup.MainActivity
import com.example.moodup.databinding.ActivityLoginBinding
import com.example.moodup.ui.register.RegisterActivity
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var moodUpAuth: FirebaseAuth

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = moodUpAuth.currentUser
        if (currentUser != null) {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.loginViewModel = loginViewModel
        setContentView(binding.root)

        supportActionBar?.hide()

        moodUpAuth = FirebaseAuth.getInstance()

        registerHereButtonObserver()
        loginButtonObserver()
    }

    override fun onBackPressed() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Leaving so soon?")
        alertDialogBuilder.setMessage("Are you sure you want to exit the app?")
        alertDialogBuilder.setPositiveButton("No") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        alertDialogBuilder.setNegativeButton("Yes") { _: DialogInterface, _: Int ->
            super.onBackPressed()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun registerHereButtonObserver() {
        loginViewModel.onRegisterHereButtonClicked.observe(this) { isClicked ->
            if (isClicked) {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
                loginViewModel.onRegisterHereButtonClicked.value = false
            }
        }
    }

    private fun loginButtonObserver() {
        loginViewModel.onLoginButtonClicked.observe(this) { isClicked ->
            if (isClicked) {
                if (loginViewModel.loginEmail.value.isNullOrBlank()) {
                    Toast.makeText(this@LoginActivity, "Enter email", Toast.LENGTH_SHORT).show()
                    return@observe
                }
                if (loginViewModel.loginPassword.value.isNullOrBlank()) {
                    Toast.makeText(this@LoginActivity, "Enter password", Toast.LENGTH_SHORT).show()
                    return@observe
                }

                val lEmail: String = loginViewModel.loginEmail.value!!
                val lPassword: String = loginViewModel.loginPassword.value!!

                Log.d("TAG", "!!!!Your email value: $lEmail")
                Log.d("TAG", "!!!!Your password value: $lPassword")

                moodUpAuth.signInWithEmailAndPassword(lEmail, lPassword)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithEmail:success")
                            Toast.makeText(
                                baseContext,
                                "Authentication successful.",
                                Toast.LENGTH_SHORT,
                            ).show()

                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                loginViewModel.onLoginButtonClicked.value = false
            }
        }
    }
}