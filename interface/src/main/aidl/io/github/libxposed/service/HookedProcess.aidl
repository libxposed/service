package io.github.libxposed.service;

parcelable HookedProcess {
    String packageName;
    int pid;
    int uid;
    boolean upToDate;
}
