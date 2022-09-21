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
import com.turbo.silica.vpn.MainApplication;
import com.turbo.silica.vpn.PassServerData;
import com.turbo.silica.vpn.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FreeServersFragment extends Fragment {

    @BindView(R.id.freeServersRecyclerview)
    RecyclerView freeServersRecyclerview;

    private PassServerData mCallback;
    private FreeServersAdapter adapter = new FreeServersAdapter();
    private List<Country> list = new ArrayList<>();
    InterstitialAd mInterstitialAd;
    Country selectedCountry;

    public static Fragment createInstance()
    {
        FreeServersFragment freeServersFragment = new FreeServersFragment();
        return freeServersFragment;
    }

    public void setFreeServersList(List<Country> servers) {
        list = servers;
        adapter.notifyDataSetChanged();
    }

    public void registerCallback(PassServerData passServerData) {
        mCallback = passServerData;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_free_servers, container, false);
        ButterKnife.bind(this, view);
        freeServersRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        freeServersRecyclerview.setAdapter(adapter);

        return view;
    }

    class FreeServersAdapter extends RecyclerView.Adapter<FreeServersViewHolder> {

        @NonNull
        @Override
        public FreeServersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.region_list_item, parent, false);
            return new FreeServersViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FreeServersViewHolder holder, int position) {
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
    class FreeServersViewHolder extends RecyclerView.ViewHolder {

        private TextView mRegionTitle;
        private ImageView mRegionImage;
        private RelativeLayout mItemView;
        ImageView lock;

        public FreeServersViewHolder(@NonNull View itemView) {
            super(itemView);

            mRegionTitle = itemView.findViewById(R.id.region_title);
            mRegionImage = itemView.findViewById(R.id.region_image);
            mItemView = itemView.findViewById(R.id.itemView);
            lock = itemView.findViewById(R.id.locker);
            lock.setVisibility(View.GONE);
        }

        public void populateView(Country country) {
            Locale locale = new Locale("", country.getCountry());
            mRegionTitle.setText(locale.getDisplayCountry());
            mRegionImage.setImageResource(MainApplication.getStaticContext().getResources().getIdentifier("drawable/" + country.getCountry(), null, MainApplication.getStaticContext().getPackageName()));

            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedCountry = country;

                    if(getAdapterPosition() %3 == 0){
                        showInterstitial(getAdapterPosition());
                    }else{

                        mCallback.getSelectedServer(country);
                    }
                }

                private void showInterstitial(int adapterPosition) {
                }
            });

        }
    }


    public void loadAd() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();

        // Load ads into Interstitial Ads
      //  mInterstitialAd.loadAd(adRequest);

    }

   
    public void showAfterAd(int i){

        mCallback.getSelectedServer(selectedCountry);

    }


}