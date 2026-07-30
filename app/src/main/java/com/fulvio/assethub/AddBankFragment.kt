package com.fulvio.assethub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fulvio.assethub.databinding.FragmentAddBankBinding
import kotlinx.coroutines.launch

class AddBankFragment : Fragment() {

    private var _binding: FragmentAddBankBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VincoliViewModel by viewModels()
    private var bankId: Long = -1L
    private var currentBank: Bank? = null
    private var selectedColor: Int = android.graphics.Color.BLUE

    private val colors = listOf(
        0xFF0F3ADA.toInt(), // Blu
        0xFF4CAF50.toInt(), // Verde
        0xFFFF0000.toInt(), // Rosso
        0xFFFFC107.toInt(), // Ambra
        0xFF9C27B0.toInt(), // Viola
        0xFF00BCD4.toInt(), // Ciano
        0xFFFF8141.toInt(), // Arancione
        0xFF607D8B.toInt(), // Grigio Blu
        0xFFE91E63.toInt(), // Rosa
        0xFF795548.toInt()  // Marrone
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBankBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bankId = arguments?.getLong("bankId") ?: -1L

        setupColorSelector()

        if (bankId != -1L) {
            viewLifecycleOwner.lifecycleScope.launch {
                val bank = viewModel.getBankById(bankId)
                bank?.let {
                    currentBank = it
                    binding.editBankName.setText(it.name)
                    selectedColor = it.color
                    updateColorSelector()
                    binding.btnSaveBank.text = "AGGIORNA BANCA"
                    (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = "Modifica Banca"
                }
            }
        }

        binding.btnSaveBank.setOnClickListener {
            saveBank()
        }
    }

    private fun setupColorSelector() {
        val container = binding.containerColors
        container.removeAllViews()
        
        val size = 40.dpToPx()
        val margin = 8.dpToPx()
        
        colors.forEach { color ->
            val view = android.widget.FrameLayout(requireContext()).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(size, size).apply {
                    setMargins(margin, 0, margin, 0)
                }
                
                val background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(color)
                }
                setBackground(background)
                
                val checkIcon = android.widget.ImageView(requireContext()).apply {
                    layoutParams = android.widget.FrameLayout.LayoutParams(size / 2, size / 2).apply {
                        gravity = android.view.Gravity.CENTER
                    }
                    setImageResource(R.drawable.ic_check_white)
                    visibility = if (color == selectedColor) View.VISIBLE else View.GONE
                    tag = "check"
                }
                
                addView(checkIcon)
                
                setOnClickListener {
                    selectedColor = color
                    updateColorSelector()
                }
                tag = color
            }
            container.addView(view)
        }
    }

    private fun updateColorSelector() {
        val container = binding.containerColors
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i) as android.widget.FrameLayout
            val checkIcon = child.findViewWithTag<android.view.View>("check")
            checkIcon.visibility = if ((child.tag as Int) == selectedColor) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun Int.dpToPx() = (this * resources.displayMetrics.density).toInt()

    private fun saveBank() {
        val name = binding.editBankName.text.toString()
        
        if (name.isBlank()) {
            binding.layoutBankName.error = "Obbligatorio"
            return
        }

        val bank = Bank(
            id = if (bankId != -1L) bankId else 0L,
            name = name,
            color = selectedColor,
            isDeleted = currentBank?.isDeleted ?: false
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (bankId != -1L) {
                    viewModel.updateBank(bank)
                } else {
                    viewModel.insertBank(bank)
                }
                Toast.makeText(requireContext(), "Banca salvata!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Errore: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
