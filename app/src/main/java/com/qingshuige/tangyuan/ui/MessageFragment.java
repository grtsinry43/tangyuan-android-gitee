package com.qingshuige.tangyuan.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qingshuige.tangyuan.PostActivity;
import com.qingshuige.tangyuan.R;
import com.qingshuige.tangyuan.TangyuanApplication;
import com.qingshuige.tangyuan.TokenManager;
import com.qingshuige.tangyuan.data.DataTools;
import com.qingshuige.tangyuan.network.ApiHelper;
import com.qingshuige.tangyuan.network.Notification;
import com.qingshuige.tangyuan.viewmodels.NotificationCardAdapter;
import com.qingshuige.tangyuan.viewmodels.NotificationInfo;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationCardAdapter adapter;

    private ProgressBar pgBar;
    private TextView textMessageStatus;

    private TokenManager tm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_message, container, false);
        recyclerView = root.findViewById(R.id.rcvMessage);
        pgBar = root.findViewById(R.id.pgBar);
        textMessageStatus = root.findViewById(R.id.textMessageStatus);

        adapter = new NotificationCardAdapter();
        adapter.setOnItemClickListener(this::handleItemClick);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        DividerItemDecoration decoration = new DividerItemDecoration(getActivity(), layoutManager.getOrientation());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(decoration);

        tm = TangyuanApplication.getTokenManager();

        initializeUI();

        return root;
    }

    private void initializeUI() {
        if (tm.getToken() != null) {
            TangyuanApplication.getApi().getAllUnreadNotificationsOf(DataTools.decodeJwtTokenUserId(tm.getToken())).enqueue(new Callback<List<Notification>>() {
                @Override
                public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                    if (response.code() == 200) { //有通知
                        for (Notification n : response.body()) {
                            ApiHelper.getNotificationInfoAsync(n, result -> {
                                adapter.append(result);
                            });
                        }
                    } else if (response.code() == 404) { //无通知
                        recyclerView.setVisibility(View.GONE);
                        textMessageStatus.setVisibility(View.VISIBLE);
                        textMessageStatus.setText(R.string.no_message);
                    }
                    pgBar.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<List<Notification>> call, Throwable throwable) {
                    pgBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    textMessageStatus.setVisibility(View.VISIBLE);
                    textMessageStatus.setText(R.string.network_error);
                }
            });
        } else {
            pgBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            textMessageStatus.setVisibility(View.VISIBLE);
            textMessageStatus.setText(R.string.unloggedin);
        }
    }

    private void handleItemClick(NotificationInfo info) {
        Intent intent = new Intent(getActivity(), PostActivity.class);
        intent.putExtra("postId", info.getTargetPostId());
        intent.putExtra("commentId", info.getSourceCommentId());
        startActivity(intent);
        TangyuanApplication.getApi().markNotificationAsRead(info.getNotificationId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {

            }
        });
    }
}