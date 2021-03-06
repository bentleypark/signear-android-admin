package com.sullivan.signearadmin.ui_login.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sullivan.common.ui_common.utils.SharedPreferenceManager
import com.sullivan.signearadmin.data.model.ResponseCheckEmail
import com.sullivan.signearadmin.data.model.ResponseLogin
import com.sullivan.signearadmin.domain.SignearRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject
constructor(
    private val repository: SignearRepository,
    private val sharedPreferenceManager: SharedPreferenceManager
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>(LoginState.Init)
    val loginState: LiveData<LoginState> = _loginState

    private val _resultCheckEmail = MutableLiveData<ResponseCheckEmail>()
    val resultCheckEmail: LiveData<ResponseCheckEmail> = _resultCheckEmail

    private val _resultLogin = MutableLiveData<ResponseLogin>()
    val resultLogin: LiveData<ResponseLogin> = _resultLogin

    private val _resultJoin = MutableLiveData<ResponseLogin>()
    val resultJoin: LiveData<ResponseLogin> = _resultJoin

    private val _errorMsg = MutableLiveData<String>()
    val errorMsg: LiveData<String> = _errorMsg

    fun checkEmail(email: String) {
        viewModelScope.launch {
            repository.checkEmail(email).collect { result ->
                _resultCheckEmail.value = result
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            repository.login(email, password)
                .catch { exception ->
                    _errorMsg.value = "로그인 실패: 비밀번호를 다시 입력해주세요!"
                    Timber.e(exception)
                }
                .collect { response ->
                    response.let {
                        with(sharedPreferenceManager) {
                            setAccessToken(response.accessToken)
                            setUserId(response.userProfile.id)
                        }
                        _resultLogin.value = response
                    }
                }
        }
    }

    fun createUser(email: String, password: String, center: String) {
        viewModelScope.launch {
            repository.createUser(email, password, center).collect { response ->
                response.let {
                    with(sharedPreferenceManager) {
                        setAccessToken(response.accessToken)
                        setUserId(response.userProfile.id)
                    }
                    _resultJoin.value = response
                }
            }
        }
    }

    fun updateLoginState(currentState: LoginState) {
        _loginState.value = currentState
    }

    fun checkCurrentState() = _loginState.value
}