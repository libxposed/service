package io.github.libxposed.service;

import io.github.libxposed.service.HookedProcess;
import io.github.libxposed.service.IHotReloadCallback;
import io.github.libxposed.service.IXposedScopeCallback;

interface IXposedService {
    const String AUTHORITY_SUFFIX = ".XposedService";
    const String SEND_BINDER = "SendBinder";

    /**
     * API version 101.
     */
    const int API_101 = 101;
    /**
     * API version 102.
     */
    const int API_102 = 102;
    /**
     * API version implemented by this interface.
     */
    const int LIB_API = API_102;

    /**
     * Framework property bit: system process hooking is supported.
     */
    const long PROP_CAP_SYSTEM = 1L;
    /**
     * Framework property bit: remote preferences and files are supported.
     */
    const long PROP_CAP_REMOTE = 1L << 1;
    /**
     * Framework property bit: runtime API protection is enforced.
     */
    const long PROP_RT_API_PROTECTION = 1L << 2;
    /**
     * Raw hot reload status: succeeded.
     */
    const int HOT_RELOAD_SUCCEEDED = 0;
    /**
     * Raw hot reload status: module refused reload or reload raised an exception.
     */
    const int HOT_RELOAD_FAILED = 1;
    /**
     * Raw hot reload status: unsupported.
     */
    const int HOT_RELOAD_UNSUPPORTED = 2;
    /**
     * Raw hot reload status: target already reloading.
     */
    const int HOT_RELOAD_IN_PROGRESS = 3;
    /**
     * Raw hot reload status: target process died.
     */
    const int HOT_RELOAD_PROCESS_DIED = 4;

    // framework details
    int getApiVersion() = 1;
    String getFrameworkName() = 2;
    String getFrameworkVersion() = 3;
    long getFrameworkVersionCode() = 4;
    long getFrameworkProperties() = 5;

    // scope utilities
    List<String> getScope() = 10;
    oneway void requestScope(in List<String> packages, IXposedScopeCallback callback) = 11;
    void removeScope(in List<String> packages) = 12;

    /**
     * Returns running processes currently hooked by this module. Returned target ids are opaque,
     * process-scoped tokens and may become invalid after this call returns.
     */
    List<HookedProcess> getRunningTargets() = 13;

    /**
     * Requests hot reload for a target returned by getRunningTargets(). Implementations should
     * validate and enqueue the request promptly, then report completion through the callback.
     * If hot reload is unsupported for the module or target, implementations should report
     * HOT_RELOAD_UNSUPPORTED through the callback.
     *
     * @throws SecurityException if the target id is invalid or no longer belongs to this module
     */
    void hotReloadModule(long targetId, in Bundle data, IHotReloadCallback callback) = 14;

    // remote preference utilities
    Bundle requestRemotePreferences(String group) = 20;
    void updateRemotePreferences(String group, in Bundle diff) = 21;
    void deleteRemotePreferences(String group) = 22;

    // remote file utilities
    String[] listRemoteFiles() = 30;
    ParcelFileDescriptor openRemoteFile(String name) = 31;
    boolean deleteRemoteFile(String name) = 32;
}
