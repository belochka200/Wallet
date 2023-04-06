package com.example.wallet.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionManager
import com.example.wallet.R
import com.example.wallet.data.models.SettingsIds
import com.example.wallet.data.models.SettingsIds.USER_EMAIL
import com.example.wallet.data.models.SettingsIds.USER_FINGERPRINT
import com.example.wallet.data.models.SettingsIds.USER_NAME
import com.example.wallet.data.models.SettingsIds.USER_PIN
import com.example.wallet.databinding.FragmentSettingsScreenBinding
import com.example.wallet.ui.uistate.SettingsScreenUiState
import com.example.wallet.ui.viewmodels.SettingsScreenViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFade
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.launch

class Settings : Fragment(R.layout.fragment__settings_screen) {
    private val settingsScreenViewModel: SettingsScreenViewModel by viewModels { SettingsScreenViewModel.Factory }

    private val currentUserSettings: MutableMap<SettingsIds, Any> = mutableMapOf()
    private var settingsChangeFlag: Boolean = false
    private var _binding: FragmentSettingsScreenBinding? = null
    private val binding get() = checkNotNull(_binding)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)

        // Проверка изменения настроек при выходе с помощью системной кнопки
        val backPressedCallback =
            requireActivity().onBackPressedDispatcher.addCallback(this) {
                checkUserSettings()
            }
        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsScreenViewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is SettingsScreenUiState.Content -> showContentUi(
                            name = uiState.userName,
                            email = uiState.userEmail,
                            pin = uiState.pinCodeToLogin,
                            useFingerprintToLogin = uiState.fingerprintLogin
                        )

                        SettingsScreenUiState.Error -> showFailedUi()
                        SettingsScreenUiState.Loading -> showLoadingUi()
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            toolbarSettings.setNavigationOnClickListener { checkUserSettings() }
        }
    }

    private fun showContentUi(
        name: String,
        email: String,
        pin: String,
        useFingerprintToLogin: Boolean
    ) {
        hideLoading()
        // todo перенести логику во viewmodel
        with(currentUserSettings) {
            set(USER_NAME, name)
            set(USER_FINGERPRINT, useFingerprintToLogin)
            set(USER_EMAIL, email)
            set(USER_PIN, pin)
        }
        binding.apply {
            editTextUserName.setText(name)
            editTextUserEmail.setText(email)
            switchUseFingerPrintToLogin.isChecked = useFingerprintToLogin
            toolbarSettings.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_item__save_settings -> {
                        checkUserSettings()
                        true
                    }

                    else -> false
                }
            }
            buttonActionToChangePinCode.setOnClickListener {
                findNavController().navigate(R.id.action_settings_to_changePin)
            }
//            initWorkWithPinCode()
        }
    }

    private fun checkPinCodes() {
        binding.apply {
            textInputLayoutSettingsCurrentPinCode.error = null
            textInputLayoutSettingsNewPinCode.error = null
//            when {
//                editTextSettingsCurrentPinCode.text.toString() != currentUserSettings[USER_PIN].toString() ->
//                    textInputLayoutSettingsCurrentPinCode.error =
//                        getString(R.string.incorrect_pin_code)
//
//                editTextSettingsNewPinCode.text.toString().length !in 4..8 ->
//                    textInputLayoutSettingsNewPinCode.error =
//                        textInputLayoutSettingsNewPinCode.helperText
//
//                editTextSettingsCurrentPinCode.text.toString() == editTextSettingsNewPinCode.text.toString() -> {
//                    textInputLayoutSettingsNewPinCode.error = getString(R.string.pin_codes_equals)
//                    textInputLayoutSettingsCurrentPinCode.error =
//                        getString(R.string.pin_codes_equals)
//                }
//
//                else -> {
//                    try {
//                        editTextSettingsNewPinCode.text.toString().toInt()
//                        cardChangePinCode.isVisible = false
//                        Toast.makeText(
//                            requireContext(),
//                            R.string.pin_code_has_been_save,
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    } catch (e: Exception) {
//                        textInputLayoutSettingsNewPinCode.error =
//                            getString(R.string.only_number_pin_code)
//                    }
//                }
//            }
        }
    }

    // todo перенести на новый фрагмент
    private fun initWorkWithPinCode() {
//        val materialFadeIn = MaterialFade().apply {
//            duration = 150L
//        }
//        val materialFadeOut = MaterialFade().apply {
//            duration = 84L
//        }
//        binding.apply {
//            buttonActionToChangePinCode.setOnClickListener {
//                TransitionManager.beginDelayedTransition(scrollViewSettings, materialFadeIn)
//                cardChangePinCode.isVisible = true
//                TransitionManager.beginDelayedTransition(scrollViewSettings, materialFadeOut)
//                buttonActionToChangePinCode.isVisible = false
//            }
//            buttonActionSavePinCode.setOnClickListener { checkPinCodes() }
//            buttonActionCancelChangePinCode.setOnClickListener {
//                TransitionManager.beginDelayedTransition(scrollViewSettings, materialFadeOut)
//                cardChangePinCode.isVisible = false
//                TransitionManager.beginDelayedTransition(scrollViewSettings, materialFadeIn)
//                buttonActionToChangePinCode.isVisible = true
//            }
//        }
    }

    private fun showFailedUi() {
        Snackbar.make(
            requireView(),
            R.string.error_message,
            Snackbar.LENGTH_SHORT
        ).show()
        hideLoading()
    }

    private fun showLoadingUi() {
        showLoading()
    }

    private fun showLoading() {
        binding.linearProgressIndicatorSettings.isVisible = true
    }

    private fun hideLoading() {
        binding.linearProgressIndicatorSettings.isVisible = false
    }

    private fun checkUserSettings() {
        binding.apply {
            textInputLayoutUserName.error = null
            textInputLayoutUserEmail.error = null
//            when {
//                editTextUserName.text.toString().isEmpty() -> {
//                    textInputLayoutUserName.error = textInputLayoutUserName.helperText
//                    return
//                }
//
//                editTextUserEmail.text.toString().isEmpty() -> {
//                    textInputLayoutUserEmail.error = textInputLayoutUserEmail.helperText
//                    return
//                }
//
//                // fingerprint settings
//                currentUserSettings[USE_FINGERPRINT_TO_LOGIN] != switchUseFingerPrintToLogin.isChecked -> {
////                    settingsScreenViewModel.changeSettings(
////                        USE_FINGERPRINT_TO_LOGIN,
////                        switchUseFingerPrintToLogin.isChecked.toString()
////                    )
//                    settingsChangeFlag = true
//                }
//
//                // name settings
//                currentUserSettings[USER_NAME] != editTextUserName.text.toString() -> {
////                    settingsScreenViewModel.updateSettings(editTextUserName.text.toString())
//
//                    settingsChangeFlag = true
//                }
//
//                // email settingsa
//                currentUserSettings[USER_EMAIL] != editTextUserEmail.text.toString() -> {
////                    settingsScreenViewModel.changeSettings(
////                        USER_EMAIL,
////                        editTextUserEmail.text.toString()
////                    )
//                    settingsChangeFlag = true
//                }
//            }
            if (settingsChangeFlag) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.settings_have_been_saved),
                    Toast.LENGTH_SHORT
                ).show()
            }
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}