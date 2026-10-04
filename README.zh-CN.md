[English](./README.md) | [**简体中文**](./README.zh-CN.md)

# System-Shizuku（非官方分支）

> **这不是官方 Shizuku。**
>
> 官方项目：<https://github.com/RikkaApps/Shizuku>
>
> 本分支不隶属于 RikkaApps，也未获得其背书或维护。
> 请不要把本分支的问题反馈给官方 Shizuku 项目。
> RikkaApps 有权让 owner 关闭/删除此项目

## 关于本分支

System-Shizuku 是面向高级用户和研究人员的 Shizuku 修改分支。

你必须使用 `ADB` 激活一次此 System-Shizuku ，才能使用 **CVE-2024-31317** 获取 `system` 权限

本分支可能使用 **CVE-2024-31317** 获取 `system` 权限，而不是只依赖 ADB 或 root。

获取到 `system` 权限后，可以尝试使用

```shell
setprop service.adb.tcp.port 5555 && settings put global adb_enabled 0 && settings put global adb_enabled 1
```

或

```shell
setprop service.adb.tcp.port 5555 && setprop ctl.restart adbd
```

启动无线调试

`system` 理论支持使用 `pm hide` 等命令

由于本分支使用不同的签名密钥，因此**无法覆盖安装官方 Shizuku**。你必须先卸载官方版本。

## 本分支的修改

- 使用 CVE-2024-31317 获取 `system` 权限。
- 放宽权限策略，允许在 uid < 10000 的进程上启动 Shizuku_Server。
- 添加 ProtectReceiver，自动清空 `hidden_api_blacklist_exemptions` 注入（可能无效）

## ⚠️ 安全警告

本分支仅供研究或学习，请勿用于非法用途。

- 可能仅适用于 **Android 9–13**，且安全补丁在 **2024-06 之前**的设备。
- 操作不当可能导致无法开机、系统不稳定或数据丢失。
- 部分情况下只能通过恢复出厂设置解决，这会清空全部数据。
- 请只在备用设备上测试，并提前备份数据。
- 开发者不对任何损坏或数据丢失负责。
- 部分使用 Shizuku 的应用能否使用取决于兼容性

## 背景

开发需要 root 的应用时，最常见的方法是在 `su` shell 中运行命令。例如，某个应用可能使用 `pm enable/disable` 来启用或禁用组件。

这种方法有很大缺点：

1. **极慢**——需要多次创建进程。
2. 需要解析文本输出——**非常不可靠**。
3. 受限于可用的 shell 命令。
4. 即使 ADB 有足够权限，应用仍可能需要 root 权限。

本分支采用与 Shizuku 类似的设计：通过一个特权服务进程和 Binder IPC，为应用提供更高权限的系统 API 访问能力。

## 下载与构建

本分支不发布到官方应用商店。

- Releases：<https://github.com/mx0341/Shizuku/releases>
- 从源码构建：见下方“开发 System-Shizuku 本身”。

官方 Shizuku 下载页，仅供参考：

- <https://shizuku.rikka.app/>

## 工作原理

首先，我们需要了解应用如何使用系统 API。例如，如果应用想获取已安装应用，通常应使用 `PackageManager#getInstalledPackages()`。这实际上是应用进程与系统服务进程之间的 IPC 过程，只是 Android 框架帮我们隐藏了细节。

Android 使用 `binder` 进行这类 IPC。`Binder` 允许服务端获知客户端的 uid 和 pid，因此系统服务可以检查应用是否有权限执行操作。

通常，如果存在给应用使用的“manager”，系统服务进程中就应该存在对应的“service”。如果应用持有该“service”的 `binder`，就可以与“service”通信。应用进程启动时会收到系统服务的 binder。

本分支会引导用户运行一个特权服务进程，类似 Shizuku。应用启动时，指向该服务进程的 `binder` 也会发送给应用。

其核心功能是充当中间人：接收来自应用的请求，转发给系统服务，再把结果返回。

这样就能以更高权限使用系统 API。对应用来说，这几乎与直接使用系统 API 相同。

## 开发者指南

### API 与示例

原始 API 参考：

- <https://github.com/RikkaApps/Shizuku-API>

### 注意事项

1. ADB 权限有限。

   ADB 权限有限，且不同 Android 版本差异较大。调用 API 前，请检查服务端是否有足够权限。

2. Android 9 起的隐藏 API 限制。

   从 Android 9 开始，普通应用使用隐藏 API 受到限制。请使用其他方法，例如 AndroidHiddenApiBypass。

3. Android 8.0 与 ADB。

   在某些版本上，ADB 缺少某些 observer 所需权限。如果你需要在可能不由 Activity 启动的进程中使用该服务，可以通过启动一个透明 Activity 来触发 binder 发送。

4. 直接使用 `transactRemote` 需要注意。

   不同 Android 版本下 API 可能不同，请仔细检查。另外，`SystemServiceHelper.getTransactionCode` 不一定总能获取正确的 transaction code。

## 开发 System-Shizuku 本身

### 构建

- 使用 `git clone --recurse-submodules` 克隆。
- 运行 Gradle 任务 `:manager:assembleDebug` 或 `:manager:assembleRelease`。

`:manager:assembleDebug` 任务会生成可调试的服务端。你可以把调试器附加到 `shizuku_server` 来调试服务端。在 Android Studio 中，应勾选 “Run/Debug configurations” 里的 “Always install with package manager”，以便服务端使用最新代码。

## 许可证

本项目中所有代码文件均使用 Apache 2.0 许可证。

本项目是 RikkaApps 的 Shizuku 的修改分支。

- 上游：<https://github.com/RikkaApps/Shizuku>
- Copyright (c) RikkaApps

根据 Apache 2.0 第 6 条以及上游项目的附加限制：

- **禁止**使用 `manager/src/main/res/mipmap*/ic_launcher*.png` 图像文件，除非用于展示 Shizuku 本身。
- **禁止**使用 `Shizuku` 作为应用名，禁止使用 `moe.shizuku.privileged.api` 作为 application id，禁止声明 `moe.shizuku.manager.permission.*` 权限。