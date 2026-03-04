package io.github.libxposed.service;

interface IXposedScopeCallback {
    oneway void onScopeRequestApproved(in List<String> approved) = 1;
    oneway void onScopeRequestFailed(String message) = 2;
}
