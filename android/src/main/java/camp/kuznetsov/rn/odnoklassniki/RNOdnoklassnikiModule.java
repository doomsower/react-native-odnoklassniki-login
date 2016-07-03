package camp.kuznetsov.rn.odnoklassniki;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONObject;

import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkListener;
import ru.ok.android.sdk.Shared;
import ru.ok.android.sdk.util.OkAuthType;

public class RNOdnoklassnikiModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    private static final String LOG = "RNOdnoklassniki";
    private static final String E_LOGIN_ERROR = "E_LOGIN_ERROR";
    private static final String E_GET_USER_FAILED = "E_GET_USER_FAILED";

    private Odnoklassniki odnoklassniki;
    private String redirectUri;
    private Promise loginPromise;

    public RNOdnoklassnikiModule(final ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "RNOdnoklassniki";
    }

    @ReactMethod
    public void initialize(final String appId, final String appKey) {
        Log.d(LOG, "Inititalizing app " + appId + " with key " + appKey);
        odnoklassniki = Odnoklassniki.createInstance(getReactApplicationContext(), appId, appKey);
        redirectUri = "okauth://ok" + appId;
    }

    @ReactMethod
    public void login(final ReadableArray scope, final Promise promise) {
        int scopeSize = scope.size();
        final String[] scopeArray = new String[scopeSize];
        for (int i = 0; i < scopeSize; i++) {
            scopeArray[i] = scope.getString(i);
        }
        loginPromise = promise;
        odnoklassniki.checkValidTokens(new OkListener() {
            @Override
            public void onSuccess(JSONObject json) {
                loginPromise.resolve(getOKResult(json));
            }

            @Override
            public void onError(String error) {
                Log.d(LOG, "Valid token wasn't found at login, requesting authorization");
                odnoklassniki.requestAuthorization(getCurrentActivity(), redirectUri, OkAuthType.ANY, scopeArray);
            }
        });
    }

    @ReactMethod
    public void logout(Promise promise) {
        Log.d(LOG, "Logout");
        odnoklassniki.clearTokens();
        promise.resolve(null);
    }

    @ReactMethod
    public void isLoggedIn(final Promise promise) {
        Log.d(LOG, "Is logged in check");
        odnoklassniki.checkValidTokens(new OkListener() {
            @Override
            public void onSuccess(JSONObject json) {
                Log.d(LOG, "Is logged in");
                promise.resolve(getOKResult(json));
            }

            @Override
            public void onError(String error) {
                Log.d(LOG, "Is not logged in");
                promise.resolve(null);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Odnoklassniki.getInstance().isActivityRequestOAuth(requestCode)) {
            Odnoklassniki.getInstance().onAuthActivityResult(requestCode, resultCode, data, getAuthListener());
        }
    }

    @NonNull
    private OkListener getAuthListener() {
        return new OkListener() {
            @Override
            public void onSuccess(final JSONObject json) {
                Log.d(LOG, "Activity auth success");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //Do as "odnoklassniki.checkValidTokens" does
                            String userId = odnoklassniki.request("users.getLoggedInUser", "get");
                            Log.d(LOG, "User id found: " + userId);
                            if (userId != null && userId.length() > 2 && TextUtils.isDigitsOnly(userId.substring(1, userId.length() - 1))) {
                                json.put(Shared.PARAM_LOGGED_IN_USER, userId);
                                loginPromise.resolve(getOKResult(json));
                            } else
                                loginPromise.reject(E_GET_USER_FAILED, "users.getLoggedInUser returned bad value: " + userId);
                        } catch (Exception e) {
                            loginPromise.reject(E_GET_USER_FAILED, "users.getLoggedInUser failed: " + e.getLocalizedMessage());
                        }
                    }
                }).start();
            }

            @Override
            public void onError(String error) {
                Log.e(LOG, "OK Oauth error " + error);
                loginPromise.reject(E_LOGIN_ERROR, error);
            }
        };
    }

    private WritableMap getOKResult(JSONObject json){
        WritableMap result = Arguments.createMap();
        result.putString(Shared.PARAM_ACCESS_TOKEN, json.optString(Shared.PARAM_ACCESS_TOKEN));
        result.putString(Shared.PARAM_SESSION_SECRET_KEY, json.optString(Shared.PARAM_SESSION_SECRET_KEY));
        //Unquote user id
        result.putString(Shared.PARAM_LOGGED_IN_USER, json.optString(Shared.PARAM_LOGGED_IN_USER).replaceAll("\"", ""));
        result.putInt(Shared.PARAM_EXPIRES_IN, json.optInt(Shared.PARAM_EXPIRES_IN));
        return result;
    }

}
