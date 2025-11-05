package com.example.libraryapp.model

import android.net.Uri

data class Account(
    val id: String,
    val fullName: String,
    val avatarUri: String?,
    val gender: Gender,
    val role: Role,
    val email: String,
    val memberNumber: String,
    val joinDate: String
) {
    val avatar: Uri?
        get() = avatarUri?.let(Uri::parse)
}

enum class Gender(val displayName: String) {
    MALE("Laki-laki"),
    FEMALE("Perempuan")
}

enum class Role(val displayName: String) {
    AKADEMIK("Staf Akademik"),
    MAHASISWA("Mahasiswa"),
    DOSEN("Dosen")
}
