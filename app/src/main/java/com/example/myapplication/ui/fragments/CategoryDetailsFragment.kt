package com.example.myapplication.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.NewsAdapter
import com.example.myapplication.ui.NewsActivity
import com.example.myapplication.ui.NewsViewModel
import com.example.myapplication.util.Resource
import com.example.newsprojectpractice.R
import com.example.newsprojectpractice.databinding.CategoryDetailsFragmentBinding

class CategoryDetailsFragment : Fragment(R.layout.category_details_fragment) {
    lateinit var newsViewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    lateinit var binding: CategoryDetailsFragmentBinding
    private val args: CategoryDetailsFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CategoryDetailsFragmentBinding.bind(view)
        newsViewModel = (activity as NewsActivity).newsViewModel
        setupRecyclerView()
        val displayName = args.displayName
        (activity as NewsActivity).supportActionBar?.title = displayName
        newsAdapter.setOnItemClickListener { article ->
            val bundle = Bundle().apply {
                putSerializable("article", article)
            }
            findNavController().navigate(
                R.id.action_categoryDetailsFragment_to_articleFragment,
                bundle
            )
        }

        val category = args.categoryName
        newsViewModel.getCategoryNews(category)

        newsViewModel.categoryNews.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    if (newsAdapter.differ.currentList.isEmpty()) {
                        binding.categoryProgressBar.visibility = View.VISIBLE
                    }
                }

                is Resource.Success -> {
                    binding.categoryProgressBar.visibility = View.INVISIBLE
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                    }
                }

                is Resource.Error -> {
                    binding.categoryProgressBar.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.recyclerCategoryDetails.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

}