package com.qingshuige.tangyuan.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.qingshuige.tangyuan.PostActivity
import com.qingshuige.tangyuan.R
import com.qingshuige.tangyuan.TangyuanApplication
import com.qingshuige.tangyuan.TokenManager
import com.qingshuige.tangyuan.data.DataTools
import com.qingshuige.tangyuan.network.ApiHelper
import com.qingshuige.tangyuan.network.NewNotification
import com.qingshuige.tangyuan.viewmodels.NotificationCardAdapter
import com.qingshuige.tangyuan.viewmodels.NotificationInfo
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MessageFragment : Fragment() {

    private lateinit var swpMessage: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationCardAdapter
    private lateinit var textMessageStatus: TextView
    private lateinit var tm: TokenManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_message, container, false)

        swpMessage = root.findViewById(R.id.swpMessage)
        recyclerView = root.findViewById(R.id.rcvMessage)
        textMessageStatus = root.findViewById(R.id.textMessageStatus)

        adapter = NotificationCardAdapter()
        adapter.setOnItemClickListener { info -> handleItemClick(info) }
        val layoutManager = LinearLayoutManager(requireActivity())
        val decoration = DividerItemDecoration(requireActivity(), layoutManager.orientation)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(decoration)

        swpMessage.setOnRefreshListener { initializeUI() }
        swpMessage.setColorSchemeColors(
            requireActivity().getColor(R.color.mazarine_blue),
            requireActivity().getColor(R.color.nanohanacha_gold)
        )

        tm = TangyuanApplication.getTokenManager()

        initializeUI()

        return root
    }

    private fun initializeUI() {
        swpMessage.isRefreshing = true

        val token = tm.token
        if (token != null) {
            TangyuanApplication.getApi().getAllNotificationsByUserId(DataTools.decodeJwtTokenUserId(token))
                .enqueue(object : Callback<List<NewNotification>> {
                    override fun onResponse(
                        call: Call<List<NewNotification>>,
                        response: Response<List<NewNotification>>
                    ) {
                        if (response.code() == 200 && response.body() != null) {
                            val notifications = response.body()!!
                            ApiHelper.getInfoFastAsync(
                                notifications,
                                ApiHelper.NotificationInfoConstructor(requireContext())
                            ) { result ->
                                if (result != null) {
                                    val sortedResult = result.sortedWith { notificationInfo: NotificationInfo, t1: NotificationInfo ->
                                        val date1 = t1.notification.createDate
                                        val date2 = notificationInfo.notification.createDate
                                        when {
                                            date1 == null && date2 == null -> 0
                                            date1 == null -> -1
                                            date2 == null -> 1
                                            else -> date1.compareTo(date2)
                                        }
                                    }
                                    activity?.runOnUiThread {
                                        adapter.setDataset(sortedResult)
                                        swpMessage.isRefreshing = false
                                    }
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<List<NewNotification>>, throwable: Throwable) {
                        // Handle failure
                    }
                })
        } else {
            swpMessage.isRefreshing = false
            swpMessage.visibility = View.GONE
            textMessageStatus.visibility = View.VISIBLE
            textMessageStatus.setText(R.string.unloggedin)
        }
    }

    private fun handleItemClick(info: NotificationInfo) {
        when (info.notification.type) {
            "comment", "reply" -> {
                val intent = Intent(requireActivity(), PostActivity::class.java)
                intent.putExtra("postId", info.relatedPostId)
                intent.putExtra("commentId", info.notification.sourceId)
                startActivity(intent)
            }
        }
        TangyuanApplication.getApi().markNewNotificationAsRead(info.notification.notificationId)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.code() != 200) {
                        AlertDialog.Builder(requireContext())
                            .setMessage(R.string.cannot_mark_notification)
                            .setTitle(R.string.network_error)
                            .setPositiveButton(R.string.ok, null)
                            .create().show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, throwable: Throwable) {
                    AlertDialog.Builder(requireContext())
                        .setMessage(R.string.cannot_mark_notification)
                        .setTitle(R.string.network_error)
                        .create().show()
                }
            })
    }
}