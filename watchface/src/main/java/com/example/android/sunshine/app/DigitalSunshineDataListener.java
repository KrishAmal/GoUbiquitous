package com.example.android.sunshine.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;

import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Created by Amal Krishnan on 20-11-2016.
 */

public class DigitalSunshineDataListener extends WearableListenerService {

    private static final String TEMP_KEY = "com.sunshine.temp";
    private static final String ICON_KEY = "com.sunshine.icon";
    private static final String TAG="Wear Listener";
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        ConnectionResult connectionResult =
                mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);

        if (!connectionResult.isSuccess()) {
            Log.e(TAG, "Failed to connect to GoogleApiClient.");
            return;
        }
        else
            Log.d(TAG, " Connected to GoogleApiClient.");


        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/temp") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                    String[] temp=dataMap.getString(TEMP_KEY).split(" ");
                    Asset iconAsset=dataMap.getAsset(ICON_KEY);
                    Bitmap bitmap=loadBitmapFromAsset(iconAsset);

                    String high=temp[0];
                    String low=temp[1];

                    DigitalSunshine.HIGH_TEMP=high;
                    DigitalSunshine.LOW_TEMP=low;
                    DigitalSunshine.ICON_BITMAP=bitmap;

                    Log.d(TAG, "onDataChanged: TEMP"+temp);
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result =
                mGoogleApiClient.blockingConnect(2000, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();
        mGoogleApiClient.disconnect();

        if (assetInputStream == null) {
            Log.w(TAG, "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }
}
