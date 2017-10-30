package com.example.user.simplechat.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.user.simplechat.R;


/**
 * Created by User on 011 11.10.17.
 */

public class ChatListFragment extends BaseFragment {
    private String userID;

    public static ChatListFragment newInstance(String userID){
        Bundle bundle = new Bundle();
        bundle.putString(USER_ID, userID);
        ChatListFragment fragment = new ChatListFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(USER_ID)){
            userID = getArguments().getString(USER_ID);
            loadUserList();
        }
    }

    private void loadUserList() {
        //todo: firebaseListAdapter
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_list_fragment, container, false);
        bindFragment(this, view);
        return view;
    }
}
