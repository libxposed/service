package io.github.xposed.xposedservice;

interface IXposedService {
    int getVersion() = 1;
    List<PackageInfo> getInstalledPackagesFromAllUsers(int flags) = 2;
}
