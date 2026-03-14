package moe.shizuku.manager.patch;

public class Util {
    public static String getNameByUid(int uid) {
		switch (uid) {
			case 0: return "root";
			case 1: return "daemon";
			case 2: return "bin";
			case 1000: return "system";
			case 1001: return "radio";
			case 1002: return "graphics";
			case 1003: return "input";
			case 1004: return "audio";
			case 1005: return "camera";
			case 1006: return "log";
			case 1007: return "compass";
			case 1008: return "mount";
			case 1009: return "wifi";
			case 1010: return "adb";
			case 1011: return "install";
			case 1012: return "media";
			case 1013: return "dhcp";
			case 1014: return "sdcard_rw";
			case 1015: return "vpn";
			case 1016: return "keystore";
			case 1017: return "usb";
			case 1018: return "drm";
			case 1019: return "mdnsr";
			case 1020: return "gps";
			case 1021: return "unused1";
			case 1022: return "media_rw";
			case 1023: return "mtp";
			case 1024: return "nfc";
			case 1025: return "drmrpc";
			case 1026: return "epm_rtc";
			case 1027: return "lock_settings";
			case 1028: return "credentials";
			case 1029: return "audioserver";
			case 1030: return "metrics_collector";
			case 1031: return "metricsd";
			case 1032: return "webservd";
			case 1033: return "debuggerd";
			case 1034: return "mediadrm";
			case 1035: return "diskread";
			case 1036: return "net_bt_admin";
			case 1037: return "nfc";
			case 1038: return "sensor";
			case 1039: return "mediadrm";
			case 1040: return "camerad";
			case 1041: return "print";
			case 1042: return "tether";
			case 1043: return "trustedui";
			case 1044: return "rild";
			case 1045: return "configstore";
			case 1046: return "wificond";
			case 1047: return "paccm";
			case 1048: return "ipacm";
			case 1049: return "neuralnetworks";
			case 1050: return "credstore";
			case 2000: return "shell";
		}
		return "unknown_" + uid;
	}
}

