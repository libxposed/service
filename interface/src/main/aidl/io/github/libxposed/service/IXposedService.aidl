package io.github.libxposed.service;
import io.github.libxposed.service.HookedProcess;
import io.github.libxposed.service.IXposedScopeCallback;

interface IXposedService {
    const String AUTHORITY_SUFFIX = ".XposedService";
    const String SEND_BINDER = "SendBinder";

    // framework details
    int getAPIVersion() = 1;
    String getFrameworkName() = 2;
    String getFrameworkVersion() = 3;
    long getFrameworkVersionCode() = 4;
    int getFrameworkCapabilities() = 5;

    // scope utilities
    List<String> getScope() = 10;
    oneway void requestScope(String packageName, IXposedScopeCallback callback) = 11;
    String removeScope(String packageName) = 12;
    List<HookedProcess> getRunningTargets() = 13;

    // remote preference utilities
    Bundle requestRemotePreferences(String group) = 20;
    void updateRemotePreferences(String group, in Bundle diff) = 21;
    void deleteRemotePreferences(String group) = 22;

    // remote file utilities
    String[] listRemoteFiles() = 30;
    ParcelFileDescriptor openRemoteFile(String name) = 31;
    boolean deleteRemoteFile(String name) = 32;
}
