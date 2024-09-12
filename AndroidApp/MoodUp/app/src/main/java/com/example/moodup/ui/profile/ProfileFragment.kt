package com.example.moodup.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.moodup.R
import com.example.moodup.databinding.FragmentProfileBinding
import com.example.moodup.ui.login.LoginActivity
import com.example.moodup.ui.register.RegisterActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.Date

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var moodUpAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.profileViewModel = profileViewModel

        moodUpAuth = FirebaseAuth.getInstance()
        currentUser = moodUpAuth.currentUser!!

        //TODO: Create an edit profile fragment then remove the following line
        binding.editProfileButton.visibility = View.GONE

        infoButtonObserver()
        analysisButtonObserver()
        trainAIButtonObserver()
        logoutButtonObserver()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showQuitAppDialog()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun infoButtonObserver() {
        profileViewModel.onInfoButtonClicked.observe(viewLifecycleOwner) { isClicked ->
            if (isClicked) {
                showInfoDialog()
            }
        }
    }

    private fun analysisButtonObserver() {
        profileViewModel.onAnalysisButtonClicked.observe(viewLifecycleOwner) { isClicked ->
            if (isClicked) {
                profileViewModel.calculateAllDailyScores()
                findNavController().navigate(R.id.action_profileFragment_to_analysisFragment)
                profileViewModel.onAnalysisButtonClicked.value = false
            }
        }
    }

    private fun trainAIButtonObserver() {
        profileViewModel.onTrainAIButtonClicked.observe(viewLifecycleOwner) { isClicked ->
            if (isClicked) {
                showTrainAIDialog()
            }
        }
    }

    private fun logoutButtonObserver() {
        profileViewModel.onLogoutButtonClicked.observe(viewLifecycleOwner) { isClicked ->
            if (isClicked) {
                showLogoutDialog()
            }
        }
    }

    // ----- DIALOGS ----- //

    private fun showInfoDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("App Info")
            .setMessage(R.string.app_info)
            .setPositiveButton("Learn More") { _, _ ->
                val uri: Uri =
                    Uri.parse("https://drive.google.com/file/d/1R_b9gJt9-Fmd19vwGQ8whLCoJ7PS6qoJ/view?usp=sharing")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
            .setNegativeButton("Close") { dialog, _ ->
                dialog.dismiss()
                profileViewModel.onInfoButtonClicked.value = false
            }
            .show()
    }

    private fun showTrainAIDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Coming Soon")
            .setMessage("Here will be the place where you will be able to personalise your AI!")
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
                profileViewModel.onTrainAIButtonClicked.value = false
            }
            .show()
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Log Out?")
            .setMessage("Are you sure you want log out from your account?")
            .setPositiveButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Yes") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(activity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                activity?.finish()
                profileViewModel.onLogoutButtonClicked.value = false
            }
            .show()
    }

    private fun showQuitAppDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Leaving so Soon?")
            .setMessage("Are you sure you want to exit the app?")
            .setPositiveButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Yes") { _, _ ->
                activity?.finish()
            }
            .show()
    }
}