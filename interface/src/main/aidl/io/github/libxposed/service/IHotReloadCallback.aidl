package io.github.libxposed.service;

/**
 * Callback for asynchronous hot reload completion.
 */
interface IHotReloadCallback {
    /**
     * Called when hot reload completes or fails.
     *
     * @param status The raw hot reload status
     * @param message Optional diagnostic message; null for module-refused reloads
     */
    oneway void onHotReloadResult(int status, String message) = 1;
}
