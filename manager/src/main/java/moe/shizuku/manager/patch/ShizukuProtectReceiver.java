package moe.shizuku.manager.patch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

public class ShizukuProtectReceiver extends BroadcastReceiver {
	private final String action_shutdown = Intent.ACTION_SHUTDOWN;
    private final String action_reboot = Intent.ACTION_REBOOT;
	
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Util.isSecurityPatchUpToDate()) return;
		String action = intent.getAction();
        if (action_shutdown.equals(action) || action_reboot.equals(action)) {
			try {
				Settings.Global.putString(context.getContentResolver(), "hidden_api_blacklist_exemptions", "");
			} catch (Exception e) {
				Settings.Global.putString(context.getContentResolver(), "hidden_api_blacklist_exemptions", "");
			}
		}
    }
}