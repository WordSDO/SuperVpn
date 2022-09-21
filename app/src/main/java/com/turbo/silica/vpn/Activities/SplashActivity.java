package com.turbo.silica.vpn.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.anchorfree.partner.api.auth.AuthMethod;
import com.anchorfree.partner.api.response.User;
import com.anchorfree.sdk.UnifiedSDK;
import com.anchorfree.vpnsdk.callbacks.Callback;
import com.anchorfree.vpnsdk.exceptions.VpnException;
import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;
import com.turbo.silica.vpn.BuildConfig;
import com.turbo.silica.vpn.Helpers.Config;
import com.turbo.silica.vpn.MainActivity;
import com.turbo.silica.vpn.MainApplication;
import com.turbo.silica.vpn
        .R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends AppCompatActivity {

    // This is an ad unit ID for a test ad. Replace with your own banner ad unit ID.
    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741";
    private FrameLayout adContainerView;
    private AdView adView;
    @BindView(R.id.parent)
    ConstraintLayout parent;

    public SharedPreferences getPrefs() {
        return getSharedPreferences(BuildConfig.SHARED_PREFS, Context.MODE_PRIVATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        Snackbar snackbar = Snackbar
                .make(parent, "Logging in, Please wait...", Snackbar.LENGTH_LONG);
        snackbar.show();

        loginUser();
        //
        // Initialize the Mobile Ads SDK.
    }
    private void loginUser() {
        //logging in

        try {

            ((MainApplication) getApplication()).setNewHostAndCarrier(Config.baseURL, Config.carrierID);
            AuthMethod authMethod = AuthMethod.anonymous();
            UnifiedSDK.getInstance().getBackend().login(authMethod, new Callback<User>() {
                @Override
                public void success(User user) {

                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

                @Override
                public void failure(VpnException e) {
                    Snackbar snackbar = Snackbar
                            .make(parent, "Authentication Error, Please try again.", Snackbar.LENGTH_INDEFINITE);

                    snackbar.setAction("Try again", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loginUser();
                        }
                    });
                    snackbar.show();
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
