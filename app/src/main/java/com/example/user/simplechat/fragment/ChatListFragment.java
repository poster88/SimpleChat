package com.example.user.simplechat.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.user.simplechat.R;
import com.example.user.simplechat.activity.BaseActivity;
import com.example.user.simplechat.adapter.UserRecycleAdapter;
import com.example.user.simplechat.fragment.impl.MyClickListener;
import com.example.user.simplechat.listener.ChildValueListener;
import com.example.user.simplechat.listener.ValueListener;
import com.example.user.simplechat.model.ChatTable;
import com.example.user.simplechat.model.User;
import com.example.user.simplechat.utils.Const;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * Created by User on 011 11.10.17.
 */

public class ChatListFragment extends BaseFragment implements MyClickListener{
    @BindView(R.id.userRecycleView) RecyclerView usersRecView;

    private String currentUserID;
    private ArrayList<User> usersListData;
    private ArrayList<String> enabledChatUsersData;
    private UserRecycleAdapter adapter;
    private LinearLayoutManager layoutManager;
    private DatabaseReference chatTableRef;
    private FirebaseDatabase database;
    private Query query;

    public static ChatListFragment newInstance(){
        return new ChatListFragment();
    }

    private ChildValueListener usersInfoListener = new ChildValueListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if (!usersListData.contains(dataSnapshot.getValue(User.class))){
                if (!dataSnapshot.getValue(User.class).getUserID().equals(currentUserID)) {
                    usersListData.add(dataSnapshot.getValue(User.class));
                    adapter.notifyItemInserted(usersListData.size());
                }
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            for (int i = 0; i < usersListData.size(); i++) {
                if (usersListData.get(i).getUserID().equals(dataSnapshot.getKey())){
                    usersListData.set(i, dataSnapshot.getValue(User.class));
                    adapter.notifyItemChanged(i);
                    break;
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            super.onChildRemoved(dataSnapshot);
            int index = usersListData.indexOf(dataSnapshot.getValue(User.class));
            usersListData.remove(index);
            adapter.notifyItemRemoved(index);
        }
    };

    private ChildValueListener chatIDTableListener = new ChildValueListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if (dataSnapshot.exists() && !enabledChatUsersData.contains(dataSnapshot.getKey())){
                enabledChatUsersData.add(dataSnapshot.getKey());
                updateAdapterItems(dataSnapshot.getKey());
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            enabledChatUsersData.remove(dataSnapshot.getKey());
        }
    };

    private void updateAdapterItems(String data){
        for (int i = 0; i < usersListData.size(); i++) {
            if (usersListData.get(i).getUserID().equals(data)){
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        innitDataForQuery();
    }

    private void innitDataForQuery() {
        currentUserID = FirebaseAuth.getInstance().getUid();
        database = FirebaseDatabase.getInstance();
        usersListData = new ArrayList<>();
        enabledChatUsersData = new ArrayList<>();
        query = database.getReferenceFromUrl(Const.REF_USERS).orderByChild(Const.QUERY_NAME_KEY);
        chatTableRef = database.getReference(Const.CHAT_ID_TABLE).child(currentUserID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_list_fragment, container, false);
        bindFragment(this, view);
        layoutManager = new LinearLayoutManager(getActivity());
        if (savedInstanceState != null){
            usersListData = savedInstanceState.getParcelableArrayList(Const.USER_LIST_DATA_KEY);
            enabledChatUsersData = savedInstanceState.getStringArrayList(Const.CHAT_ID_TABLE_DATA_KEY);
            layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(Const.LAYOUT_MANAGER_KEY));
        }
        innitAdapter();
        return view;
    }

    private void innitAdapter() {
        adapter = new UserRecycleAdapter(usersListData, enabledChatUsersData);
        adapter.setMyClickListener(ChatListFragment.this);
        usersRecView.setLayoutManager(layoutManager);
        usersRecView.setAdapter(adapter);
        usersRecView.addItemDecoration(setItemDecoration());
    }

    private DividerItemDecoration setItemDecoration() {
        return new DividerItemDecoration(usersRecView.getContext(), layoutManager.getOrientation());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(Const.USER_LIST_DATA_KEY, usersListData);
        outState.putStringArrayList(Const.CHAT_ID_TABLE_DATA_KEY, enabledChatUsersData);
        outState.putParcelable(Const.LAYOUT_MANAGER_KEY, layoutManager.onSaveInstanceState());
    }

    @Override
    public void onItemClick(final String userID, final  byte[] recPhotoArray) {
        chatTableRef.child(userID).addListenerForSingleValueEvent(new ValueListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setDataForChatFragment(userID, recPhotoArray, dataSnapshot);
            }
        });
    }

    private void setDataForChatFragment(String userID, byte[] recPhotoArray, DataSnapshot dataSnapshot){
        SharedPreferences sPref = getContext().getSharedPreferences(Const.USER_DATA, Context.MODE_PRIVATE);
        String strImage = sPref.getString(Const.USER_IMAGE_KEY, null);

        ((BaseActivity) getActivity()).replaceFragments(ChatFragment.newInstance(
                userID,
                checkDataSnapshot(dataSnapshot, chatTableRef, userID),
                stringToByte(strImage),
                recPhotoArray), Const.CHAT_FRAG_TAG);
    }

    public byte[] stringToByte(String encodedString){
        return (encodedString != null) ? Base64.decode(encodedString,Base64.DEFAULT) : null;
    }

    private String checkDataSnapshot(DataSnapshot dataSnapshot, DatabaseReference ref, String receiverID) {
        String chatID = dataSnapshot.getValue(String.class);
        if (chatID == null){
            chatID = ref.push().getKey();
            createChatWithUser(currentUserID, receiverID, chatID);
            createChatWithUser(receiverID, currentUserID, chatID);
        }
        return chatID;
    }

    private void createChatWithUser(String currentID, String receiverID, String chatID){
        ChatTable chatTable = new ChatTable(chatID, receiverID);
        database.getReference(Const.CHAT_ID_TABLE).child(currentID).updateChildren(chatTable.toMap());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (query != null && chatTableRef != null){
            query.addChildEventListener(usersInfoListener);
            chatTableRef.addChildEventListener(chatIDTableListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (query != null && chatTableRef != null){
            query.removeEventListener(usersInfoListener);
            chatTableRef.removeEventListener(chatIDTableListener);
        }
    }
}