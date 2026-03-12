package io.github.libxposed.service;

import android.content.SharedPreferences;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@SuppressWarnings("unused")
public final class XposedService {
    public final static class ServiceException extends RuntimeException {
        ServiceException(String message) {
            super(message);
        }

        ServiceException(RemoteException e) {
            super("Xposed service error", e);
        }
    }

    private final static Map<OnScopeEventListener, IXposedScopeCallback> scopeCallbacks = new HashMap<>();

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

    private final IXposedService mService;
    private final Map<String, RemotePreferences> mRemotePrefs = new HashMap<>();

    final ReentrantReadWriteLock deletionLock = new ReentrantReadWriteLock();

    XposedService(IXposedService service) {
        mService = service;
    }

    IXposedService asInterface() {
        return mService;
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
     * Get a list of currently running processes that are hooked by the module. Note that one app may
     * have multiple processes, and you should use uid instead of processName to identify apps.
     *
     * @return The list of hooked processes
     * @throws ServiceException If the service is dead or an error occurred
     */
    @NonNull
    public List<HookedProcess> getRunningTargets() {
        try {
            return mService.getRunningTargets();
        } catch (RemoteException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Get remote preferences from Xposed framework. If the group does not exist, it will be created.
     *
     * @param group Group name
     * @return The preferences
     * @throws ServiceException              If the service is dead or an error occurred
     * @throws UnsupportedOperationException If the framework is embedded
     */
    @NonNull
    public SharedPreferences getRemotePreferences(@NonNull String group) {
        return mRemotePrefs.computeIfAbsent(group, k -> {
            try {
                var instance = RemotePreferences.newInstance(this, k);
                if (instance == null) {
                    throw new ServiceException("Framework returns null");
                }
                return instance;
            } catch (RemoteException e) {
                if (e.getCause() instanceof UnsupportedOperationException cause) {
                    throw cause;
                }
                throw new ServiceException(e);
            }
        });
    }

    /**
     * Delete a group of remote preferences.
     *
     * @param group Group name
     * @throws ServiceException              If the service is dead or an error occurred
     * @throws UnsupportedOperationException If the framework is embedded
     */
    public void deleteRemotePreferences(@NonNull String group) {
        deletionLock.writeLock().lock();
        try {
            mService.deleteRemotePreferences(group);
            mRemotePrefs.computeIfPresent(group, (k, v) -> {
                v.setDeleted();
                return null;
            });
        } catch (RemoteException e) {
            if (e.getCause() instanceof UnsupportedOperationException cause) {
                throw cause;
            }
            throw new ServiceException(e);
        } finally {
            deletionLock.writeLock().unlock();
        }
    }

    /**
     * List all files in the module's shared data directory.
     *
     * @return The file list
     * @throws ServiceException              If the service is dead or an error occurred
     * @throws UnsupportedOperationException If the framework is embedded
     */
    @NonNull
    public String[] listRemoteFiles() {
        try {
            var files = mService.listRemoteFiles();
            if (files == null) throw new ServiceException("Framework returns null");
            return files;
        } catch (RemoteException e) {
            if (e.getCause() instanceof UnsupportedOperationException cause) {
                throw cause;
            }
            throw new ServiceException(e);
        }
    }

    /**
     * Open a file in the module's shared data directory. The file will be created if not exists.
     *
     * @param name File name, must not contain path separators and . or ..
     * @return The file descriptor
     * @throws ServiceException              If the service is dead or an error occurred
     * @throws UnsupportedOperationException If the framework is embedded
     */
    @NonNull
    public ParcelFileDescriptor openRemoteFile(@NonNull String name) {
        try {
            var file = mService.openRemoteFile(name);
            if (file == null) throw new ServiceException("Framework returns null");
            return file;
        } catch (RemoteException e) {
            if (e.getCause() instanceof UnsupportedOperationException cause) {
                throw cause;
            }
            throw new ServiceException(e);
        }
    }

    /**
     * Delete a file in the module's shared data directory.
     *
     * @param name File name, must not contain path separators and . or ..
     * @return true if successful, false if the file does not exist
     * @throws ServiceException              If the service is dead or an error occurred
     * @throws UnsupportedOperationException If the framework is embedded
     */
    public boolean deleteRemoteFile(@NonNull String name) {
        try {
            return mService.deleteRemoteFile(name);
        } catch (RemoteException e) {
            if (e.getCause() instanceof UnsupportedOperationException cause) {
                throw cause;
            }
            throw new ServiceException(e);
        }
    }
}
