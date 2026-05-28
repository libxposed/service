package io.github.libxposed.service;

/**
 * Callback for asynchronous hot reload completion.
 */
interface IHotReloadCallback {
    /**
     * Called when hot reload completes or fails.
     */
    oneway void onHotReloadResult(int status, String message) = 1;
}
