package io.github.libxposed.service;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public final class XposedProvider extends ContentProvider {

    private static final String TAG = "XposedProvider";

    @Override
    public boolean onCreate() {
        var targetSdk = Objects.requireNonNull(getContext()).getApplicationInfo().targetSdkVersion;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && targetSdk >= Build.VERSION_CODES.R) {
            RemotePreferences.shouldNotifyCleared = true;
        }
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        if (method.equals(IXposedService.SEND_BINDER) && extras != null) {
            IBinder binder = extras.getBinder("binder");
            if (binder != null) {
                Log.d(TAG, "binder received: " + binder);
                XposedServiceHelper.onBinderReceived(binder);
            }
            return new Bundle();
        }
        return null;
    }
}
