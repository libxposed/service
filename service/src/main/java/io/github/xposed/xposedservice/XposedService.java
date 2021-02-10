package io.github.xposed.xposedservice;

import android.os.IBinder;
import android.os.RemoteException;

public class XposedService implements IXposedService {
    private static IBinder service_binder = null;
    private static IXposedService service = null;
    private static XposedService instance = null;

    public static XposedService getService() {
        if (service_binder != null && service != null) {
            service = IXposedService.Stub.asInterface(service_binder);
        }
        if (instance == null) {
            instance = new XposedService();
        }
        return instance;
    }

    @Override
    public int getVersion() throws RemoteException {
        return service.getVersion();
    }

    @Override
    public IBinder asBinder() {
        return service_binder;
    }
}
