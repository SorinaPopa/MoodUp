package com.example.moodup.ui.register

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.example.moodup.MainActivity
import com.example.moodup.databinding.ActivityRegisterBinding
import com.example.moodup.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val registerViewModel: RegisterViewModel by viewModels()
    private lateinit var moodUpAuth: FirebaseAuth

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = moodUpAuth.currentUser
        if (currentUser != null) {
            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.registerViewModel = registerViewModel
        setContentView(binding.root)

        supportActionBar?.hide()

        moodUpAuth = FirebaseAuth.getInstance()

        loginHereButtonObserver()
        registerButtonObserver()
    }

    override fun onBackPressed() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Unsaved Data")
        alertDialogBuilder.setMessage("Are you sure you want to leave this page?")
        alertDialogBuilder.setPositiveButton("No") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        alertDialogBuilder.setNegativeButton("Yes") { _: DialogInterface, _: Int ->
            finish()
            super.onBackPressed()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun loginHereButtonObserver() {
        registerViewModel.onLoginHereButtonClicked.observe(this) { isClicked ->
            if (isClicked) {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
                registerViewModel.onLoginHereButtonClicked.value = false
            }
        }
    }

    private fun registerButtonObserver() {
        registerViewModel.onRegisterButtonClicked.observe(this) { isClicked ->
            if (isClicked) {
                if (registerViewModel.registerEmail.value.isNullOrBlank()) {
                    Toast.makeText(this@RegisterActivity, "Enter email", Toast.LENGTH_SHORT).show()
                    return@observe
                }
                if (registerViewModel.registerPassword.value.isNullOrBlank()) {
                    Toast.makeText(this@RegisterActivity, "Enter password", Toast.LENGTH_SHORT)
                        .show()
                    return@observe
                }

                val rEmail: String = registerViewModel.registerEmail.value!!
                val rPassword: String = registerViewModel.registerPassword.value!!

                Log.d("TAG", "!!!!Your email value: $rEmail")
                Log.d("TAG", "!!!!Your password value: $rPassword")

                moodUpAuth.createUserWithEmailAndPassword(rEmail, rPassword)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "createUserWithEmail:success")
                            Toast.makeText(
                                this@RegisterActivity,
                                "Authentication successful",
                                Toast.LENGTH_SHORT
                            ).show()

                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "createUserWithEmail:failure", task.exception)
                            Toast.makeText(
                                this@RegisterActivity,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    }
                registerViewModel.onRegisterButtonClicked.value = false
            }
        }
    }

}