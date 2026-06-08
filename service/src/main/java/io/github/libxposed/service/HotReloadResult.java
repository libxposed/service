package io.github.libxposed.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.libxposed.annotation.SinceApi;

/**
 * Result of an asynchronous hot reload request.
 *
 * @param status  The completion status
 * @param message Optional framework-provided diagnostic message. For {@link Status#FAILED}, a
 *                null message means the old module refused reload by returning {@code false} from
 *                {@code onHotReloading}; a non-null message describes a reload exception.
 */
@SinceApi(XposedService.API_102)
public record HotReloadResult(@NonNull Status status, @Nullable String message) {
    /**
     * Hot reload completion status.
     */
    public enum Status {
        /**
         * Hot reload completed successfully.
         */
        SUCCEEDED,

        /**
         * The old module refused reload, or hot reload raised an exception.
         * <p>
         * When the old module refuses reload by returning {@code false} from
         * {@code onHotReloading}, {@link HotReloadResult#message()} is null. When reload fails
         * because of an exception, the message contains a framework-provided diagnostic string.
         * </p>
         */
        FAILED,

        /**
         * The target does not support hot reload.
         * <p>
         * For example, this can be returned for modules that do not declare exactly one Java
         * entry class or targets for which the framework cannot provide a valid new module
         * generation.
         * </p>
         */
        UNSUPPORTED,

        /**
         * The target is already being hot-reloaded.
         */
        IN_PROGRESS,

        /**
         * The target process died before hot reload could complete.
         */
        PROCESS_DIED
    }

    static HotReloadResult from(int code, @Nullable String message) {
        var status = switch (code) {
            case IXposedService.HOT_RELOAD_SUCCEEDED -> Status.SUCCEEDED;
            case IXposedService.HOT_RELOAD_FAILED -> Status.FAILED;
            case IXposedService.HOT_RELOAD_UNSUPPORTED -> Status.UNSUPPORTED;
            case IXposedService.HOT_RELOAD_IN_PROGRESS -> Status.IN_PROGRESS;
            case IXposedService.HOT_RELOAD_PROCESS_DIED -> Status.PROCESS_DIED;
            default -> throw new XposedService.ServiceException("Invalid hot reload status code: " + code);
        };
        return new HotReloadResult(status, message);
    }
}
