package org.twizer.android.io.net.provider.geo;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * @author Jorge Antonio Diaz-Benito Soriano (github.com/Stoyicker).
 */
public final class CasualLocationProvider implements GoogleApiClient.ConnectionCallbacks {

    private Location mLastKnownLocation;
    private static final Object LOCK = new Object();
    private static volatile CasualLocationProvider mInstance;
    private GoogleApiClient mGoogleApiClient;

    public static CasualLocationProvider getInstance(final Context context) {
        CasualLocationProvider ret = mInstance;
        if (mInstance == null)
            synchronized (LOCK) {
                if (mInstance == null) {
                    ret = new CasualLocationProvider(context);
                    mInstance = ret;
                }
            }

        return ret;
    }

    private CasualLocationProvider(final Context context) {
        buildGoogleApiClient(context);
    }

    private synchronized void buildGoogleApiClient(final Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(final Bundle bundle) {
        mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(final int i) {
    }

    @Nullable
    public Location getLastKnownLocation() {
        if (mLastKnownLocation == null && mGoogleApiClient.isConnected()) {
            mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        return mLastKnownLocation;
    }
}
