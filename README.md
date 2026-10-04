[**English**](./README.md) | [简体中文](./README.zh-CN.md)

# System-Shizuku (Unofficial Fork)

> **This is NOT the official Shizuku.**
>
> Official project: <https://github.com/RikkaApps/Shizuku>
>
> This fork is not affiliated with, endorsed by, or maintained by RikkaApps.
> Do not report issues of this fork to the official Shizuku project.
> RikkaApps reserves the right to have the owner close/delete this project.

## About this fork

System-Shizuku is a modified fork of Shizuku for advanced users and researchers.

You must activate this System-Shizuku once using ADB before you can use CVE-2024-31317 to obtain system privileges.

This fork may use **CVE-2024-31317** to obtain `system` privileges instead of relying only on ADB or root.

After obtaining `system` privileges, you can try to enable wireless debugging with:

```shell
setprop service.adb.tcp.port 5555 && settings put global adb_enabled 0 && settings put global adb_enabled 1
```

or

```shell
setprop service.adb.tcp.port 5555 && setprop ctl.restart adbd
```

`system` theoretically supports commands such as `pm hide`.

Because this fork uses a different signing key, it **cannot be installed over the official Shizuku**. You must uninstall the official version first.

## Changes in this fork

- Uses CVE-2024-31317 to obtain `system` privileges.
- Relaxes permission policy to allow starting Shizuku_Server on processes with uid < 10000.
- Adds ProtectReceiver to automatically clear `hidden_api_blacklist_exemptions` injection (may be ineffective).

## ⚠️ Security Warning

This fork is for research and learning only. Do not use it for illegal purposes.

- It may only work on **Android 9–13** with security patch **before 2024-06**.
- Incorrect use may cause bootloop, system instability, or data loss.
- In some cases, recovery may require a factory reset, which will erase all data.
- Test only on a backup device. Back up your data first.
- The developer is not responsible for any damage or data loss.
- Whether some apps that use Shizuku can work depends on compatibility.

## Background

When developing apps that require root, the most common method is to run some commands in the `su` shell. For example, an app may use `pm enable/disable` to enable or disable components.

This method has major disadvantages:

1. **Extremely slow** — multiple process creation.
2. Needs to parse text output — **super unreliable**.
3. Limited to available shell commands.
4. Even if ADB has sufficient permissions, the app may still require root privileges.

This fork follows a design similar to Shizuku: it uses a privileged server process and Binder IPC to provide higher-privilege system API access to apps.

## Download & Build

This fork does not publish to official app stores.

- Releases: <https://github.com/mx0341/Shizuku/releases>
- Build from source: see “Developing System-Shizuku itself” below.

Official Shizuku download page, for reference only:

- <https://shizuku.rikka.app/>

## How does it work?

First, we need to talk about how apps use system APIs. For example, if an app wants to get installed apps, we normally use `PackageManager#getInstalledPackages()`. This is actually an IPC process between the app process and the system server process; the Android framework hides the details.

Android uses `binder` for this type of IPC. `Binder` allows the server side to learn the uid and pid of the client side, so the system server can check whether the app has permission to perform the operation.

Usually, if there is a “manager” for apps to use, there should be a “service” in the system server process. If the app holds the `binder` of the “service”, it can communicate with the “service”. The app process receives binders of system services on start.

This fork guides users to run a privileged server process, similar to Shizuku. When the app starts, the `binder` to the server will also be sent to the app.

The key feature is acting as a middleman: receiving requests from the app, sending them to the system server, and returning the results.

So the goal is reached: use system APIs with higher permission. To the app, it is almost identical to using system APIs directly.

## Developer guide

### API & sample

Original API reference:

- <https://github.com/RikkaApps/Shizuku-API>

### Attention

1. ADB permissions are limited.

   ADB has limited permissions and they differ across Android versions. Before calling the API, check whether the server has sufficient permissions.

2. Hidden API limitation from Android 9.

   As of Android 9, usage of hidden APIs is limited for normal apps. Use other methods, such as AndroidHiddenApiBypass.

3. Android 8.0 & ADB.

   On some versions, ADB lacks permissions for certain observers. If you need to use the service in a process that may not be started by an Activity, trigger the binder send by starting a transparent activity.

4. Direct use of `transactRemote` requires attention.

   The API may differ under different Android versions. Check carefully. Also, `SystemServiceHelper.getTransactionCode` may not always get the correct transaction code.

## Developing System-Shizuku itself

### Build

- Clone with `git clone --recurse-submodules`
- Run gradle task `:manager:assembleDebug` or `:manager:assembleRelease`

The `:manager:assembleDebug` task generates a debuggable server. You can attach a debugger to `shizuku_server` to debug the server. In Android Studio, check “Always install with package manager” in “Run/Debug configurations”, so the server uses the latest code.

## License

All code files in this project are licensed under Apache 2.0.

This project is a modified fork of Shizuku by RikkaApps.

- Upstream: <https://github.com/RikkaApps/Shizuku>
- Copyright (c) RikkaApps

Under Apache 2.0 section 6, and the upstream project’s additional restrictions:

- You are **FORBIDDEN** to use `manager/src/main/res/mipmap*/ic_launcher*.png` image files, unless for displaying Shizuku itself.
- You are **FORBIDDEN** to use `Shizuku` as app name, use `moe.shizuku.privileged.api` as application id, or declare `moe.shizuku.manager.permission.*` permission.