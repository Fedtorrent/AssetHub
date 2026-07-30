package com.fulvio.assethub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fulvio.assethub.databinding.FragmentAddLinkBinding
import kotlinx.coroutines.launch

class AddLinkFragment : Fragment() {

    private var _binding: FragmentAddLinkBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VincoliViewModel by viewModels()
    
    private var selectedIconResId: Int = R.drawable.ic_globe
    private val availableIcons = listOf(
        R.drawable.ic_globe,
        R.drawable.ic_bank,
        R.drawable.ic_wallet,
        R.drawable.ic_list,
        R.drawable.ic_calculate,
        R.drawable.ic_dashboard_gauge,
        R.drawable.ic_utility,
        R.drawable.ic_settings
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddLinkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupIconSelector()
        
        binding.btnSaveLink.setOnClickListener {
            saveLink()
        }
    }

    private fun setupIconSelector() {
        val container = binding.containerLinkIcons
        container.removeAllViews()
        
        val size = 48.dpToPx()
        val margin = 8.dpToPx()
        
        availableIcons.forEach { iconRes ->
            val frame = FrameLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    setMargins(margin, 0, margin, 0)
                }
                
                val bg = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(if (iconRes == selectedIconResId) 0x33448AFF else 0x11FFFFFF)
                    setStroke(2.dpToPx(), if (iconRes == selectedIconResId) 0xFF448AFF.toInt() else 0x33FFFFFF.toInt())
                }
                background = bg
                
                val img = ImageView(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(24.dpToPx(), 24.dpToPx()).apply {
                        gravity = android.view.Gravity.CENTER
                    }
                    setImageResource(iconRes)
                    imageTintList = android.content.res.ColorStateList.valueOf(
                        if (iconRes == selectedIconResId) 0xFF448AFF.toInt() else 0xFFBBBBBB.toInt()
                    )
                }
                
                addView(img)
                setOnClickListener {
                    selectedIconResId = iconRes
                    setupIconSelector() // Refresh selection
                }
            }
            container.addView(frame)
        }
    }

    private fun saveLink() {
        val title = binding.editLinkTitle.text.toString().trim()
        val url = binding.editLinkUrl.text.toString().trim()
        val desc = binding.editLinkDesc.text.toString().trim()
        
        if (title.isBlank()) {
            binding.layoutLinkTitle.error = "Obbligatorio"
            return
        }
        if (url.isBlank()) {
            binding.layoutLinkUrl.error = "Inserisci l'indirizzo"
            return
        }
        
        // Verifica minima URL
        if (!url.startsWith("http")) {
            binding.layoutLinkUrl.error = "L'indirizzo deve iniziare con http:// o https://"
            return
        }

        val newLink = UsefulLink(
            title = title,
            description = desc,
            url = url,
            iconResId = selectedIconResId
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.insertUsefulLink(newLink)
            Toast.makeText(requireContext(), "Link aggiunto con successo!", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    private fun Int.dpToPx() = (this * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
