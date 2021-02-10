package io.github.xposed.xposedservice;

import android.os.IBinder;

public class XposedService {
    private static IBinder service_binder = null;
    private static IXposedService service = null;

    public static IXposedService getService() {
        if (service_binder != null && service != null) {
            service = IXposedService.Stub.asInterface(service_binder);
        }
        return service;
    }
}
