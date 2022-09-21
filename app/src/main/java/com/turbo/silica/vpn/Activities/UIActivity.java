package com.turbo.silica.vpn.Activities;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.anchorfree.partner.api.response.RemainingTraffic;
import com.anchorfree.sdk.UnifiedSDK;
import com.anchorfree.vpnsdk.callbacks.Callback;
import com.anchorfree.vpnsdk.exceptions.VpnException;
import com.anchorfree.vpnsdk.vpnservice.VPNState;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;

import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.navigation.NavigationView;
import com.infideap.drawerbehavior.AdvanceDrawerLayout;
import com.turbo.silica.vpn.Fragments.ServersFragment;
import com.turbo.silica.vpn.Helpers.Config;
import com.turbo.silica.vpn.Helpers.Converter;
import com.turbo.silica.vpn.Helpers.Helper;
import com.turbo.silica.vpn.MainActivity;
import com.turbo.silica.vpn.MainApplication;
import com.turbo.silica.vpn.R;

import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public abstract class UIActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected static final String TAG = MainActivity.class.getSimpleName();
    protected static final String HELPER_TAG = "Helper";


    protected Toolbar toolbar;
    @BindView(R.id.connect_btn)
    TextView vpn_connect_btn;
    @BindView(R.id.vpn_connection_block)
    LinearLayout conblock;
    @BindView(R.id.vpn_connection_time)
    TextView vpn_connection_time;

    UnifiedNativeAd nativeAd;

    @BindView(R.id.drawer_opener)
    ImageView Drawer_opener_image;

    private AdvanceDrawerLayout drawer;
    /*google ads*/
    //  private UnifiedNativeAd nativeAd;
    private InterstitialAd mInterstitialAd;
    private Handler mUIHandler = new Handler(Looper.getMainLooper());
    final Runnable mUIUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateUI();
            checkRemainingTraffic();
            mUIHandler.postDelayed(mUIUpdateRunnable, 10000);
        }
    };

    private void initSpeedTest() {


    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_drawer);
        ButterKnife.bind(this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setupDrawer();
        //
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                //mediation code
                Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
                for (String adapterClass : statusMap.keySet()) {
                    AdapterStatus status = statusMap.get(adapterClass);
                    Log.d("MyApp", String.format(
                            "Adapter name: %s, Description: %s, Latency: %d",
                            adapterClass, status.getDescription(), status.getLatency()));
                }//end

            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();
        //
        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");

                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                Log.d("TAG", "The ad was dismissed.");


                            }





                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when fullscreen content failed to show.
                                Log.d("TAG", "The ad failed to show.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                mInterstitialAd = null;
                                Log.d("TAG", "The ad was shown.");

                            }
