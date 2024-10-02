package fi.nhg.paketti.cordova.plugin;

import android.os.Looper;
import android.os.Handler;
import android.util.Log;
import android.util.Base64;

import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionInfo;
import android.content.Context;
import android.telephony.TelephonyManager;

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

        // SubscriptionManager provides the number (fails silently if no permissions).        
        SubscriptionManager subscriptionManager = SubscriptionManager.from(this.cordova.getActivity().getApplicationContext());
        List<SubscriptionInfo> subsInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        Log.i(LOG_TAG, "Current telephony subscription list = " + subsInfoList);
        for (SubscriptionInfo subscriptionInfo : subsInfoList) {
            // String number = subscriptionInfo.getNumber(); // This method was deprecated in API level 33. 
            final String number = subscriptionManager.getPhoneNumber(subscriptionInfo.getSubscriptionId());
            Log.i(LOG_TAG, "Got subscription number (using SubscriptionManager#getPhoneNumber): " + number);
            if(number == null || number.isEmpty()) {
                Log.i(LOG_TAG, " Number is null or empty.");
                continue;
            } else {
                mPhone = number.replace("+358", "0"); // PhoneNumberUtils.formatNumberToE164()
                Log.i(LOG_TAG, " Number exist, mPhone set to: " + mPhone);
                break;                
            }
        }

        // As a fallback TelephonyManager provides the number (throws exception if no permissions). This method was deprecated in API level 33. 
        if(mPhone == null) {
            Log.i(LOG_TAG, "Finding using TelephonyManager#getLine1Number as a fallback.");
            try {
                TelephonyManager phoneMgr = (TelephonyManager)this.cordova.getActivity().getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                final String number = phoneMgr.getLine1Number();
                Log.i(LOG_TAG, "Got phone number (using TelephonyManager#getLine1Number): " + number);
                if(number == null || number.isEmpty()) {
                    Log.i(LOG_TAG, " Number is null or empty.");
                } else {
                    mPhone = number.replace("+358", "0"); // PhoneNumberUtils.formatNumberToE164()
                    Log.i(LOG_TAG, " Number exist, mPhone set to: " + mPhone);
                }
            } catch (Throwable t) {
                Log.i(LOG_TAG, "Exception when finding number (using elephonyManager#getLine1Number):", t);
            }
        }

        // Finally give up.
        if(mPhone == null) {
            Log.i(LOG_TAG, "No telephony number found. Make sure SIM-card is installed and application has sufficient permissions. Abort.");
        }
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