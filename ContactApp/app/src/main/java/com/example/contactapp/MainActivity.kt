package com.example.contactapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.button.MaterialButton
import java.util.concurrent.atomic.AtomicLong

class MainActivity : AppCompatActivity() {

    private val contacts = mutableListOf<Contact>()
    private val idGenerator = AtomicLong(1L)

    private lateinit var recyclerView: RecyclerView
    private lateinit var textEmpty: TextView
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var inputSearch: EditText
    private lateinit var inputFilterBy: MaterialAutoCompleteTextView
    private lateinit var inputSortOrder: MaterialAutoCompleteTextView

    private var searchQuery: String = ""
    private var filterBy: FilterBy = FilterBy.ALL
    private var sortOrder: SortOrder = SortOrder.ASC

    private var pendingPhotoUri: String? = null
    private var pendingImagePreview: ImageView? = null

    private val pickPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                contentResolver.takePersistableUriPermissionSafe(uri)
                pendingPhotoUri = uri.toString()
                pendingImagePreview?.setImageURI(uri)
            }
        }

    private val contactsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                importContactsFromDevice()
            } else {
                showToast(getString(R.string.permission_required))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerContacts)
        textEmpty = findViewById(R.id.textEmpty)
        inputSearch = findViewById(R.id.inputSearch)
        inputFilterBy = findViewById(R.id.inputFilterBy)
        inputSortOrder = findViewById(R.id.inputSortOrder)

        contactAdapter = ContactAdapter(
            onEditClick = { contact -> showContactDialog(existingContact = contact) },
            onDeleteClick = { contact -> deleteContact(contact.id) }
        )

        recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = contactAdapter
            setHasFixedSize(true)
        }

        setupSearchFilterAndSort()

        findViewById<MaterialButton>(R.id.buttonAddContact).setOnClickListener {
            showContactDialog(existingContact = null)
        }

        findViewById<MaterialButton>(R.id.buttonImportContacts).setOnClickListener {
            requestAndImportContacts()
        }

        renderContacts()
    }

    private fun setupSearchFilterAndSort() {
        val filterOptions = listOf(
            getString(R.string.filter_all),
            getString(R.string.filter_name),
            getString(R.string.filter_phone)
        )
        inputFilterBy.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, filterOptions)
        )
        inputFilterBy.setText(filterOptions.first(), false)

        val sortOptions = listOf(getString(R.string.sort_asc), getString(R.string.sort_desc))
        inputSortOrder.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, sortOptions)
        )
        inputSortOrder.setText(sortOptions.first(), false)

        inputSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim().orEmpty()
                renderContacts()
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        inputFilterBy.setOnItemClickListener { _, _, position, _ ->
            filterBy = when (position) {
                1 -> FilterBy.NAME
                2 -> FilterBy.PHONE
                else -> FilterBy.ALL
            }
            renderContacts()
        }

        inputSortOrder.setOnItemClickListener { _, _, position, _ ->
            sortOrder = if (position == 1) SortOrder.DESC else SortOrder.ASC
            renderContacts()
        }
    }

    private fun showContactDialog(existingContact: Contact?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_contact, null)
        val inputName = dialogView.findViewById<EditText>(R.id.inputName)
        val inputPhone = dialogView.findViewById<EditText>(R.id.inputPhone)
        val imagePreview = dialogView.findViewById<ImageView>(R.id.imagePreview)
        val buttonPickPhoto = dialogView.findViewById<MaterialButton>(R.id.buttonPickPhoto)

        pendingImagePreview = imagePreview

        if (existingContact == null) {
            pendingPhotoUri = null
            imagePreview.setImageResource(R.drawable.ic_profile_placeholder)
        } else {
            inputName.setText(existingContact.name)
            inputPhone.setText(existingContact.phone)
            pendingPhotoUri = existingContact.photoUri
            if (existingContact.photoUri.isNullOrBlank()) {
                imagePreview.setImageResource(R.drawable.ic_profile_placeholder)
            } else {
                imagePreview.setImageURI(android.net.Uri.parse(existingContact.photoUri))
            }
        }

        buttonPickPhoto.setOnClickListener {
            pickPhotoLauncher.launch("image/*")
        }

        val titleRes = if (existingContact == null) {
            R.string.add_new_contact
        } else {
            R.string.edit_contact
        }

        AlertDialog.Builder(this)
            .setTitle(titleRes)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = inputName.text?.toString()?.trim().orEmpty()
                val phone = inputPhone.text?.toString()?.trim().orEmpty()

                if (name.isBlank() || phone.isBlank()) {
                    showToast(getString(R.string.empty_name_phone))
                    return@setPositiveButton
                }

                if (existingContact == null) {
                    addContact(name = name, phone = phone, photoUri = pendingPhotoUri)
                } else {
                    updateContact(
                        id = existingContact.id,
                        name = name,
                        phone = phone,
                        photoUri = pendingPhotoUri
                    )
                }
                pendingImagePreview = null
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                pendingImagePreview = null
                dialog.dismiss()
            }
            .show()
    }

    private fun addContact(name: String, phone: String, photoUri: String?) {
        val newContact = Contact(
            id = idGenerator.getAndIncrement(),
            name = name,
            phone = phone,
            photoUri = photoUri,
            importedFromDevice = false
        )
        contacts.add(0, newContact)
        renderContacts()
        showToast(getString(R.string.contact_saved))
    }

    private fun updateContact(id: Long, name: String, phone: String, photoUri: String?) {
        val target = contacts.firstOrNull { it.id == id } ?: return
        target.name = name
        target.phone = phone
        target.photoUri = photoUri
        renderContacts()
        showToast(getString(R.string.contact_saved))
    }

    private fun deleteContact(id: Long) {
        contacts.removeAll { it.id == id }
        renderContacts()
        showToast(getString(R.string.contact_deleted))
    }

    private fun requestAndImportContacts() {
        val isGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            importContactsFromDevice()
        } else {
            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun importContactsFromDevice() {
        val resolver = contentResolver
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )

        val imported = mutableListOf<Contact>()

        resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            while (cursor.moveToNext()) {
                val name = if (nameIndex >= 0) cursor.getString(nameIndex) else ""
                val phone = if (numberIndex >= 0) cursor.getString(numberIndex) else ""
                val photoUri = if (photoIndex >= 0) cursor.getString(photoIndex) else null

                if (name.isBlank() || phone.isBlank()) continue

                imported.add(
                    Contact(
                        id = idGenerator.getAndIncrement(),
                        name = name,
                        phone = phone,
                        photoUri = photoUri,
                        importedFromDevice = true
                    )
                )
            }
        }

        val existingKeys = contacts.associateBy { "${it.name.lowercase()}|${it.phone}" }
        val deduped = imported.filterNot { existingKeys.containsKey("${it.name.lowercase()}|${it.phone}") }

        contacts.addAll(0, deduped)
        renderContacts()
        showToast(getString(R.string.contacts_imported))
    }

    private fun renderContacts() {
        val filtered = contacts
            .asSequence()
            .filter { contact ->
                if (searchQuery.isBlank()) return@filter true

                val query = searchQuery.lowercase()
                val name = contact.name.lowercase()
                val phone = contact.phone.lowercase()

                when (filterBy) {
                    FilterBy.ALL -> name.contains(query) || phone.contains(query)
                    FilterBy.NAME -> name.contains(query)
                    FilterBy.PHONE -> phone.contains(query)
                }
            }
            .sortedWith(
                compareBy<Contact> { it.name.lowercase() }
                    .thenBy { it.phone.lowercase() }
            )
            .let { sequence ->
                if (sortOrder == SortOrder.DESC) sequence.toList().asReversed() else sequence.toList()
            }

        contactAdapter.submitList(filtered)
        textEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun android.content.ContentResolver.takePersistableUriPermissionSafe(uri: android.net.Uri) {
        val mode = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        try {
            takePersistableUriPermission(uri, mode)
        } catch (_: SecurityException) {
            // Some providers do not support persisted permissions.
        }
    }

    private enum class FilterBy {
        ALL,
        NAME,
        PHONE
    }

    private enum class SortOrder {
        ASC,
        DESC
    }

}
