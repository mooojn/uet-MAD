package com.example.contactapp

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class ContactAdapter(
    private val onEditClick: (Contact) -> Unit,
    private val onDeleteClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    private val contacts = mutableListOf<Contact>()

    fun submitList(items: List<Contact>) {
        contacts.clear()
        contacts.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(contacts[position])
    }

    override fun getItemCount(): Int = contacts.size

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageProfile: ImageView = itemView.findViewById(R.id.imageProfile)
        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val textPhone: TextView = itemView.findViewById(R.id.textPhone)
        private val textSource: TextView = itemView.findViewById(R.id.textSource)
        private val buttonEdit: MaterialButton = itemView.findViewById(R.id.buttonEdit)
        private val buttonDelete: MaterialButton = itemView.findViewById(R.id.buttonDelete)

        fun bind(contact: Contact) {
            textName.text = contact.name
            textPhone.text = contact.phone
            textSource.visibility = if (contact.importedFromDevice) View.VISIBLE else View.GONE

            if (contact.photoUri.isNullOrBlank()) {
                imageProfile.setImageResource(R.drawable.ic_profile_placeholder)
            } else {
                imageProfile.setImageURI(Uri.parse(contact.photoUri))
            }

            buttonEdit.setOnClickListener { onEditClick(contact) }
            buttonDelete.setOnClickListener { onDeleteClick(contact) }
        }
    }
}
