
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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class AccountManagerSampleActivity extends Activity implements
        AccountManagerCallback<Bundle> {
    private static final String TAG = AccountManagerSampleActivity.class.getSimpleName();

    private AccountManager mAccountManager = null;
    private static String sToken = null;
    private String mAuthTokenType = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (mAccountManager == null) {
            mAccountManager = AccountManager.get(this);
        }

        // 認証するサービスを設定
        setAuthTokenType(AccountManagerSampleActivity.CALENDAR);

        Button auth = (Button) findViewById(R.id.auth);
        auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Account[] accounts = null;
                accounts = mAccountManager.getAccountsByType("com.google");
                for (Account ac : accounts) {
                    // 複数のアカウントがある場合は、複数が取れる
                    Log.d(TAG, ac.toString());
                }

                // 1つめのGmailアカウントで認証する
                AccountManagerFuture<Bundle> accountManagerFuture = mAccountManager
                        .getAuthToken(accounts[0], mAuthTokenType, false,
                                AccountManagerSampleActivity.this, null);
            }
        });
    }

    /**
     * 認証するサービスの設定
     * 
     * @param type
     */
    public void setAuthTokenType(String type) {
        mAuthTokenType = type;
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
                sToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                Log.d(TAG, "Token = " + sToken);
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

    /**
     * Googleのサービスの認証
     */
    private void loginGoogle() {
        DefaultHttpClient http_client = new DefaultHttpClient();
        HttpGet http_get = new HttpGet("https://www.google.com/accounts/TokenAuth?auth=" + sToken
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
                    mAccountManager.invalidateAuthToken("com.google", sToken);
                } else {
                    Toast.makeText(this, "Authentication Success", Toast.LENGTH_LONG).show();
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Login failure");
        }
    }

    /**
     * 取得したトークンを返す
     * 
     * @return
     */
    public static String getToken() {
        return sToken;
    }

    public static final String ANDROID = "android";
    public static final String APP_ENGINE = "ah";
    public static final String CALENDAR = "cl";
    public static final String MAIL = "mail";
    public static final String TALK = "talk";
    public static final String YOUTUBE = "youtube";
    public static final String FUSION_TABLES = "fusiontables";
}
