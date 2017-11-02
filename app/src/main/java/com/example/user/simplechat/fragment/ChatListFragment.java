package com.example.user.simplechat.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.user.simplechat.R;
import com.example.user.simplechat.adapter.FirebaseUserAdapter;
import com.example.user.simplechat.model.User;
import com.example.user.simplechat.utils.Const;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import butterknife.BindView;

/**
 * Created by User on 011 11.10.17.
 */

public class ChatListFragment extends BaseFragment implements FirebaseUserAdapter.MyClickListener{
    @BindView(R.id.userRecycleView) RecyclerView usersRecView;

    private FirebaseUserAdapter usersListAdapter;

    public static ChatListFragment newInstance(){
        return new ChatListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            setRetainInstance(true);
            usersListAdapter = new FirebaseUserAdapter(setFirebaseRecyclerOptions(), FirebaseAuth.getInstance().getUid());
            usersListAdapter.setMyOnClickListener(this);
            usersListAdapter.startListening();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_list_fragment, container, false);
        bindFragment(this, view);
        setRecycleView();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        usersListAdapter.stopListening();
    }

    private FirebaseRecyclerOptions<User> setFirebaseRecyclerOptions() {
        Query query = FirebaseDatabase.getInstance().getReferenceFromUrl(Const.REF_USERS).orderByChild(Const.QUERY_NAME_KEY);
        return new FirebaseRecyclerOptions.Builder<User>().setQuery(query, User.class).build();
    }

    private void setRecycleView() {
        usersRecView.setLayoutManager(new LinearLayoutManager(getActivity()));
        usersRecView.setAdapter(usersListAdapter);
    }

    @Override
    public void onItemClick(String userID) {
        super.replaceFragments(ChatFragment.newInstance(userID), Const.CHAT_FRAG_TAG);
    }
}
