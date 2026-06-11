package io.github.libxposed.service;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.github.libxposed.annotation.SinceApi;

@SuppressWarnings("unused")
public final class XposedService {
    /**
     * API version 101.
     */
    public static final int API_101 = IXposedService.API_101;

    /**
     * API version 102.
     * <p>API additions:</p>
     * <ul>
     * <li>Running hooked targets can be queried.</li>
     * <li>Hot reload can be requested for a hooked target when permitted by the framework.</li>
     * </ul>
     */
    public static final int API_102 = IXposedService.API_102;

    /**
     * The framework has the capability to hook system_server and other system processes.
     */
    public static final long PROP_CAP_SYSTEM = IXposedService.PROP_CAP_SYSTEM;

    /**
     * The framework provides remote preferences and remote files support.
     */
    public static final long PROP_CAP_REMOTE = IXposedService.PROP_CAP_REMOTE;

    /**
     * The framework disallows accessing Xposed API via reflection or dynamically loaded code.
     */
    public static final long PROP_RT_API_PROTECTION = IXposedService.PROP_RT_API_PROTECTION;

    public static final class ServiceException extends RuntimeException {
        ServiceException(String message) {
            super(message);
        }

        ServiceException(RemoteException e) {
            super("Xposed service error", e);
        }
    }

    private static final Map<OnScopeEventListener, IXposedScopeCallback> scopeCallbacks = new ConcurrentHashMap<>();
    private static final Set<IHotReloadCallback> hotReloadCallbacks = ConcurrentHashMap.newKeySet();

    /**
     * Callback interface for module scope request.
     */
    public interface OnScopeEventListener {
        /**
         * Callback when the request is approved.
         *
         * @param approved Approved packages for the request
         */
        default void onScopeRequestApproved(@NonNull List<String> approved) {
        }

        /**
         * Callback when the request is failed.
         *
         * @param message Error message
         */
        default void onScopeRequestFailed(@NonNull String message) {
        }

        private IXposedScopeCallback asInterface() {
            return scopeCallbacks.computeIfAbsent(this, (listener) -> new IXposedScopeCallback.Stub() {
                @Override
                public void onScopeRequestApproved(List<String> approved) {
                    listener.onScopeRequestApproved(approved);
                    scopeCallbacks.remove(listener);
                }

                @Override
                public void onScopeRequestFailed(String message) {
                    listener.onScopeRequestFailed(message);
                    scopeCallbacks.remove(listener);
                }
            });
        }
    }

    /**
     * Callback interface for hot reload requests.
     */
    @SinceApi(API_102)
    public interface HotReloadCallback {
        /**
         * Called when hot reload completes or fails.
         * <p>
         * This callback may run on a Binder thread. Dispatch to the main thread before touching UI.
         * </p>
         *
         * @param target The target process passed to
         *               {@link #hotReloadModule(HookedTarget, Bundle, HotReloadCallback)}
         * @param result The hot reload result
         */
        void onHotReloadResult(@NonNull HookedTarget target, @NonNull HotReloadResult result);
    }

    private final IXposedService mService;
    private final Map<String, RemotePreferences> mRemotePrefs = new HashMap<>();

    XposedService(IXposedService service) {
        mService = service;
    }

    IXposedService asInterface() {
        return mService;
    }

    private void requireApi(int api) {
        if (getApiVersion() < api) {
            throw new UnsupportedOperationException("Requires Xposed service API " + api);
        }
    }

