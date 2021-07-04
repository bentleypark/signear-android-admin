package com.sullivan.signearadmin.ui_login.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sullivan.common.ui_common.base.BaseFragment
import com.sullivan.common.ui_common.ex.*
import com.sullivan.common.ui_common.navigator.ReservationNavigator
import com.sullivan.signearadmin.ui_login.R
import com.sullivan.signearadmin.ui_login.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import timber.log.Timber
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {

    private val viewModel: LoginViewModel by activityViewModels()

    private val validEmailRegex =
        Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE)
    private lateinit var centerArray: Array<String>

    @Inject
    lateinit var reservationNavigator: ReservationNavigator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObserve()
        setTextWatcher()
    }

    override fun setupView() {
        binding.apply {
            loginLayout.apply {
                btnNext.setOnClickListener {
                    when (viewModel.checkCurrentState()) {
                        is LoginState.Init -> {
                            val email = etEmailInput.text.toString().trim()
                            if (email.isNotEmpty()) {
                                viewModel.checkEmail(email)
                            }
                        }
                        is LoginState.EmailValid -> {
                            val email = etEmailInput.text.toString().trim()
                            val password = etPasswordInput.text.toString().trim()
                            if (password.isNotEmpty()) {
                                etPasswordInput.apply {
                                    clearFocus()
                                    hideKeyboard()
                                }
                            }
                            viewModel.login(email, password)
                        }
                    }
                }

                btnBack.setOnClickListener {
                    when (viewModel.checkCurrentState()) {
                        is LoginState.Init -> {
                            findNavController().navigate(R.id.action_loginFragment_to_loginStartFragment)
                        }
                        is LoginState.JoinMember -> {
                            showLoginView()
                        }
                        is LoginState.FindAccount -> {
                            showLoginView()
                        }
                        is LoginState.EmailValid -> {
                            showLoginView()
                        }
                    }
                }

                btnJoin.setOnClickListener {
                    val email = etEmailInput.text.toString().trim()
                    val password = etPasswordInput.text.toString().trim()
                    val center = etCenterInput.text.toString()
                    if (email.isNotEmpty() && password.isNotEmpty() && center.isNotEmpty()) {
                        etCenterInput.apply {
                            clearFocus()
                            hideKeyboard()
                        }
                        viewModel.createUser(email, password, center)
                    }
                }

                btnFindAccount.setOnClickListener {
//                    viewModel.updateLoginState(LoginState.FindAccount)
                    requireContext().showDialog(
                        getString(R.string.dialog_title),
                        getString(R.string.future_develop),
                        getString(R.string.future_develop_positive_btn_title)
                    )
                }
            }

            findAccountLayout.apply {
                btnNext.setOnClickListener {
                    showNewPasswordInputRequestView()
                }

                btnLogin.setOnClickListener {
                    showLoginView()
                }
            }
        }
    }

    private fun setupObserve() {
        viewModel.apply {
            loginState.observe(viewLifecycleOwner, { loginState ->
                run {
                    when (loginState) {
                        is LoginState.EmailValid -> {
                            showPasswordInputView()
                        }
                        is LoginState.FindAccount -> {
                            showFindAccountView()
                        }
                        is LoginState.Success -> {
                            moveToMainScreen()
                        }
                        is LoginState.JoinMember -> {
                            showMemberJoinView()
                        }
                        is LoginState.JoinSuccess -> {
                            findNavController().navigate(R.id.action_loginFragment_to_loginFinishFragment)
                        }
                    }
                }
            })

            resultCheckEmail.observe(viewLifecycleOwner, { response ->
                if (response.result) {
                    if (viewModel.checkCurrentState() == LoginState.Init) {
                        viewModel.updateLoginState(LoginState.EmailValid)
                    } else {
                        showNewPasswordInputRequestView()
                    }
                } else {
                    if (viewModel.checkCurrentState() == LoginState.Init) {
                        viewModel.updateLoginState(LoginState.JoinMember)
                    } else {
                        makeToast("입력하신 이메일의 계정이 존재하지 않습니다!")
                    }
                }
            })

            resultLogin.observe(viewLifecycleOwner, { response ->
                if (response.accessToken.isNotEmpty()) {
                    viewModel.updateLoginState(LoginState.Success)
                } else {
                    makeToast("로그인 실패: 비밀번호를 다시 입력해주세요!")
                }
            })

            resultJoin.observe(viewLifecycleOwner, { response ->
                if (response.accessToken.isNotEmpty()) {
                    viewModel.updateLoginState(LoginState.JoinSuccess)
                } else {
                    makeToast("회원 가입 실패!")
                }
            })

            errorMsg.observe(viewLifecycleOwner, { msg ->
                makeToast(msg)
            })

//            selectedCenter.observe(viewLifecycleOwner, { center ->
//                with(binding.loginLayout) {
//                    etCenterInput.makeGone()
//                    tvCenterInput.makeVisible()
//                    tvCenterInput.text = center
//                    tvRule.setMargin(0,60,0,0)
//                }
//            })
        }
    }

    private fun setTextWatcher() {
        var email: String
        var password: String
        var center: String

        binding.loginLayout.etEmailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                binding.loginLayout.apply {
                    email = etEmailInput.text.toString().trim()
                    if (email.isNotEmpty() && checkEmailValidation(email)) {
                        ViewCompat.setBackgroundTintList(
                            binding.loginLayout.btnNext,
                            ContextCompat.getColorStateList(
                                requireContext(),
                                R.color.black
                            )
                        )
                    } else {
                        ViewCompat.setBackgroundTintList(
                            binding.loginLayout.btnNext,
                            ContextCompat.getColorStateList(
                                requireContext(),
                                R.color.btn_next_disable
                            )
                        )
                    }
                }
            }
        })

        binding.findAccountLayout.etEmailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                binding.findAccountLayout.apply {
                    email = etEmailInput.text.toString().trim()
                    if (email.isNotEmpty() && checkEmailValidation(email)) {
                        makeBtnEnable(btnNext)
                    } else {
                        makeBtnDisable(btnNext)
                    }
                }
            }
        })

