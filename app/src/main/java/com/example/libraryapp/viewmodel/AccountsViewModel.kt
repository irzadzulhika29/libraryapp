package com.example.libraryapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.libraryapp.model.Account
import com.example.libraryapp.model.Gender
import com.example.libraryapp.model.Role
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private val EMAIL_DOMAINS = listOf("@gmail.com", "@yahoo.com")

data class AccountFormState(
    val avatarUri: String? = null,
    val avatarError: String? = null,
    val firstName: String = "",
    val firstNameError: String? = null,
    val lastName: String = "",
    val lastNameError: String? = null,
    val gender: Gender? = null,
    val genderError: String? = null,
    val role: Role? = null,
    val roleError: String? = null,
    val emailLocalPart: String = "",
    val emailLocalPartError: String? = null,
    val emailDomain: String = EMAIL_DOMAINS.first(),
    val memberNumber: String = "",
    val memberNumberError: String? = null,
    val joinDate: String = "",
    val joinDateError: String? = null
) {
    val isValid: Boolean
        get() = listOf(
            avatarError,
            firstNameError,
            lastNameError,
            genderError,
            roleError,
            emailLocalPartError,
            memberNumberError,
            joinDateError
        ).all { it == null }
}

class AccountsViewModel : ViewModel() {

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    private val _formState = MutableStateFlow(AccountFormState())
    val formState: StateFlow<AccountFormState> = _formState.asStateFlow()

    fun onAvatarChange(uri: String?) {
        _formState.update { state ->
            val error = if (uri.isNullOrEmpty()) "Foto profil wajib dipilih" else null
            state.copy(avatarUri = uri, avatarError = error)
        }
    }

    fun onFirstNameChange(value: String) {
        _formState.update { state ->
            state.copy(
                firstName = value,
                firstNameError = validateNonEmpty(value, "Nama depan wajib diisi")
            )
        }
    }

    fun onLastNameChange(value: String) {
        _formState.update { state ->
            state.copy(
                lastName = value,
                lastNameError = validateNonEmpty(value, "Nama belakang wajib diisi")
            )
        }
    }

    fun onGenderChange(value: Gender) {
        _formState.update { state ->
            state.copy(gender = value, genderError = null)
        }
    }

    fun onRoleChange(value: Role) {
        _formState.update { state ->
            state.copy(role = value, roleError = null)
        }
    }

    fun onEmailLocalPartChange(value: String) {
        _formState.update { state ->
            state.copy(
                emailLocalPart = value,
                emailLocalPartError = validateEmailLocal(value)
            )
        }
    }

    fun onEmailDomainChange(value: String) {
        _formState.update { state ->
            state.copy(emailDomain = value)
        }
    }

    fun onMemberNumberChange(value: String) {
        _formState.update { state ->
            state.copy(
                memberNumber = value,
                memberNumberError = validateMemberNumber(value)
            )
        }
    }

    fun onJoinDateChange(value: String) {
        _formState.update { state ->
            state.copy(
                joinDate = value,
                joinDateError = validateNonEmpty(value, "Tanggal bergabung wajib diisi")
            )
        }
    }

    fun submitAccount(): Boolean {
        val validated = validateCurrentState(_formState.value)
        _formState.value = validated
        if (!validated.isValid) return false

        val fullName = buildString {
            append(validated.firstName.trim())
            append(' ')
            append(validated.lastName.trim())
        }
        val email = validated.emailLocalPart.trim() + validated.emailDomain

        val account = Account(
            id = UUID.randomUUID().toString(),
            fullName = fullName,
            avatarUri = validated.avatarUri,
            gender = validated.gender!!,
            role = validated.role!!,
            email = email,
            memberNumber = validated.memberNumber.trim(),
            joinDate = validated.joinDate.trim()
        )

        _accounts.update { it + account }
        _formState.value = AccountFormState()
        return true
    }

    fun getAccount(accountId: String): Account? = _accounts.value.firstOrNull { it.id == accountId }

    fun availableDomains(): List<String> = EMAIL_DOMAINS

    private fun validateCurrentState(state: AccountFormState): AccountFormState {
        return state.copy(
            avatarError = if (state.avatarUri.isNullOrEmpty()) "Foto profil wajib dipilih" else null,
            firstNameError = validateNonEmpty(state.firstName, "Nama depan wajib diisi"),
            lastNameError = validateNonEmpty(state.lastName, "Nama belakang wajib diisi"),
            genderError = if (state.gender == null) "Pilih jenis kelamin" else null,
            roleError = if (state.role == null) "Pilih jabatan" else null,
            emailLocalPartError = validateEmailLocal(state.emailLocalPart),
            memberNumberError = validateMemberNumber(state.memberNumber),
            joinDateError = validateNonEmpty(state.joinDate, "Tanggal bergabung wajib diisi")
        )
    }

    private fun validateNonEmpty(value: String, message: String): String? {
        return if (value.isBlank()) message else null
    }

    private fun validateEmailLocal(value: String): String? {
        if (value.isBlank()) {
            return "Email wajib diisi"
        }
        val regex = "^[A-Za-z0-9._%+-]+$".toRegex()
        return if (!regex.matches(value)) "Format email tidak valid" else null
    }

    private fun validateMemberNumber(value: String): String? {
        if (value.isBlank()) {
            return "Nomor anggota wajib diisi"
        }
        val regex = "^\\d{4,}$".toRegex()
        return if (!regex.matches(value)) "Nomor anggota minimal 4 digit angka" else null
    }
}