//                            @Override
//                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                                // Handle the error
//                                Log.i(TAG, loadAdError.getMessage());
//                                mInterstitialAd = null;
//                            }


                        });


                    }});
        //

    }

    @Override
    protected void onResume() {
        super.onResume();


        isConnected(new Callback<Boolean>() {
            @Override
            public void success(@NonNull Boolean aBoolean) {
                if (aBoolean) {
                    startUIUpdateTask();
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });

//        load_reward_video();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //  load_reward_video();
        stopUIUpdateTask();
    }

    protected abstract void isLoggedIn(Callback<Boolean> callback);

    protected abstract void loginToVpn();

    protected abstract void logOutFromVnp();

    @OnClick(R.id.go_pro)
    public void go_pro_click() {
        //startActivity(new Intent(UIActivity.this, PurchasesActivity.class));
    }

    @OnClick(R.id.vpn_select_country)
    public void showRegionDialog() {
        //RegionChooserDialog.newInstance().show(getSupportFragmentManager(), RegionChooserDialog.TAG);

        ServersFragment.newInstance().show(getSupportFragmentManager(), ServersFragment.TAG);

//        startActivity(new Intent(UIActivity.this, ServersActivity.class));

    }

    @OnClick(R.id.share_app_link)
    public void shareAppClick() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "share app");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "I'm using Super Vpn App, it's provide all servers free https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName());
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception e) {
        }
    }

    @OnClick(R.id.connect_btn)
    public void onConnectBtnClick(View v) {
        vpn_connection_time.setText(R.string.connecting);
        vpn_connection_time.setVisibility(View.VISIBLE);

        //   vpn_connection_state.setVisibility(View.VISIBLE);

        isConnected(new Callback<Boolean>() {
            @Override
            public void success(@NonNull Boolean aBoolean) {

                if (aBoolean) {

                    disconnectFromVnp();

                } else {
                    if (mInterstitialAd != null) {
                        mInterstitialAd.show(UIActivity.this);
                    } else {
                        isConnected(new Callback<Boolean>() {
                            @Override
                            public void success(@NonNull Boolean aBoolean) {

                                if (aBoolean) {

                                    disconnectFromVnp();


                                } else {
                                    connectToVpn();
                                }
                            }

                            @Override
                            public void failure(@NonNull VpnException e) {

                            }
                        });
                    }
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });


    }
//

    //

    protected abstract void isConnected(Callback<Boolean> callback);

    protected abstract void connectToVpn();

    protected abstract void disconnectFromVnp();

    protected abstract void chooseServer();

    protected abstract void getCurrentServer(Callback<String> callback);

    protected void startUIUpdateTask() {
        stopUIUpdateTask();
        mUIHandler.post(mUIUpdateRunnable);
    }

    protected void stopUIUpdateTask() {
        mUIHandler.removeCallbacks(mUIUpdateRunnable);
        updateUI();
    }

    protected abstract void checkRemainingTraffic();

    protected void updateUI() {
        UnifiedSDK.getVpnState(new Callback<VPNState>() {
            @Override
            public void success(@NonNull VPNState vpnState) {


                switch (vpnState) {
                    case IDLE: {

                        vpn_connection_time.setVisibility(View.VISIBLE);
                        vpn_connection_time.setText("Not Connected");
                        vpn_connection_time.setTextColor(ContextCompat.getColor(UIActivity.this, R.color.maroonClr));

                        vpn_connect_btn.setText("START");

                        conblock.setBackgroundResource(R.drawable.mystyle);

                        //    connectionStateTextView.setText(R.string.disconnected);
                        hideConnectProgress();
//                        vpn_connect_btn.setImageResource(R.drawable.ic_connect_vpn);

                        Helper.printLog("idle");
                        break;
                    }
                    case CONNECTED: {
                        //  vpn_connection_state.setVisibility(View.GONE);
                        vpn_connection_time.setVisibility(View.VISIBLE);
                        vpn_connection_time.setText("Connected");
                        vpn_connection_time.setTextColor(ContextCompat.getColor(UIActivity.this, R.color.darkBlueClr));

                        vpn_connect_btn.setText("STOP");
                        conblock.setBackgroundResource(R.drawable.connected);


                        // rippleBackground.startRippleAnimation();
                        //rippleBackground.setVisibility(View.VISIBLE);
                        //   connectionStateTextView.setText(R.string.connected);
                        hideConnectProgress();
                        Helper.printLog("connected");
                        break;
                    }
                    case CONNECTING_VPN:
                        //    vpn_connection_time.setText(R.string.connecting);
                        //vpn_connection_time.setVisibility(View.VISIBLE);
                        //  vpn_connection_time.setTextColor(ContextCompat.getColor(UIActivity.this,R.color.errorcolor));

                        //rippleBackground.stopRippleAnimation();
                        //rippleBackground.setVisibility(View.INVISIBLE);
                    case CONNECTING_CREDENTIALS:
                    case CONNECTING_PERMISSIONS: {
                        showConnectProgress();
                        break;
                    }
                    case PAUSED: {
                        vpn_connection_time.setText(R.string.paused);
                        vpn_connection_time.setTextColor(ContextCompat.getColor(UIActivity.this, R.color.maroonClr));

                        Helper.printLog("paused");
                        //rippleBackground.startRippleAnimation();
                        //  rippleBackground.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });
        UnifiedSDK.getInstance().getBackend().isLoggedIn(new Callback<Boolean>() {
            @Override
            public void success(@NonNull Boolean isLoggedIn) {

                //make connect button enabled
            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });

        getCurrentServer(new Callback<String>() {
            @Override
            public void success(@NonNull final String currentServer) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Locale locale = new Locale("", currentServer);
                        //  selectedServerTextView.setText(currentServer != null ? locale.getDisplayCountry() : "UNKNOWN");
                    }
                });
            }

            @Override
            public void failure(@NonNull VpnException e) {
                // selectedServerTextView.setText("UNKNOWN");
            }
        });

    }

    protected void updateTrafficStats(long outBytes, long inBytes) {
        String outString = Converter.humanReadableByteCountOld(outBytes, false);
        String inString = Converter.humanReadableByteCountOld(inBytes, false);

        //uploading_speed_textview.setText(inString);
        // downloading_speed_textview.setText(outString);
    }

    protected void updateRemainingTraffic(RemainingTraffic remainingTrafficResponse) {
        if (remainingTrafficResponse.isUnlimited()) {
        } else {
            String trafficUsed = Converter.megabyteCount(remainingTrafficResponse.getTrafficUsed()) + "Mb";
            String trafficLimit = Converter.megabyteCount(remainingTrafficResponse.getTrafficLimit()) + "Mb";

        }
    }

    protected void showLoginProgress() {
    }

    protected void hideLoginProgress() {
    }

    protected void showConnectProgress() {
        // connectionStateTextView.setVisibility(View.VISIBLE);
    }

    protected void hideConnectProgress() {
        //  connectionStateTextView.setVisibility(View.VISIBLE);


    }

    protected void showMessage(String msg) {
        Toast.makeText(UIActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    private void handleUserLogin() {
        ((MainApplication) getApplication()).setNewHostAndCarrier(Config.baseURL, Config.carrierID);
        loginToVpn();
    }

    private void setupDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                UIActivity.this, drawer, null, 0, 0);//R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(UIActivity.this);

        initSpeedTest();
    }

    @OnClick(R.id.drawer_opener)
    public void OpenDrawer(View v) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuitem) {


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        // Handle navigation view item clicks here.
//        int id = menuitem.getItemId();
        switch (menuitem.getItemId()) {
            case R.id.nav_upgrade:
//            upgrade application is available...
//                RegionChooserDialog.newInstance().show(getSupportFragmentManager(), RegionChooserDialog.TAG);
                ServersFragment.newInstance().show(getSupportFragmentManager(), ServersFragment.TAG);
                break;

            case R.id.nav_helpus:
//            find help about the application
               /* Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.developer_email)});
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.help_to_improve_us_email_subject));
                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.help_to_improve_us_body));

                try {
                    startActivity(Intent.createChooser(intent, "send mail"));
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(this, "No mail app found!!!", Toast.LENGTH_SHORT);
                } catch (Exception ex) {
                    Toast.makeText(this, "Unexpected Error!!!", Toast.LENGTH_SHORT);
                }*/

                showFeedbacDialog();
                break;
            case R.id.nav_rate:
//            rate application...
                rateUs();
                break;

            case R.id.nav_share:
//            share the application...
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "share app");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "I'm using Super VPN App, it's provide all servers free https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName());
                    startActivity(Intent.createChooser(shareIntent, "choose one"));
                } catch (Exception e) {
                }
                break;


            case R.id.nav_about:
                showAboutDialog();
                break;

            case R.id.nav_policy:
                Uri uri = Uri.parse(getResources().getString(R.string.privacy_policy_link)); // missing 'http://' will cause crashed
                Intent intent_policy = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent_policy);
                break;
        }
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);


        return true;
    }

    private void showAboutDialog() {
        Dialog about_dialog = new Dialog(this);

        about_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        about_dialog.setContentView(R.layout.dialog_about);
        about_dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(about_dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        ((ImageButton) about_dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                about_dialog.dismiss();
            }
        });

        about_dialog.show();
        about_dialog.getWindow().setAttributes(lp);
    }

    protected void rateUs() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.rating_dialog_layout);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        TextView poitiveButton = dialog.findViewById(R.id.dialog_rating_button_positive);
        TextView negativeButton = dialog.findViewById(R.id.dialog_rating_button_negative);


        poitiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getApplicationContext().getPackageName()));
                startActivity(intent);

            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    private void showFeedbacDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_feedback);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView poitiveButton = dialog.findViewById(R.id.dialog_rating_button_feedback_submit);
        TextView negativeButton = dialog.findViewById(R.id.dialog_rating_button_feedback_cancel);
        EditText editText = dialog.findViewById(R.id.dialog_rating_feedback);

        poitiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (editText.getText().toString().isEmpty()) {
                    Helper.showToast(UIActivity.this, "Please explain the issue");
                } else {
                    // go to email
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:"));
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.developer_email)});
                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_email_subject));
                    intent.putExtra(Intent.EXTRA_TEXT, editText.getText().toString());

                    try {
                        startActivity(Intent.createChooser(intent, "Send E-mail"));
                    } catch (ActivityNotFoundException ex) {
                        Helper.showToast(UIActivity.this, "No email app found.");
                    } catch (Exception ex) {
                        Helper.showToast(UIActivity.this, "Network Error");
                    }
                }
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }




}
