package com.example.myapplication.ui.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.BannerAdapter
import com.example.myapplication.adapter.CategoryAdapter
import com.example.myapplication.adapter.NewsAdapter
import com.example.myapplication.models.Article
import com.example.myapplication.models.CategoryModel
import com.example.myapplication.ui.NewsActivity
import com.example.myapplication.ui.NewsViewModel
import com.example.myapplication.util.Constants
import com.example.myapplication.util.Resource
import com.example.newsprojectpractice.R
import com.example.newsprojectpractice.databinding.FragmentHeadLinesBinding


class HeadLinesFragment : Fragment(R.layout.fragment_head_lines) {
    lateinit var newsViewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    lateinit var binding: FragmentHeadLinesBinding
    lateinit var retryButton: Button
    lateinit var errorText: TextView
    lateinit var itemHeadLinesError: CardView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHeadLinesBinding.bind(view)

        itemHeadLinesError = view.findViewById(R.id.itemHeadlinesError)
        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_error, null)
        retryButton = view.findViewById(R.id.retryButton)
        errorText = view.findViewById(R.id.errorText)

        newsViewModel = (activity as NewsActivity).newsViewModel
        val sharedPref = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val savedQuery = sharedPref.getString("query", "news") ?: "news"

        if (savedQuery != "news") {
            newsViewModel.getNewsByLanguage(savedQuery)
        }
        setupHeadLineRecycler()

        newsViewModel.languageNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressbar()
                    hideErrorMessage()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                    }
                }
                is Resource.Error -> {
                    hideProgressbar()
                    response.message?.let { message ->
                        Toast.makeText(activity, "Error: $message", Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading -> {
                    showProgressbar()
                }
            }
        })

        newsAdapter.setOnItemClickListener {
            val safeArticle = it.copy(
                source = it.source?.copy(
                    id = it.source.id ?: "",
                    name = it.source.name ?: ""
                )
            )

            val bundle = Bundle().apply {
                putSerializable("article", safeArticle)
            }

            findNavController().navigate(
                R.id.action_headLinesFragment_to_articleFragment,
                bundle
            )
        }
        newsViewModel.headlines.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressbar()
                    hideErrorMessage()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = newsViewModel.headLinesPage == totalPages
                        if (!isLastPage) {
                            binding.recyclerHeadlines.setPadding(0, 0, 0, 0)
                        }
                    }
                }

                is Resource.Error<*> -> {
                    hideProgressbar()
                    response.message?.let { message ->
                        Toast.makeText(activity, "Error: $message", Toast.LENGTH_LONG).show()
                        showErrorMessage(message)
                    }
                }

                is Resource.Loading -> {
                    showProgressbar()
                }


            }


        })
        retryButton.setOnClickListener {
            newsViewModel.getHeadLines(newsViewModel.currentCountry, newsViewModel.currentCategory)
        }
        newsAdapter.setOnItemClickListener { article ->
            val action =
                HeadLinesFragmentDirections.actionHeadLinesFragmentToArticleFragment(article)
            findNavController().navigate(action)
        }
        //////////////////
        val categoryList = listOf(
            CategoryModel("business", "Business", R.drawable.busniess ),
            CategoryModel("entertainment", "Entertainment", R.drawable.entertainment),
            CategoryModel("health", "Health", R.drawable.health),
            CategoryModel("science", "Science", R.drawable.science),
            CategoryModel("sports", "Sports", R.drawable.sport ),
            CategoryModel("technology", "Tech", R.drawable.tech)
        )
        val categoryAdapter = CategoryAdapter(categoryList) { category ->
            val bundle = Bundle().apply {
                putString("categoryName", category.categoryName)
                putString("displayName", category.displayName)
            }
            val action = HeadLinesFragmentDirections
                .actionHeadLinesFragmentToCategoryDetailsFragment(
                    category.categoryName,
                    category.displayName
                )
            findNavController().navigate(action)
        }
        binding.recyclerCategories.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        }
        ///////////////////////////

        val images = listOf(R.drawable.banner1, R.drawable.banner2, R.drawable.banner3)

        val bannerAdapter = BannerAdapter(images)
        binding.viewPagerBanner.adapter = bannerAdapter

        binding.indicator.setViewPager(binding.viewPagerBanner)

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (binding.viewPagerBanner != null) {
                    // 1. مسح أي طلبات قديمة (عشان نضمن إن واحد بس اللي شغال)
                    handler.removeCallbacks(this)

                    var currentItem = binding.viewPagerBanner.currentItem
                    currentItem = if (currentItem == images.size - 1) 0 else currentItem + 1
                    binding.viewPagerBanner.setCurrentItem(currentItem, true)

                    // 2. طلب القلبة الجاية بعد 3 ثواني
                    handler.postDelayed(this, 3000)
                }
            }
        }
        handler.postDelayed(runnable, 5000)

        binding.indicator.setViewPager(binding.viewPagerBanner)

        bannerAdapter.registerAdapterDataObserver(binding.indicator.adapterDataObserver)

    }

    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private fun hideProgressbar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressbar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun hideErrorMessage() {
        itemHeadLinesError.visibility = View.INVISIBLE
        isError = false
    }

    private fun showErrorMessage(message: String) {
        itemHeadLinesError.visibility = View.VISIBLE
        errorText.text = message
        isError = true
    }

    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNoError = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate =
                isNoError && isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                newsViewModel.getHeadLines("us")
                isScrolling = false
            }


        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    private fun setupHeadLineRecycler() {
        newsAdapter = NewsAdapter()
        binding.recyclerHeadlines.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@HeadLinesFragment.scrollListener)


        }
    }

}