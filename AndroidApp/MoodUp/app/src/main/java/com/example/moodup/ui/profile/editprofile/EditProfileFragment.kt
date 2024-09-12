package com.example.moodup.ui.profile.editprofile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.moodup.R
import com.example.moodup.databinding.FragmentEditProfileBinding

class EditProfileFragment : Fragment() {

    private val editProfileViewModel: EditProfileViewModel by viewModels()
    private lateinit var binding: FragmentEditProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.editProfileViewModel = editProfileViewModel

        arrowButtonListener()

        return binding.root

    }

    private fun arrowButtonListener() {
        editProfileViewModel.onArrowButtonClicked.observe(viewLifecycleOwner) { isClicked ->
            if (isClicked) {
                findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
                editProfileViewModel.onArrowButtonClicked.value = false
            }
        }
    }

}