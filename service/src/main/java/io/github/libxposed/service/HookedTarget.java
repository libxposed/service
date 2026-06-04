package io.github.libxposed.service;

import androidx.annotation.NonNull;

import java.util.Objects;

import io.github.libxposed.annotation.SinceApi;

/**
 * Information about a process currently hooked by this module.
 * <p>Instances are snapshot handles and do not define stable value equality.</p>
 */
@SinceApi(XposedService.API_102)
@SuppressWarnings("unused")
public final class HookedTarget {
    /**
     * State of a hooked target.
     */
    public enum State {
        /**
         * The target is running the currently installed module code.
         */
        UP_TO_DATE,

        /**
         * The target is still running old module code and may be hot-reloaded.
         */
        STALE,

        /**
         * The target is currently being hot-reloaded.
         */
        RELOADING,

        /**
         * The target's last hot reload attempt failed because the old module refused reload or
         * reload raised an exception.
         */
        FAILED
    }

    final long mTargetId;
    private final int mUid;
    private final int mPid;
    private final String mProcessName;
    private final State mState;
    private final long mLoadedVersionCode;

    HookedTarget(long targetId, int uid, int pid, @NonNull String processName,
                 @NonNull State state, long loadedVersionCode) {
        mTargetId = targetId;
        mUid = uid;
        mPid = pid;
        mProcessName = Objects.requireNonNull(processName);
        mState = Objects.requireNonNull(state);
        mLoadedVersionCode = loadedVersionCode;
    }

    /**
     * Gets the process uid, provided for display and diagnostics.
     */
    public int getUid() {
        return mUid;
    }

    /**
     * Gets the process id, provided for display and diagnostics.
     * It must not be used as target identity.
     */
    public int getPid() {
        return mPid;
    }

    /**
     * Gets the Android process name, provided for display and diagnostics.
     */
    @NonNull
    public String getProcessName() {
        return mProcessName;
    }

    /**
     * Gets the target state.
     */
    @NonNull
    public State getState() {
        return mState;
    }

    /**
     * Gets the version code of the module package loaded in this process.
     * This is only a diagnostic value; the framework may use a stronger internal code identity to
     * determine state.
     */
    public long getLoadedVersionCode() {
        return mLoadedVersionCode;
    }
}
