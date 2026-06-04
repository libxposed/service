package io.github.libxposed.service;

/**
 * Information about a process currently hooked by this module.
 */
parcelable HookedProcess {
    /**
     * The target is running the currently installed module code.
     */
    const int TARGET_STATE_UP_TO_DATE = 0;

    /**
     * The target is still running old module code and may be hot-reloaded.
     */
    const int TARGET_STATE_STALE = 1;

    /**
     * The target is currently being hot-reloaded.
     */
    const int TARGET_STATE_RELOADING = 2;

    /**
     * The target's last hot reload attempt failed because the old module refused reload or reload
     * raised an exception.
     */
    const int TARGET_STATE_FAILED = 3;

    /**
     * Opaque identifier assigned by the framework. Module apps must only pass this value back to
     * the service and must not infer ordering, lifetime, or process identity from it.
     */
    long targetId;

    /**
     * The process uid, provided for display and diagnostics.
     */
    int uid;

    /**
     * The process id, provided for display and diagnostics. It must not be used as target identity.
     */
    int pid;

    /**
     * The Android process name, provided for display and diagnostics.
     */
    String processName;

    /**
     * One of TARGET_STATE_*.
     */
    int state;

    /**
     * Version code of the module package loaded in this process. This is only a diagnostic value;
     * the framework may use a stronger internal code identity to determine state.
     */
    long loadedVersionCode;
}
