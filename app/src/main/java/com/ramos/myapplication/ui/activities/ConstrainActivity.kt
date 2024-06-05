package com.ramos.myapplication.ui.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.ramos.myapplication.databinding.ActivityConstrainBinding
import com.ramos.myapplication.logic.usercases.GetAllTopsNewUserCase
import com.ramos.myapplication.ui.adapters.NewsDiffutilAdapter
import com.ramos.myapplication.ui.entities.NewsDataUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConstrainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConstrainBinding
    private var items: MutableList<NewsDataUI> = mutableListOf()
    private lateinit var newsListAdapter: NewsDiffutilAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConstrainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initVariables()
        initListeners()
        initData()
    }

    private fun initVariables() {
        newsListAdapter = NewsDiffutilAdapter(
            { descriptionItem(it) },
            { deleteItem(it) }
        )
        binding.rvTopNews.adapter = newsListAdapter
        binding.rvTopNews.layoutManager = CarouselLayoutManager()
    }

    private fun initListeners() {
        binding.refreshRV.setOnRefreshListener {
            initData()
            binding.refreshRV.isRefreshing = false
        }

        binding.btnInsert.setOnClickListener {
            addItem()
        }
    }

    private fun initData() {
        binding.pgbarLoadData.visibility = View.VISIBLE
        binding.animationView.visibility=View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val resultItems = GetAllTopsNewUserCase().invoke()
            withContext(Dispatchers.Main) {
                binding.pgbarLoadData.visibility = View.INVISIBLE
                binding.animationView.visibility = View.GONE

                resultItems.onSuccess {
                    items = it.toMutableList()
                    newsListAdapter.submitList(items.toList())
                }

                resultItems.onFailure {
                    Snackbar.make(binding.refreshRV, it.message.toString(), Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun descriptionItem(news: NewsDataUI) {
        Snackbar.make(binding.refreshRV, news.name, Snackbar.LENGTH_LONG).show()
    }

    private fun deleteItem(position: Int) {
        items.removeAt(position)
        newsListAdapter.submitList(items.toList())
    }

    private fun addItem() {
        lifecycleScope.launch(Dispatchers.IO) {
            val resultItems = GetAllTopsNewUserCase().invoke()
            withContext(Dispatchers.Main) {
                resultItems.onSuccess { newsList ->
                    if (newsList.isNotEmpty()) {
                        val newItem = newsList.random()  // Puedes cambiar esto para obtener un elemento específico si lo deseas
                        items.add(newItem)
                        newsListAdapter.submitList(items.toList())
                    } else {
                        Snackbar.make(binding.refreshRV, "No new items available", Snackbar.LENGTH_SHORT).show()
                    }
                }

                resultItems.onFailure {
                    Snackbar.make(binding.refreshRV, it.message.toString(), Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }
}
