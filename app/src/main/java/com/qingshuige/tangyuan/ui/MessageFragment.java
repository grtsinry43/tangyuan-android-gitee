package com.qingshuige.tangyuan.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.qingshuige.tangyuan.PostActivity;
import com.qingshuige.tangyuan.R;
import com.qingshuige.tangyuan.TangyuanApplication;
import com.qingshuige.tangyuan.TokenManager;
import com.qingshuige.tangyuan.data.DataTools;
import com.qingshuige.tangyuan.network.ApiHelper;
import com.qingshuige.tangyuan.network.NewNotification;
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
            TangyuanApplication.getApi().getAllNotificationsByUserId(DataTools.decodeJwtTokenUserId(tm.getToken())).enqueue(new Callback<List<NewNotification>>() {
                @Override
                public void onResponse(Call<List<NewNotification>> call, Response<List<NewNotification>> response) {
                    if (response.code() == 200 && response.body() != null) {
                        List<NewNotification> notifications = response.body();
                        ApiHelper.getNotificationInfoFastAsync(notifications, MessageFragment.this.getContext(), result -> {
                            if (result != null) {
                                getActivity().runOnUiThread(() -> {
                                    adapter.setDataset(result);
                                    pgBar.setVisibility(View.GONE);
                                });
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<List<NewNotification>> call, Throwable throwable) {

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
        switch (info.getNotification().type) {
            case "comment":
            case "reply":
                Intent intent = new Intent(getActivity(), PostActivity.class);
                intent.putExtra("postId", info.getRelatedPostId());
                intent.putExtra("commentId", info.getNotification().sourceId);
                startActivity(intent);
                break;
        }
        TangyuanApplication.getApi().markNewNotificationAsRead(info.getNotification().notificationId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() != 200) {
                    new AlertDialog.Builder(getContext())
                            .setMessage(R.string.cannot_mark_notification)
                            .setTitle(R.string.network_error)
                            .setPositiveButton(R.string.ok, null)
                            .create().show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                new AlertDialog.Builder(getContext())
                        .setMessage(R.string.cannot_mark_notification)
                        .setTitle(R.string.network_error)
                        .create().show();
            }
        });

    }
}