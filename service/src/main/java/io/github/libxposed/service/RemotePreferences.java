package io.github.libxposed.service;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("unchecked")
public final class RemotePreferences implements SharedPreferences {

    private static final String TAG = "RemotePreferences";
    private static final Object CONTENT = new Object();
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    static volatile boolean shouldNotifyCleared = false;

    private final XposedService mService;
    private final String mGroup;
    private final Map<OnSharedPreferenceChangeListener, Object> mListeners = Collections.synchronizedMap(new WeakHashMap<>());

    private volatile Map<String, Object> mMap;

    private RemotePreferences(XposedService service, String group) {
        this.mService = service;
        this.mGroup = group;
    }

    @NonNull
    static RemotePreferences newInstance(XposedService service, String group) throws RemoteException {
        var output = service.asInterface().requestRemotePreferences(group);
        if (output == null) throw new RemoteException("Framework returns null");
        var prefs = new RemotePreferences(service, group);
        var map = (Map<String, Object>) output.getSerializable("map");
        if (map != null) prefs.mMap = Collections.unmodifiableMap(map);
        else prefs.mMap = Collections.emptyMap();
        return prefs;
    }

    synchronized void onDelete() {
        mMap = Collections.emptyMap();
    }

    @Override
    public Map<String, ?> getAll() {
        return new TreeMap<>(mMap);
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        return (String) mMap.getOrDefault(key, defValue);
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        return (Set<String>) mMap.getOrDefault(key, defValues);
    }

    @Override
    public int getInt(String key, int defValue) {
        var v = mMap.get(key);
        return v != null ? (int) v : defValue;
    }

    @Override
    public long getLong(String key, long defValue) {
        var v = mMap.get(key);
        return v != null ? (long) v : defValue;
    }

    @Override
    public float getFloat(String key, float defValue) {
        var v = mMap.get(key);
        return v != null ? (float) v : defValue;
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        var v = mMap.get(key);
        return v != null ? (boolean) v : defValue;
    }

    @Override
    public boolean contains(String key) {
        return mMap.containsKey(key);
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mListeners.put(listener, CONTENT);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mListeners.remove(listener);
    }

    @Override
    public Editor edit() {
        return new Editor();
    }

    public class Editor implements SharedPreferences.Editor {

        private final HashSet<String> mDelete = new HashSet<>();
        private final HashMap<String, Object> mPut = new HashMap<>();
        private boolean mClear = false;

        private void put(String key, @NonNull Object value) {
            mDelete.remove(key);
            mPut.put(key, value);
        }

        @Override
        public SharedPreferences.Editor putString(String key, @Nullable String value) {
            if (value == null) remove(key);
            else put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor putStringSet(String key, @Nullable Set<String> values) {
            if (values == null) remove(key);
            else put(key, values);
            return this;
        }

        @Override
        public SharedPreferences.Editor putInt(String key, int value) {
            put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor putLong(String key, long value) {
            put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor putFloat(String key, float value) {
            put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor putBoolean(String key, boolean value) {
            put(key, value);
            return this;
        }

        @Override
        public SharedPreferences.Editor remove(String key) {
            mDelete.add(key);
            mPut.remove(key);
            return this;
        }

        @Override
        public SharedPreferences.Editor clear() {
            mClear = true;
            mDelete.clear();
            mPut.clear();
            return this;
        }

        private void doUpdate() {
            synchronized (RemotePreferences.this) {
                var newMap = new HashMap<>(mMap);
                if (mClear) newMap.clear();
                mDelete.forEach(newMap::remove);
                newMap.putAll(mPut);
                mMap = Collections.unmodifiableMap(newMap);
            }
            // Snapshot listeners to avoid ConcurrentModificationException
            // Collections.synchronizedMap does not synchronize iteration
            List<OnSharedPreferenceChangeListener> listeners;
            synchronized (mListeners) {
                listeners = new ArrayList<>(mListeners.keySet());
            }
            for (var listener : listeners) {
                if (mClear && shouldNotifyCleared) {
                    listener.onSharedPreferenceChanged(RemotePreferences.this, null);
                }
                for (var key : mDelete) {
                    listener.onSharedPreferenceChanged(RemotePreferences.this, key);
                }
                for (var key : mPut.keySet()) {
                    listener.onSharedPreferenceChanged(RemotePreferences.this, key);
                }
            }
        }

        private Bundle buildCommitBundle() {
            if (!mClear && mDelete.isEmpty() && mPut.isEmpty()) return null;
            var bundle = new Bundle();
            bundle.putBoolean("clear", mClear);
            bundle.putSerializable("delete", new HashSet<>(mDelete));
            bundle.putSerializable("put", new HashMap<>(mPut));
            return bundle;
        }

        private boolean doCommit(Bundle bundle) {
            if (bundle != null) {
                try {
                    mService.asInterface().updateRemotePreferences(mGroup, bundle);
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to commit changes to framework", e);
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean commit() {
            var bundle = buildCommitBundle();
            if (bundle == null) return true;
            doUpdate();
            return doCommit(bundle);
        }

        @Override
        public void apply() {
            var bundle = buildCommitBundle();
            if (bundle == null) return;
            doUpdate();
            EXECUTOR.execute(() -> doCommit(bundle));
        }
    }
}
