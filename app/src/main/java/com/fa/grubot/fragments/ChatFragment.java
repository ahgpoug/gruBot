package com.fa.grubot.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fa.grubot.App;
import com.fa.grubot.R;
import com.fa.grubot.abstractions.ChatFragmentBase;
import com.fa.grubot.holders.IncomingImageMessageViewHolder;
import com.fa.grubot.holders.OutcomingImageMessageViewHolder;
import com.fa.grubot.objects.chat.Chat;
import com.fa.grubot.objects.chat.ChatImageMessage;
import com.fa.grubot.objects.chat.ChatMessage;
import com.fa.grubot.objects.chat.MessagesListParcelable;
import com.fa.grubot.objects.users.User;
import com.fa.grubot.presenters.ChatPresenter;
import com.fa.grubot.util.Consts;
import com.fa.grubot.util.ImageLoader;
import com.github.badoualy.telegram.tl.api.TLInputPeerChannel;
import com.github.badoualy.telegram.tl.api.TLInputPeerChat;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.vk.sdk.VKAccessToken;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import io.reactivex.annotations.Nullable;

public class ChatFragment extends Fragment
        implements ChatFragmentBase, Serializable, MessagesListAdapter.OnLoadMoreListener, MessageInput.InputListener, MessageHolders.ContentChecker<ChatMessage> {

    @Nullable @BindView(R.id.messagesList) MessagesListParcelable messagesList;
    @Nullable @BindView(R.id.input) MessageInput messageInput;
    @Nullable @BindView(R.id.toolbar) Toolbar toolbar;

    @Nullable @BindView(R.id.retryBtn) Button retryBtn;

    @Nullable @BindView(R.id.progressBar) ProgressBar progressBar;
    @Nullable @BindView(R.id.content) View content;
    @Nullable @BindView(R.id.noInternet) View noInternet;
    @Nullable @BindView(R.id.noData) View noData;

    private ChatPresenter presenter;
    private MessagesListAdapter<ChatMessage> messagesListAdapter;
    private Unbinder unbinder;

    private int state;
    private Chat chat;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    public static ChatFragment newInstance(Chat chat) {
        Bundle args = new Bundle();
        args.putSerializable("chat", chat);
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        presenter = new ChatPresenter(this, getActivity());
        setHasOptionsMenu(true);
        unbinder = ButterKnife.bind(this, v);
        chat = (Chat) this.getArguments().getSerializable("chat");

        return v;
    }

    @Override
    public void onResume() {
        presenter.notifyFragmentStarted(chat);
        if (chat.getType().equals(Consts.Telegram)) {
            presenter.setUpdateCallback();
        } else if (chat.getType().equals(Consts.VK) && false){
            presenter.setupPollingVk();
        }
        if (App.INSTANCE.getResendingMessage() != null){
            presenter.sendMessage(App.INSTANCE.getResendingMessage());
            App.INSTANCE.resetResendingMessage();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        App.INSTANCE.closeTelegramClient();
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d("tag", "onStop called");
        App.INSTANCE.closeTelegramClient();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        App.INSTANCE.closeTelegramClient();
        presenter.destroy();
        unbinder.unbind();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public boolean onSubmit(CharSequence input) {
        presenter.sendMessage(input.toString());
        messageInput.getInputEditText().setText(null);
        return false;
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        if (chat.getType().equals(Consts.Telegram)) {
            presenter.loadMoreMessages(totalItemsCount);
        } else if (chat.getType().equals(Consts.Telegram)){
            presenter.loadmoreVkMessages();
        }
    }

    @Override
    public boolean hasContentFor(ChatMessage message, byte type) {
        switch (type) {
            case Consts.MESSAGE_CONTENT_TYPE_IMAGE:
                return (message instanceof ChatImageMessage);
        }
        return false;
    }

    private void animateViewAppearance(View view) {
        view.setAlpha(0.0f);
        view.animate()
                .translationY(view.getHeight())
                .alpha(1.0f)
                .setListener(null);
    }

    @Override
    public void showRequiredViews() {
        progressBar.setVisibility(View.GONE);
        noInternet.setVisibility(View.GONE);
        noData.setVisibility(View.GONE);
        content.setVisibility(View.GONE);

        switch (state) {
            case Consts.STATE_CONTENT:
                content.setVisibility(View.VISIBLE);
                animateViewAppearance(content);
                break;
            case Consts.STATE_NO_INTERNET_CONNECTION:
                noInternet.setVisibility(View.VISIBLE);
                animateViewAppearance(noInternet);
                break;
            case Consts.STATE_NO_DATA:
                content.setVisibility(View.VISIBLE);
                animateViewAppearance(content);
                break;
        }
    }

    @Override
    public void setupLayouts(boolean isNetworkAvailable, boolean isHasData) {
        if (isNetworkAvailable) {
            if (isHasData)
                state = Consts.STATE_CONTENT;
            else {
                state = Consts.STATE_NO_DATA;
                messagesListAdapter = null;
            }
        }
        else {
            state = Consts.STATE_NO_INTERNET_CONNECTION;
            messagesListAdapter = null;
        }
    }

    public void addNewMessagesToList(ArrayList<ChatMessage> messages, boolean moveToTop) {
        if (!messages.isEmpty())
            messagesListAdapter.addToEnd(messages, true);
    }

    @Override
    public void onMessageReceived(ChatMessage chatMessage) {
        messagesListAdapter.addToStart(chatMessage, true);
    }

    @Override
    public void setupRecyclerView(ArrayList<ChatMessage> messages, String type) {
        MessageHolders holdersConfig = new MessageHolders()
                .setIncomingTextLayout(R.layout.item_incoming_text_message)
                .setOutcomingTextLayout(R.layout.item_outcoming_text_message)
                .registerContentType(Consts.MESSAGE_CONTENT_TYPE_IMAGE,
                        IncomingImageMessageViewHolder.class,
                        R.layout.item_incoming_image_message,
                        OutcomingImageMessageViewHolder.class,
                        R.layout.item_outcoming_image_message,
                        this);

        ImageLoader imageLoader = new ImageLoader(this);

        String userId = "-1";
        if (type.equals(Consts.VK)){
            userId = VKAccessToken.currentToken().userId;
        } else if (type.equals(Consts.Telegram)){
            userId = String.valueOf(App.INSTANCE.getCurrentUser().getTelegramUser().getId());
        }

        messagesListAdapter = new MessagesListAdapter<>(userId, holdersConfig, imageLoader);
        messagesListAdapter.registerViewClickListener(R.id.messageUserAvatar, (view, message) -> showUserProfile((User) message.getUser()));
        messagesListAdapter.addToEnd(messages, false);
        messagesListAdapter.setLoadMoreListener(this);
        messagesListAdapter.setOnMessageLongClickListener(message ->{
            new MaterialDialog.Builder(this.getContext())
                    .items("Переслать...", "Отмена")
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            if (which == 0){
                                resendMessage(message.getText());
                            }
                        }
                    })
                    .show();
        });
        messagesList.setAdapter(messagesListAdapter);
        messageInput.setInputListener(this);
    }

    private void resendMessage(CharSequence text) {
        App.INSTANCE.setResendingMessage(text.toString());

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(((ViewGroup)getView().getParent()).getId(), ChatsListFragment.newInstance(1));
        ft.commitAllowingStateLoss();
    }

    private void showUserProfile(User user) {
        Fragment profileItemFragment = ProfileItemFragment.newInstance(Integer.valueOf(user.getId()), user.getUserType(), user, Consts.PROFILE_MODE_SINGLE);

        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.addToBackStack(null);
        transaction.add(R.id.content, profileItemFragment);
        transaction.commit();
    }

    @Override
    public void setupToolbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle(chat.getName());
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.bringToFront();

        toolbar.setOnClickListener(v -> {
            if (chat.getInputPeer() instanceof TLInputPeerChat || chat.getInputPeer() instanceof TLInputPeerChannel) {
                Fragment groupInfoFragment = GroupInfoFragment.newInstance(0, chat);

                FragmentManager fm = this.getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.addToBackStack(null);
                transaction.add(R.id.content, groupInfoFragment);
                transaction.commit();
            }
        });
    }

    @Override
    public void setupRetryButton() {
        retryBtn.setOnClickListener(view -> presenter.onRetryBtnClick());
    }

    @Override
    public boolean isListEmpty() {
        return messagesListAdapter == null || messagesListAdapter.getItemCount() == 0;
    }

    @Override
    public boolean isAdapterExists() {
        return messagesListAdapter != null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
