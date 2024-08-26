package fi.nhg.paketti.cordova.plugin;

import android.os.Looper;
import android.os.Handler;
import android.util.Log;
import android.util.Base64;

import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionInfo;
// import android.content.Context;
// import android.telephony.TelephonyManager;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import java.util.List;

import java.io.IOException;

// import android.app.AlertDialog;
// import android.content.DialogInterface;


public class CordovaPhonenumber extends CordovaPlugin {

    public static final String LOG_TAG = "PhonenumberPlugin";

    // private CallbackContext mCallbackContext = null;
    private String mPhone = null;


    /**
      * CVD Plugin initialization. Fetch phone number to member.
      *
      */
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        resolveTelephone();
    }


    private void resolveTelephone() {

        // Provides the number (fails silently if no permissions).
        SubscriptionManager subscriptionManager = SubscriptionManager.from(this.cordova.getActivity().getApplicationContext());
        List<SubscriptionInfo> subsInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        Log.d(LOG_TAG, "Current telephony subscription list = " + subsInfoList);
        for (SubscriptionInfo subscriptionInfo : subsInfoList) {
            String number = subscriptionInfo.getNumber();
            Log.i(LOG_TAG, "Founc subscription number:  " + number);
            if(number != null && !number.isEmpty()) {
                mPhone = number.replace("+358", "0");
                Log.i(LOG_TAG, "mPhone set to:  " + mPhone);
                break;
            }
        }
        if(mPhone == null)
            Log.i(LOG_TAG, "No telephony number found. Make sure SIM-card is installed and application has sufficient permissions");

        // Also provides the number but throws exception if no permissions.
        // TelephonyManager phoneMgr = (TelephonyManager) this.cordova.getActivity().getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        // Log.d(LOG_TAG, "[LINE NUMBER] -----------> getPhoneNumber(): " + phoneMgr.getLine1Number());
    }
    

    /**
      * CVD command interface.
      *
      */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.i(LOG_TAG, "action: " + action);
        if (action.equals("number")) {
            number(callbackContext);
            return true;
        }
        // TODO Send error event.
        return false;
    }

    /**
      * Send phone number (member set at initiation).
      *
      */
    private void number(CallbackContext callbackContext) {
        Log.w(LOG_TAG, "Returning phone: " + mPhone);
        JSONObject resultData = new JSONObject();
        try {
            resultData.put("phone", mPhone);
            PluginResult result = new PluginResult(PluginResult.Status.OK, resultData);
            callbackContext.sendPluginResult(result);
        } catch (Throwable e) {
            Log.w(LOG_TAG, "Throwable in synchronous invocation:\n" + Log.getStackTraceString(e));
            callbackContext.error(e.getClass().getName() + ":" + e.getMessage());
        }
    }
}