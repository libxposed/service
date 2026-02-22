package io.github.libxposed.service;

parcelable HookedProcess {
    String packageName;
    int uid;
    int pid;
    boolean upToDate;
}
