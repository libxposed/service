package io.github.libxposed.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public record HotReloadResult(@NonNull Status status, @Nullable String message) {
    public enum Status {
        SUCCESS,
        FAILED,
        IN_PROGRESS,
        PROCESS_DIED
    }

    static HotReloadResult from(int code, @Nullable String message) {
        var status = switch (code) {
            case IXposedService.HOT_RELOAD_SUCCESS -> Status.SUCCESS;
            case IXposedService.HOT_RELOAD_FAILED -> Status.FAILED;
            case IXposedService.HOT_RELOAD_IN_PROGRESS -> Status.IN_PROGRESS;
            case IXposedService.HOT_RELOAD_PROCESS_DIED -> Status.PROCESS_DIED;
            default -> throw new XposedService.ServiceException("Invalid hot reload status code: " + code);
        };
        return new HotReloadResult(status, message);
    }
}
