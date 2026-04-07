package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.models.CategoryModel
import com.example.newsprojectpractice.R

class CategoryAdapter(
    private  val category:List<CategoryModel>,
    private val onItemClick:(CategoryModel)->Unit
): RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>(){
    override fun onCreateViewHolder(
        p0: ViewGroup,
        p1: Int
    ): CategoryViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_category, p0, false)
        return CategoryViewHolder(view)    }

    override fun onBindViewHolder(
        p0: CategoryViewHolder,
        p1: Int
    ) {
       p0.bind(category[p1])    }

    override fun getItemCount() = category.size
    inner  class  CategoryViewHolder (view: View): RecyclerView.ViewHolder(view){
        val categoryImage: ImageView = view.findViewById(R.id.categoryImage)
        val categoryText: TextView = view.findViewById(R.id.categoryName)

        fun bind(category: CategoryModel) {
            categoryImage.setImageResource(category.imageResId)
            categoryText.text = category.displayName
            itemView.setOnClickListener { onItemClick(category) }
        }
    }

}