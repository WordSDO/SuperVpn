package com.turbo.silica.vpn.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anchorfree.partner.api.data.Country;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.turbo.silica.vpn.Helpers.Config;
import com.turbo.silica.vpn.MainApplication;
import com.turbo.silica.vpn.PassServerData;
import com.turbo.silica.vpn.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VipServersFragment extends Fragment {

    @BindView(R.id.freeServersRecyclerview)
    RecyclerView VIPServersRecyclerview;
    Country selectedCountry;
    InterstitialAd mInterstitialAd;
    private PassServerData mCallback;
    private VIPServersAdapter adapter = new VIPServersAdapter();
    private List<Country> list = new ArrayList<>();

    public static Fragment createInstance() {
        VipServersFragment vipServersFragment = new VipServersFragment();
        return vipServersFragment;
    }

    public void registerCallback(PassServerData passServerData) {
        mCallback = passServerData;
    }

    public void setVIPServersList(List<Country> servers) {
        list = servers;
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vip_servers, container, false);
        ButterKnife.bind(this, view);

        VIPServersRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        VIPServersRecyclerview.setAdapter(adapter);

//        mInterstitialAd = new InterstitialAd(getContext());
//        mInterstitialAd.setAdUnitId(getString(R.string.admob_interstitial));
//        loadAd();

        return view;
    }

    public void upgrade_vpn(String name, Country country) {

        mCallback.getSelectedServer(country);


    }

    public void loadAd() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();

        // Load ads into Interstitial Ads
      //  mInterstitialAd.loadAd(adRequest);

    }

//    private void initAndLoadInterstitialAds(final int j) {
//
//
//        mInterstitialAd.setAdListener(new AdListener() {
//            public void onAdLoaded() {
//
//            }
//
//            @Override
//            public void onAdClosed() {
//                super.onAdClosed();
//                showAfterAd(j);
//
//                loadAd();
//            }
//
//            @Override
//            public void onAdFailedToLoad(LoadAdError var1) {
//                super.onAdFailedToLoad(var1);
//
//                showAfterAd(j);
//
//                loadAd();
//            }
//        });


    
//
//    private void showInterstitial(int i) {
//        if (mInterstitialAd.isLoaded()) {
//            mInterstitialAd.show();
//            initAndLoadInterstitialAds(i);
//        } else {
//            showAfterAd(i);
//            loadAd();
//        }
//    }

    public void showAfterAd(int i) {

        mCallback.getSelectedServer(selectedCountry);

    }

    class VIPServersAdapter extends RecyclerView.Adapter<VIPServersViewHolder> {

        @NonNull
        @Override
        public VIPServersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.region_list_item, parent, false);
            return new VIPServersViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VIPServersViewHolder holder, int position) {
            holder.populateView(list.get(position));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    /**
     * RecyclerView viewHolder
     */
    class VIPServersViewHolder extends RecyclerView.ViewHolder {

        ImageView locker;
        private TextView mRegionTitle;
        private ImageView mRegionImage;
        //private RelativeLayout mLockLayout;
        private RelativeLayout mItemView;

        public VIPServersViewHolder(@NonNull View itemView) {
            super(itemView);

            mRegionTitle = itemView.findViewById(R.id.region_title);
            mRegionImage = itemView.findViewById(R.id.region_image);
            //   mLockLayout = itemView.findViewById(R.id.lockLayout);
            mItemView = itemView.findViewById(R.id.itemView);
            locker = itemView.findViewById(R.id.locker);
        }

        public void populateView(Country country) {

            if (!Config.servers_subscription) {
                locker.setVisibility(View.GONE);
            } else {
                locker.setVisibility(View.GONE);

            }

            Locale locale = new Locale("", country.getCountry());
            mRegionTitle.setText(locale.getDisplayCountry());
            mRegionImage.setImageResource(MainApplication.getStaticContext().getResources().getIdentifier("drawable/" + country.getCountry(), null, MainApplication.getStaticContext().getPackageName()));

            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedCountry = country;

                    if (getAdapterPosition() % 3 == 0) {
                        showInterstitial(getAdapterPosition());
                    } else {

                        upgrade_vpn(locale.getDisplayCountry(), country);
                    }

                }

                private void showInterstitial(int adapterPosition) {
                }
            });

        }
    }


}