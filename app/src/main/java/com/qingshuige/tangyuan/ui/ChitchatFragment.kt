package com.qingshuige.tangyuan.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.qingshuige.tangyuan.PostActivity
import com.qingshuige.tangyuan.R
import com.qingshuige.tangyuan.TangyuanApplication
import com.qingshuige.tangyuan.databinding.FragmentChitchatBinding
import com.qingshuige.tangyuan.network.ApiHelper
import com.qingshuige.tangyuan.network.PostMetadata
import com.qingshuige.tangyuan.viewmodels.PostCardAdapter
import java.io.IOException

class ChitchatFragment : Fragment() {

    private var binding: FragmentChitchatBinding? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostCardAdapter
    private lateinit var swp: SwipeRefreshLayout
    private lateinit var fab: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChitchatBinding.inflate(inflater, container, false)
        val root = binding!!.root

        swp = root.findViewById(R.id.swpChitChat)
        swp.setOnRefreshListener { updateRecyclerView() }
        swp.setColorSchemeColors(
            requireActivity().getColor(R.color.mazarine_blue),
            requireActivity().getColor(R.color.nanohanacha_gold)
        )

        fab = root.findViewById(R.id.fabTop)
        fab.setOnClickListener { updateRecyclerView() }

        recyclerView = root.findViewById(R.id.chitchat_recyclerview)
        adapter = PostCardAdapter()
        adapter.setSectionVisible(false)
        recyclerView.adapter = adapter
        val rcvLayoutManager = LinearLayoutManager(requireActivity())
        recyclerView.layoutManager = rcvLayoutManager
        adapter.setOnItemClickListener { postId ->
            val intent = Intent(requireActivity(), PostActivity::class.java)
            intent.putExtra("postId", postId)
            startActivity(intent)
        }

        val divider = DividerItemDecoration(requireActivity(), rcvLayoutManager.orientation)
        recyclerView.addItemDecoration(divider)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val firstVisiblePosition = rcvLayoutManager.findFirstVisibleItemPosition()
                if (firstVisiblePosition >= 5 && !swp.isRefreshing) {
                    fab.show()
                } else {
                    fab.hide()
                }
            }
        })

        updateRecyclerView()

        return root
    }

    private fun updateRecyclerView() {
        swp.isRefreshing = true
        fab.hide()

        Thread {
            try {
                val response =
                    TangyuanApplication.getApi().phtPostMetadata(2, adapter.getAllPostIds())
                        .execute()
                if (response.code() == 200) {
                    val metadatas = response.body()
                    if (metadatas != null) {
                        ApiHelper.getInfoFastAsync(
                            metadatas,
                            ApiHelper.PostInfoConstructor()
                        ) { result ->
                            if (result != null) {
                                Handler(Looper.getMainLooper()).post {
                                    (recyclerView.adapter as PostCardAdapter).prependDataset(result)
                                    recyclerView.scrollToPosition(0)
                                }
                            } else {
                                Handler(Looper.getMainLooper()).post {
                                    Toast.makeText(
                                        context,
                                        R.string.network_error,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            Handler(Looper.getMainLooper()).post {
                                swp.isRefreshing = false
                            }
                        }
                    } else {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT)
                                .show()
                        }
                        swp.isRefreshing = false
                    }
                } else if (response.code() == 404) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, R.string.no_post_to_show, Toast.LENGTH_SHORT).show()
                    }
                    swp.isRefreshing = false
                }
            } catch (e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, R.string.network_error, Toast.LENGTH_SHORT).show()
                }
                swp.isRefreshing = false
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}