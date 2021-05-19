package io.github.xposed.xposedservice;

interface IXRemotePreferenceCallback {
    oneway void onUpdate(in Bundle map);
}
