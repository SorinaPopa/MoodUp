package com.example.moodup.ui.chat

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moodup.R
import com.example.moodup.databinding.FragmentChatBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private val chatViewModel: ChatViewModel by viewModels()
    private lateinit var userInput: TextInputLayout
    private val SPEECH_RECOGNIZER_REQUEST_CODE = 1

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = FragmentChatBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.chatViewModel = chatViewModel
        userInput = binding.messageInput

        sharedPreferences = requireActivity().getSharedPreferences("chatId", Context.MODE_PRIVATE)

        val adapter = ChatAdapter() { message, textView ->
            val popup = PopupMenu(context, textView)
            try {
                val fields = popup.javaClass.declaredFields
                for (field in fields) {
                    if ("mPopup" == field.name) {
                        field.isAccessible = true
                        val menuPopupHelper = field.get(popup)
                        val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                        val setForceIcons = classPopupHelper.getMethod(
                            "setForceShowIcon", Boolean::class.javaPrimitiveType
                        )
                        setForceIcons.invoke(menuPopupHelper, true)
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            popup.menuInflater.inflate(R.menu.option_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.copy_menu -> {
                        return@setOnMenuItemClickListener true
                    }

                    R.id.select_menu -> {
                        return@setOnMenuItemClickListener true
                    }

                    R.id.share_menu -> {
                        return@setOnMenuItemClickListener true
                    }

                    else -> {
                        return@setOnMenuItemClickListener true
                    }
                }
            }
            popup.show()

        }
        val recyclerView = binding.messagesRecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        //adapter.submitList(chatList)

        micButtonObserver()
        sendButtonObserver(adapter, recyclerView)

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

    private fun micButtonObserver() {
        chatViewModel.onMicButtonClicked.observe(viewLifecycleOwner) { isClicked ->
            if (isClicked) {
                val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                speechRecognizerIntent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!")
                startActivityForResult(speechRecognizerIntent, SPEECH_RECOGNIZER_REQUEST_CODE)
                chatViewModel.onMicButtonClicked.value = false
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SPEECH_RECOGNIZER_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val matches: ArrayList<String>? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                val spokenText = matches[0]
                userInput.editText?.setText(spokenText)
            }
        }
    }

    private fun sendButtonObserver(adapter: ChatAdapter, recyclerView: RecyclerView) {

        chatViewModel.onSendButtonClicked.observe(viewLifecycleOwner) { isClicked ->
            if (isClicked) {
                val messageInput: String = chatViewModel.messageInput.value!!.trim()

                if (messageInput.isNotEmpty()) {
                    val chatId = generateChatId()
                    chatViewModel.addChatsToDB(chatId, messageInput, "sender")
                    chatViewModel.messageInput.value = ""
                } else {
                    Toast.makeText(context, "please write something first!", Toast.LENGTH_LONG)
                        .show()
                }
                val chatId = generateChatId()
                chatViewModel.createChatCompletion(messageInput, chatId)
            }
        }

        chatViewModel.chatList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            recyclerView.smoothScrollToPosition(it.size)
        }
        chatViewModel.onSendButtonClicked.value = false
    }


    private fun generateChatId(): Int {
        val currentCounter = sharedPreferences.getInt("chat_counter", 0)

        val newCounter = currentCounter + 1

        val editor = sharedPreferences.edit()
        editor.putInt("chat_counter", newCounter)
        editor.apply()

        return newCounter
    }
}
