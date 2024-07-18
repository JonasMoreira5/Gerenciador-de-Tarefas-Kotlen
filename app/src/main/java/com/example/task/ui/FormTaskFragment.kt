package com.example.task.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.example.task.R
import com.example.task.databinding.FragmentFormTaskBinding
import com.example.task.helper.FirebaseHelper
import com.example.task.model.Task
import com.google.android.material.radiobutton.MaterialRadioButton

class FormTaskFragment : Fragment() {

    private var _binding: FragmentFormTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var task: Task
    private var newTask: Boolean = true
    private var statusTask: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initListeners() {
        binding.btnSave.setOnClickListener { validateData() }
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            for (i in 0 until group.childCount) {
                val radioButton = group.getChildAt(i) as MaterialRadioButton
                radioButton.setTextColor(resources.getColor(R.color.white, null))
            }
            val selectedRadioButton = group.findViewById<MaterialRadioButton>(checkedId)
            selectedRadioButton.setTextColor(resources.getColor(R.color.selected_radio_button_color, null))
            statusTask = when (checkedId) {
                R.id.rbTodo -> 0
                R.id.rbDoing -> 1
                else -> 2
            }
        }
    }

    private fun setStatus() {
        binding.radioGroup.check(
            when (task.status) {
                0 -> R.id.rbTodo
                1 -> R.id.rbDoing
                else -> R.id.rbDone
            }
        )
    }

    private fun validateData() {
        val description = binding.editDescription.text.toString().trim()

        if (description.isNotEmpty()) {
            hideKeyboard()
            binding.progressBar.isVisible = true

            if (newTask) {
                task = Task()
            }
            task.description = description
            task.status = statusTask

            saveTask()
        } else {
            showBottomSheet(message = R.string.text_description_empty_form_task_fragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        initListeners()

        // Verifica se estamos editando uma tarefa existente
        val args = FormTaskFragmentArgs.fromBundle(requireArguments())
        if (args.task != null) {
            task = args.task!!
            newTask = false
            validateTask()
        } else {
            newTask = true
            task = Task() // Inicializa uma nova tarefa vazia
        }
    }


    private fun saveTask() {
        if (newTask) {
            task.id = FirebaseHelper.getDatabase()
                .child("task")
                .child(FirebaseHelper.getIdUser() ?: "")
                .push().key ?: ""

            FirebaseHelper.getDatabase()
                .child("task")
                .child(FirebaseHelper.getIdUser() ?: "")
                .child(task.id)
                .setValue(task)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        findNavController().popBackStack()
                        Toast.makeText(
                            requireContext(),
                            R.string.text_save_task_sucess_form_task_fragment,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        binding.progressBar.isVisible = false
                        Toast.makeText(
                            requireContext(),
                            R.string.text_erro_save_task_form_task_fragment,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }.addOnFailureListener {
                    binding.progressBar.isVisible = false
                    Toast.makeText(
                        requireContext(),
                        R.string.text_erro_save_task_form_task_fragment,
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            FirebaseHelper.getDatabase()
                .child("task")
                .child(FirebaseHelper.getIdUser() ?: "")
                .child(task.id ?: "")
                .setValue(task)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        binding.progressBar.isVisible = false
                        Toast.makeText(
                            requireContext(),
                            R.string.text_update_task_sucess_form_task_fragment,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        binding.progressBar.isVisible = false
                        Toast.makeText(
                            requireContext(),
                            R.string.text_erro_save_task_form_task_fragment,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }.addOnFailureListener {
                    binding.progressBar.isVisible = false
                    Toast.makeText(
                        requireContext(),
                        R.string.text_erro_save_task_form_task_fragment,
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun validateTask() {
        binding.textToolbar.text = getString(R.string.text_editing_task_form_task_fragment)
        binding.editDescription.setText(task.description)
        setStatus()
    }

    private fun hideKeyboard() {
        // Implement hide keyboard functionality
    }

    private fun showBottomSheet(message: Int) {
        // Implement show bottom sheet functionality
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
