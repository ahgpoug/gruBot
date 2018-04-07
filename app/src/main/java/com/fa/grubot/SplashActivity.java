package com.fa.grubot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.fa.grubot.helpers.TelegramHelper;
import com.fa.grubot.objects.users.CurrentUser;
import com.fa.grubot.objects.users.VkUser;
import com.fa.grubot.util.Globals;
import com.github.badoualy.telegram.api.TelegramClient;
import com.github.badoualy.telegram.tl.api.TLInputUserSelf;
import com.github.badoualy.telegram.tl.api.TLUser;
import com.github.badoualy.telegram.tl.api.TLUserFull;
import com.github.badoualy.telegram.tl.exception.RpcErrorException;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;

import java.lang.ref.WeakReference;

import static com.fa.grubot.App.INSTANCE;

public class SplashActivity extends AppCompatActivity {
    private VkUser vkUser;
    private TLUser tlUser;
    private boolean tlUserChecked = false;
    private boolean vkUserChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadPreferences();
        if (Globals.InternetMethods.isNetworkAvailable(this)) {
            (new TryToLoginAsyncTask(this)).execute();
            if (VKSdk.isLoggedIn() && VKAccessToken.tokenFromFile(App.INSTANCE.getVkTokenFilePath()) != null) {
                vkUser = new VkUser(VKAccessToken.tokenFromFile(App.INSTANCE.getVkTokenFilePath()).accessToken);
            }
            vkUserChecked = true;
            nextIfBothAccountsChecked();
        } else {
            Toast.makeText(this, "Нет подключения к сети", Toast.LENGTH_LONG).show();
            this.finishAffinity();
        }
    }

    private void loadPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        INSTANCE.setAnimationsEnabled(prefs.getBoolean("animationsSwitch", false));
        INSTANCE.setBackstackEnabled(prefs.getBoolean("backstackSwitch", false));
        INSTANCE.setSlidrEnabled(prefs.getBoolean("slidrSwitch", true));
    }

    @Override
    protected void onDestroy() {
        App.INSTANCE.closeTelegramClient();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        App.INSTANCE.closeTelegramClient();
        super.onPause();
    }

    @Override
    protected void onStop() {
        App.INSTANCE.closeTelegramClient();
        super.onStop();
    }

    private class TryToLoginAsyncTask extends AsyncTask<Void, Void, Object> {
        private WeakReference<Context> context;

        private TryToLoginAsyncTask(Context context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Void... params) {
            Object returnObject;

            TelegramClient client = App.INSTANCE.getNewTelegramClient(null);

            try {
                TLUserFull userFull = client.usersGetFullUser(new TLInputUserSelf());
                returnObject = userFull.getUser().getAsUser();
                App.INSTANCE.getCurrentUser().setTelegramChatUser(TelegramHelper.Chats.getChatUser(client, userFull.getUser().getId(), context.get()));
            } catch (RpcErrorException e) {
                returnObject = e;
            } catch (Exception e) {
                returnObject = e;
            } finally {
                App.INSTANCE.closeTelegramClient();
            }
            return returnObject;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (result instanceof Exception) {
                Toast.makeText(context.get(), ((RpcErrorException) result).getTag(), Toast.LENGTH_SHORT).show();
                setTlUser(null);
            } else {
                setTlUser((TLUser) result);
            }

            nextIfBothAccountsChecked();

            super.onPostExecute(result);
        }
    }

    private void setTlUser(TLUser tlUser) {
        tlUserChecked = true;
        this.tlUser = tlUser;
    }

    private void nextIfBothAccountsChecked() {
        if ((vkUser != null || tlUser != null) && (vkUserChecked && tlUserChecked)) {
            App.INSTANCE.setCurrentUser(new CurrentUser(tlUser, vkUser));
            startActivity(new Intent(this, MainActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        } else if (vkUserChecked && tlUserChecked){
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }
}
