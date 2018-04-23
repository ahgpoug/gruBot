package com.fa.grubot.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fa.grubot.App;
import com.fa.grubot.R;
import com.fa.grubot.abstractions.TelegramLoginFragmentBase;
import com.fa.grubot.presenters.TelegramLoginPresenter;
import com.github.badoualy.telegram.tl.api.auth.TLSentCode;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.schedulers.Schedulers;

public class TelegramLoginFragment extends Fragment implements TelegramLoginFragmentBase {
    @Nullable @BindView(R.id.phoneNumber) EditText phoneNumberText;
    @Nullable @BindView(R.id.continueBtn) Button continueBtn;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private TelegramLoginPresenter presenter;
    private Unbinder unbinder;

    public static TelegramLoginFragment newInstance() {
        Bundle args = new Bundle();
        TelegramLoginFragment fragment = new TelegramLoginFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        presenter = new TelegramLoginPresenter(this);
        View v = inflater.inflate(R.layout.fragment_telegram_login, container, false);
        setHasOptionsMenu(true);
        unbinder = ButterKnife.bind(this, v);

        presenter.notifyFragmentStarted();
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        presenter.destroy();
    }

    public void setupToolbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();

        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("TelegramHelper вход");
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void setupViews() {
        continueBtn.setOnClickListener(v -> {
            String phoneNumber = phoneNumberText.getText().toString();

            if (!phoneNumber.isEmpty()) {
                Observable<Object> telegramSendAuthCodeObs = App.INSTANCE.telegramMessenger.sendAuthCodeObs(phoneNumber).timeout(3, TimeUnit.SECONDS).onErrorResumeNext(Observable.empty()).defaultIfEmpty(new Exception());

                Observable.defer(() -> telegramSendAuthCodeObs)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(returnObject -> {
                            if (returnObject instanceof TLSentCode) {
                                TLSentCode tlSentCode = (TLSentCode) returnObject;
                                Fragment telegramVerificationFragment = TelegramVerificationFragment.newInstance(tlSentCode, phoneNumber);

                                FragmentManager fm = getActivity().getSupportFragmentManager();
                                FragmentTransaction transaction = fm.beginTransaction();
                                transaction.addToBackStack(null);
                                transaction.replace(R.id.content, telegramVerificationFragment);
                                transaction.commit();
                            } else if (returnObject instanceof Exception) {
                                Toast.makeText(getActivity(), "Ошибка: " + ((Exception) returnObject).getMessage(), Toast.LENGTH_LONG).show();
                            }
                        })
                        .subscribe();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                getActivity().onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
