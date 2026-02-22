package io.github.libxposed.service;

parcelable HookedProcess {
    int uid;
    int pid;
    String processName;
    boolean upToDate;
}
