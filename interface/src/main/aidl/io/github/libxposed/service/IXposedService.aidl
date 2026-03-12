package io.github.libxposed.service;
import io.github.libxposed.service.IXposedScopeCallback;

interface IXposedService {
    const String AUTHORITY_SUFFIX = ".XposedService";
    const String SEND_BINDER = "SendBinder";

    /**
     * The API version of this <b>library</b>. This is a static value for the framework.
     * Modules should use {@link #getApiVersion()} to check the API version at runtime.
     */
    const int LIB_API = 101;

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
    // api 102 roadmap
    // List<HookedProcess> getRunningTargets() = 13;

    // remote preference utilities
    Bundle requestRemotePreferences(String group) = 20;
    void updateRemotePreferences(String group, in Bundle diff) = 21;
    void deleteRemotePreferences(String group) = 22;

    // remote file utilities
    String[] listRemoteFiles() = 30;
    ParcelFileDescriptor openRemoteFile(String name) = 31;
    boolean deleteRemoteFile(String name) = 32;
}
