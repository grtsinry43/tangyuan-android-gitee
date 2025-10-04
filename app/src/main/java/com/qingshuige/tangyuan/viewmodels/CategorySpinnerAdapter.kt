package com.qingshuige.tangyuan.viewmodels

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.qingshuige.tangyuan.R
import com.qingshuige.tangyuan.network.Category

class CategorySpinnerAdapter(
    context: Context,
    resource: Int,
    private val items: List<Category>
) : ArrayAdapter<Category>(context, resource, items) {

    private val inflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return super.getView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createCustomView(position, convertView, parent)
    }

    private fun createCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.view_spinner_category, parent, false)
        
        val nameTextView = view.findViewById<TextView>(R.id.textCategoryName)
        val descTextView = view.findViewById<TextView>(R.id.textCategoryDisc)
        
        val category = items[position]
        nameTextView.text = category.baseName
        descTextView.text = category.baseDescription
        
        return view
    }
}