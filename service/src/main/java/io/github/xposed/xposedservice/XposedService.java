package io.github.xposed.xposedservice;

import android.os.IBinder;
import android.os.RemoteException;

public class XposedService implements IXposedService {
    private static IBinder serviceBinder = null;
    private static IXposedService service = null;
    private static XposedService instance = null;

    public static XposedService getService() {
        if (serviceBinder != null && service == null) {
            service = IXposedService.Stub.asInterface(serviceBinder);
        }
        if (instance == null) {
            instance = new XposedService();
        }
        return service == null ? null : instance;
    }

    @Override
    public int getVersion() throws RemoteException {
        return service.getVersion();
    }

    @Override
    public IBinder asBinder() {
        return serviceBinder;
    }
}
