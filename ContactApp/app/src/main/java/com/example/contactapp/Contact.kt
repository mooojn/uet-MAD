package com.example.contactapp

data class Contact(
    val id: Long,
    var name: String,
    var phone: String,
    var photoUri: String? = null,
    var importedFromDevice: Boolean = false
)