//        binding.loginLayout.etCenterInput.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(
//                s: CharSequence?,
//                start: Int,
//                count: Int,
//                after: Int
//            ) {
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//                binding.loginLayout.apply {
//                    email = etEmailInput.text.toString().trim()
//                    password = etPasswordInput.text.toString().trim()
//                    center = etCenterInput.text.toString().trim()
//                    Timber.d("center: $center")
//                    if (center.isNotEmpty() && password.isNotEmpty() && email.isNotEmpty()) {
//                        makeBtnEnable(btnJoin)
//                    } else {
//                        makeBtnDisable(btnJoin)
//                    }
//                }
//            }
//        })

        binding.loginLayout.etPasswordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                binding.loginLayout.apply {
                    email = etEmailInput.text.toString().trim()
                    password = etPasswordInput.text.toString().trim()
                    center = etCenterInput.text.toString().trim()
                    if (password.isNotEmpty() && email.isNotEmpty() && center.isNotEmpty()) {
                        makeBtnEnable(btnJoin)
                    } else {
                        makeBtnDisable(btnJoin)
                    }
                }
            }
        })
    }

    private fun moveToMainScreen() {
        lifecycleScope.launchWhenCreated {
            delay(1_000)
            reservationNavigator.openRealTimeReservationHome(requireActivity())
        }
    }

    private fun checkEmailValidation(input: String) = validEmailRegex.matcher(input).matches()

    private fun updateTitle(title: String) {
        binding.tvTitle.text = title
    }

    private fun showMemberJoinView() {
        binding.loginLayout.apply {
            updateTitle("회원가입")
            ivPassword.makeVisible()
            etEmailInput.apply {
                hideKeyboard()
                clearFocus()
            }
            etPasswordInput.makeVisible()
            ivCenter.makeVisible()
            etCenterInput.makeVisible()
            btnNext.makeGone()
            btnFindAccount.makeGone()
            tvRule.makeVisible()
            btnJoin.makeVisible()

            centerArray = resources.getStringArray(R.array.center_array)

            with(etCenterInput) {
                setItems(centerArray.toList())
                setOnItemSelectedListener { _, _, _, item ->
                }
            }
        }
    }

    private fun showPasswordInputView() {
        binding.loginLayout.apply {
            etEmailInput.clearFocus()
            etPasswordInput.apply {
                requestFocus()
                showKeyboard()
            }
            ivPassword.makeVisible()
            etPasswordInput.makeVisible()
        }
    }

    private fun showFindAccountView() {
        binding.apply {
            loginLayout.loginLayout.apply {
                makeGone()
                hideKeyboard()
            }

            findAccountLayout.apply {
                updateTitle("계정 찾기")
                findAccountLayout.makeVisible()
            }
        }
    }

    private fun showNewPasswordInputRequestView() {
        binding.findAccountLayout.apply {
            tvFindAccountGuideMsg.makeGone()
            ivHuman.makeGone()
            etEmailInput.apply {
                clearFocus()
                hideKeyboard()
                makeGone()
            }
            btnNext.makeGone()

            ivVoyage.makeVisible()
            guideMsg.makeVisible()
            guideMsg2.makeVisible()
            btnLogin.makeVisible()
        }
    }

    private fun showLoginView() {
        viewModel.updateLoginState(LoginState.Init)
        binding.loginLayout.apply {
            loginLayout.makeVisible()
            etEmailInput.apply {
                text = null
                hideKeyboard()
            }
            btnNext.makeVisible()
            btnFindAccount.makeVisible()

            etPasswordInput.apply {
                text = null
                makeGone()
            }
            ivPassword.makeGone()
            ivCenter.makeGone()
            etCenterInput.apply {
                text = null
                makeGone()
            }
            tvRule.makeGone()
            btnJoin.makeGone()

        }
        binding.findAccountLayout.apply {
            tvFindAccountGuideMsg.makeVisible()
            ivHuman.makeVisible()
            etEmailInput.apply {
                makeVisible()
                text = null
            }
            btnNext.makeVisible()

            ivVoyage.makeGone()
            guideMsg.makeGone()
            guideMsg2.makeGone()
            btnLogin.makeGone()

            findAccountLayout.makeGone()
            updateTitle("로그인")
        }
    }

    private fun makeBtnEnable(btn: Button) {
        ViewCompat.setBackgroundTintList(
            btn,
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.black
            )
        )
        btn.makeEnable()
    }

    private fun makeBtnDisable(btn: Button) {
        ViewCompat.setBackgroundTintList(
            btn,
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.btn_next_disable
            )
        )
        btn.makeDisable()
    }

    companion object {
        private const val ARGS_KEY = "center"
    }
}