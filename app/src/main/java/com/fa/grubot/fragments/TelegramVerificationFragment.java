package com.fa.grubot.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.fa.grubot.MainActivity;
import com.fa.grubot.R;
import com.fa.grubot.abstractions.TelegramVerificationFragmentBase;
import com.fa.grubot.presenters.TelegramVerificationPresenter;
import com.github.badoualy.telegram.tl.api.auth.TLAuthorization;
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

public class TelegramVerificationFragment extends Fragment implements TelegramVerificationFragmentBase {
    @Nullable @BindView(R.id.verificationCode) EditText verificationCodeText;
    @Nullable @BindView(R.id.continueBtn) Button continueBtn;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private TelegramVerificationPresenter presenter;
    private Unbinder unbinder;

    private TLSentCode sentCode;
    private String phoneNumber;

    public static TelegramVerificationFragment newInstance(TLSentCode sentCode, String phoneNumber) {
        Bundle args = new Bundle();
        args.putString("phoneNumberText", phoneNumber);
        args.putSerializable("sentCode", sentCode);
        TelegramVerificationFragment fragment = new TelegramVerificationFragment();
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
        presenter = new TelegramVerificationPresenter(this);
        View v = inflater.inflate(R.layout.fragment_telegram_verification, container, false);
        setHasOptionsMenu(true);
        sentCode = (TLSentCode) this.getArguments().getSerializable("sentCode");
        phoneNumber = this.getArguments().getString("phoneNumberText");
        unbinder = ButterKnife.bind(this, v);

        presenter.notifyFragmentStarted();
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    public void setupToolbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();

        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("TelegramHelper подтверждение");
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void setupViews() {
        continueBtn.setOnClickListener(v -> {
            String verificationCode = verificationCodeText.getText().toString();
            if (!verificationCode.isEmpty()) {
                Observable<Object> verifyAuthCodeObs = App.INSTANCE.telegramMessenger.verifyAuthCodeObs(sentCode, phoneNumber, verificationCode).timeout(3, TimeUnit.SECONDS).onErrorResumeNext(Observable.empty()).defaultIfEmpty(new Exception());

                Observable.defer(() -> verifyAuthCodeObs)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(returnObject -> {
                            if (returnObject instanceof TLAuthorization) {
                                getActivity().startActivity(new Intent(getActivity(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                            } else if (returnObject instanceof Exception) {
                                Toast.makeText(getActivity(), "Ошибка: " + ((Exception) returnObject).getMessage(), Toast.LENGTH_LONG).show();
                            }
                        })
                        .subscribe();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        presenter.destroy();
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
