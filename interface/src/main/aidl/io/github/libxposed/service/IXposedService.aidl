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
     * <p>Behavior changes: Modules targeting 102 or higher</p>
     * <ul>
     * <li>Running hooked targets can be queried.</li>
     * <li>Hot reload can be requested for a hooked target when permitted by the framework.</li>
     * </ul>
     */
    const int API_102 = 102;
    /**
     * The API version of this <b>library</b>. This is a static value for the framework.
     * Modules should use {@link #getApiVersion()} to check the API version at runtime.
     */
    const int LIB_API = API_102;

    /**
     * The framework has the capability to hook system_server and other system processes.
     */
    const long PROP_CAP_SYSTEM = 1L;
    /**
     * The framework provides remote preferences and remote files support.
     */
    const long PROP_CAP_REMOTE = 1L << 1;
    /**
     * The framework disallows accessing Xposed API via reflection or dynamically loaded code.
     */
    const long PROP_RT_API_PROTECTION = 1L << 2;
    /**
     * The framework permits hot reloading.
     */
    const long PROP_RT_HOT_RELOAD = 1L << 3;

    /**
     * Hot reload completed successfully.
     */
    const int HOT_RELOAD_SUCCESS = 0;

    /**
     * Hot reload failed or was refused before completion.
     */
    const int HOT_RELOAD_FAILED = 1;

    /**
     * The target is already being hot-reloaded.
     */
    const int HOT_RELOAD_IN_PROGRESS = 2;

    /**
     * The target process died before hot reload could complete.
     */
    const int HOT_RELOAD_PROCESS_DIED = 3;

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
     *
     * @throws SecurityException if the target id is invalid, no longer belongs to this module, or
     *                           hot reload is denied by framework policy
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
