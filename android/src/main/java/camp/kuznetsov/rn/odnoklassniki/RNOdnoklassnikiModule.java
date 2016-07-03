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
import ru.ok.android.sdk.OkRequestMode;
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
                Log.d(LOG, "Check valid token success");
                resolveWithCurrentUser(json.optString(Shared.PARAM_ACCESS_TOKEN), json.optString(Shared.PARAM_SESSION_SECRET_KEY));
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
                resolveWithCurrentUser(json.optString(Shared.PARAM_ACCESS_TOKEN), json.optString(Shared.PARAM_SESSION_SECRET_KEY));
            }

            @Override
            public void onError(String error) {
                Log.d(LOG, "OK Oauth error " + error);
                loginPromise.reject(E_LOGIN_ERROR, error);
            }
        };
    }

    private void resolveWithCurrentUser(final String accessToken, final String sessionSecretKey){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String userStr = odnoklassniki.request("users.getCurrentUser", "get");
                    JSONObject user = new JSONObject(userStr);
                    WritableMap result = Arguments.createMap();
                    result.putString(Shared.PARAM_ACCESS_TOKEN, accessToken);
                    result.putString(Shared.PARAM_SESSION_SECRET_KEY, sessionSecretKey);
                    result.putMap("user", JSONUtil.convertMap(user));
                    loginPromise.resolve(result);
                } catch (Exception e) {
                    loginPromise.reject(E_GET_USER_FAILED, "users.getLoggedInUser failed: " + e.getLocalizedMessage());
                }
            }
        }).start();
    }

}
