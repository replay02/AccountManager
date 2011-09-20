
package com.tomovwgti.account;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class AccountManagerSampleActivity extends Activity implements
        AccountManagerCallback<Bundle> {
    private static final String TAG = AccountManagerSampleActivity.class.getSimpleName();

    private AccountManager mAccountManager = null;
    private String mToken = null;
    private String mAuthTokenType = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mAuthTokenType = CALENDAR;
        Account[] accounts = null;
        if (mAccountManager == null) {
            mAccountManager = AccountManager.get(this);
        }
        accounts = mAccountManager.getAccountsByType("com.google");
        for (Account ac : accounts) {
            // 複数のアカウントがある場合は、複数が取れる
            Log.d(TAG, ac.toString());
        }

        // 1つめのGmailアカウントで認証する
        AccountManagerFuture<Bundle> accountManagerFuture = mAccountManager.getAuthToken(
                accounts[0], mAuthTokenType, false, this, null);
    }

    @Override
    public void run(AccountManagerFuture<Bundle> data) {
        Bundle bundle;
        try {
            bundle = data.getResult();
            Intent intent = (Intent) bundle.get(AccountManager.KEY_INTENT);
            if (intent != null) {
                Log.d(TAG, "User Input required");
                startActivity(intent);
            } else {
                mToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                Log.d(TAG, "Token = " + mToken);
                loginGoogle();
            }
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loginGoogle() {
        DefaultHttpClient http_client = new DefaultHttpClient();
        HttpGet http_get = new HttpGet("https://www.google.com/accounts/TokenAuth?auth=" + mToken
                + "&service=" + mAuthTokenType + "&source=Android"
                + "&continue=http://www.google.com/");
        HttpResponse response = null;
        try {
            response = http_client.execute(http_get);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                String entity = EntityUtils.toString(response.getEntity());
                Log.d(TAG, entity);
                if (entity.contains("The page you requested is invalid")) {
                    Log.d(TAG, "The page you requested is invalid");
                    mAccountManager.invalidateAuthToken("com.google", mToken);
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            Log.d(TAG, "Login failure");
    }

    private static final String ANDROID = "android";
    private static final String APP_ENGINE = "ah";
    private static final String CALENDAR = "cl";
    private static final String MAIL = "mail";
    private static final String TALK = "talk";
    private static final String YOUTUBE = "youtube";
    private static final String FUSION_TABLES = "fusiontables";
}