    /**
     * Get the Xposed API version of current implementation.
     *
     * @return API version
     * @throws ServiceException If the service is dead or an error occurred
     */
    public int getApiVersion() {
        try {
            return mService.getApiVersion();
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Get the Xposed framework name of current implementation.
     *
     * @return Framework name
     * @throws ServiceException If the service is dead or an error occurred
     */
    @NonNull
    public String getFrameworkName() {
        try {
            return mService.getFrameworkName();
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Get the Xposed framework version of current implementation.
     *
     * @return Framework version
     * @throws ServiceException If the service is dead or an error occurred
     */
    @NonNull
    public String getFrameworkVersion() {
        try {
            return mService.getFrameworkVersion();
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Get the Xposed framework version code of current implementation.
     *
     * @return Framework version code
     * @throws ServiceException If the service is dead or an error occurred
     */
    public long getFrameworkVersionCode() {
        try {
            return mService.getFrameworkVersionCode();
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the Xposed framework properties.
     * Properties with prefix PROP_RT_ may change among launches.
     *
     * @return Framework properties
     * @throws ServiceException If the service is dead or an error occurred
     */
    public long getFrameworkProperties() {
        try {
            return mService.getFrameworkProperties();
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Get the application scope of current module.
     *
     * @return Module scope
     * @throws ServiceException If the service is dead or an error occurred
     */
    @NonNull
    public List<String> getScope() {
        try {
            return mService.getScope();
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Request to add a new app to the module scope.
     *
     * @param packages Packages to be added
     * @param callback Callback to be invoked when the request is completed or error occurred
     * @throws ServiceException If the service is dead or an error occurred
     */
    public void requestScope(@NonNull List<String> packages, @NonNull OnScopeEventListener callback) {
        try {
            mService.requestScope(packages, callback.asInterface());
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Remove an app from the module scope.
     *
     * @param packages Packages to be removed
     * @throws ServiceException If the service is dead or an error occurred
     */
    public void removeScope(@NonNull List<String> packages) {
        try {
            mService.removeScope(packages);
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Get a list of currently running processes that are hooked by the module.
     * <p>
     * Returned targets can be passed to
     * {@link #hotReloadModule(HookedTarget, Bundle, HotReloadCallback)}. The pid, uid, process
     * name, and loaded version code are diagnostic values only.
     * </p>
     *
     * @return The list of hooked processes
     * @throws UnsupportedOperationException If the framework does not support service API 102
     * @throws ServiceException              If the service is dead or an error occurred
     */
    @SinceApi(API_102)
    @NonNull
    public List<HookedTarget> getRunningTargets() {
        requireApi(API_102);
        try {
            var processes = mService.getRunningTargets();
            if (processes == null) throw new ServiceException("Framework returns null");
            var targets = new ArrayList<HookedTarget>(processes.size());
            for (var process : processes) {
                if (process == null) throw new ServiceException("Framework returns null target");
                targets.add(toHookedTarget(process));
            }
            return Collections.unmodifiableList(targets);
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Request hot reload for the module in the specified process. The process must be one of the
     * targets returned by {@link #getRunningTargets()}.
     * <p>
     * This method only validates and submits the request. The actual reload result is delivered
     * asynchronously through {@code callback}. If the framework cannot provide a valid new module
     * generation for the target, the callback receives {@link HotReloadResult.Status#UNSUPPORTED}.
     * </p>
     * <p>
     * If the old module rejects reload by returning {@code false} from {@code onHotReloading},
     * the callback receives {@link HotReloadResult.Status#FAILED} with a null message. If reload
     * fails because of an exception, the callback receives {@link HotReloadResult.Status#FAILED}
     * with a framework-provided diagnostic message.
     * </p>
     * <p>
     * Hot reload is intended for loading a new module generation after the module app is updated.
     * It should not be used to propagate configuration changes. For configuration updates, use
     * {@link #getRemotePreferences(String)} and
     * {@link SharedPreferences.OnSharedPreferenceChangeListener}.
     * </p>
     * <p>
     * The optional data should contain only classloader-neutral values that can be unmarshalled
     * without the module's class loader. Do not put module-defined
     * {@link android.os.Parcelable} or {@link java.io.Serializable} objects in this bundle.
     * </p>
     *
     * @param target   The target process
     * @param data     Optional data to be passed to the old module
     * @param callback Callback to be invoked when the request completes or fails
     * @throws ServiceException  If the service is dead or an error occurred
     * @throws SecurityException If the target is invalid or no longer belongs to this module
     */
    @SinceApi(API_102)
    public void hotReloadModule(@NonNull HookedTarget target, @Nullable Bundle data,
                                @NonNull HotReloadCallback callback) {
        requireApi(API_102);
        Objects.requireNonNull(target);
        Objects.requireNonNull(callback);
        var remoteCallback = new IHotReloadCallback.Stub() {
            @Override
            public void onHotReloadResult(int code, String message) {
                try {
                    callback.onHotReloadResult(target, HotReloadResult.from(code, message));
                } finally {
                    hotReloadCallbacks.remove(this);
                }
            }
        };
        hotReloadCallbacks.add(remoteCallback);
        try {
            mService.hotReloadModule(target.mTargetId, data, remoteCallback);
        } catch (RemoteException e) {
            hotReloadCallbacks.remove(remoteCallback);
            throw new ServiceException(e);
        } catch (RuntimeException e) {
            hotReloadCallbacks.remove(remoteCallback);
            throw e;
        }
    }

    private static HookedTarget toHookedTarget(HookedProcess process) {
        if (process.processName == null) {
            throw new ServiceException("Framework returns target with null processName");
        }
        return new HookedTarget(
                process.targetId,
                process.uid,
                process.pid,
                process.processName,
                toHookedTargetState(process.state),
                process.loadedVersionCode
        );
    }

    private static HookedTarget.State toHookedTargetState(int state) {
        return switch (state) {
            case HookedProcess.TARGET_STATE_UP_TO_DATE -> HookedTarget.State.UP_TO_DATE;
            case HookedProcess.TARGET_STATE_STALE -> HookedTarget.State.STALE;
            case HookedProcess.TARGET_STATE_RELOADING -> HookedTarget.State.RELOADING;
            case HookedProcess.TARGET_STATE_FAILED -> HookedTarget.State.FAILED;
            default -> throw new ServiceException("Invalid hooked target state: " + state);
        };
    }

    /**
     * Get remote preferences from Xposed framework. If the group does not exist, it will be created.
     *
     * @param group Group name
     * @return The preferences
     * @throws ServiceException              If the service is dead or an error occurred
     * @throws UnsupportedOperationException If the framework does not have remote capability
     */
    @NonNull
    public synchronized SharedPreferences getRemotePreferences(@NonNull String group) {
        return mRemotePrefs.computeIfAbsent(group, k -> {
            try {
                return RemotePreferences.newInstance(this, k);
            } catch (RemoteException e) {
                throw new ServiceException(e);
            }
        });
    }

    /**
     * Delete a group of remote preferences.
     *
     * @param group Group name
     * @throws ServiceException              If the service is dead or an error occurred
     * @throws UnsupportedOperationException If the framework does not have remote capability
     */
    public synchronized void deleteRemotePreferences(@NonNull String group) {
        try {
            var prefs = mRemotePrefs.get(group);
            if (prefs != null) prefs.onDelete();
            mService.deleteRemotePreferences(group);
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * List all files in the module's shared data directory.
     *
     * @return The file list
     * @throws ServiceException              If the service is dead or an error occurred
     * @throws UnsupportedOperationException If the framework does not have remote capability
     */
    @NonNull
    public String[] listRemoteFiles() {
        try {
            var files = mService.listRemoteFiles();
            if (files == null) throw new ServiceException("Framework returns null");
            return files;
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Open a file in the module's shared data directory. The file will be created if not exists.
     *
     * @param name File name, must not contain path separators and . or ..
     * @return The file descriptor
     * @throws ServiceException              If the service is dead or an error occurred
     * @throws UnsupportedOperationException If the framework does not have remote capability
     */
    @NonNull
    public ParcelFileDescriptor openRemoteFile(@NonNull String name) {
        try {
            var file = mService.openRemoteFile(name);
            if (file == null) throw new ServiceException("Framework returns null");
            return file;
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Delete a file in the module's shared data directory.
     *
     * @param name File name, must not contain path separators and . or ..
     * @return true if successful, false if the file does not exist
     * @throws ServiceException              If the service is dead or an error occurred
     * @throws UnsupportedOperationException If the framework does not have remote capability
     */
    public boolean deleteRemoteFile(@NonNull String name) {
        try {
            return mService.deleteRemoteFile(name);
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
    }
}
