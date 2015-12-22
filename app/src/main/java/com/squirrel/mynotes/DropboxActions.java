package com.squirrel.mynotes;

import android.content.Context;
import android.content.SharedPreferences;

import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

/**
 * Created by squirrel on 12/21/15.
 */
public class DropboxActions {

    /**
     * Dropbox authentification
     * @param session
     * @param context
     */
    public static void loadAuth(AndroidAuthSession session, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.ACCOUNT_PREFS_NAME,0);
        String key = prefs.getString(Constants.ACCESS_KEY_NAME, null);
        String secret = prefs.getString(Constants.ACCESS_SECRET_NAME, null);
        if(key == null || secret == null || key.length() == 0 || secret.length() == 0) {
            return;
        }
        //new or old of dropbox auth
        if(key.equals("oauth2:")) {
            //newer version
            session.setOAuth2AccessToken(secret);
        } else {
            session.setAccessTokenPair(new AccessTokenPair(key, secret));
        }
    }

    /**
     * Save auth
     * @param session
     * @param context
     */
    public static void storeAuth(AndroidAuthSession session, Context context) {
        String oAuth2AccessToken = session.getOAuth2AccessToken();
        if(oAuth2AccessToken != null) {
            saveAuth(context, "oauth2:", oAuth2AccessToken);
            return;
        }
        AccessTokenPair oAuth1AccessToken = session.getAccessTokenPair();
        if(oAuth1AccessToken != null) {
            saveAuth(context, oAuth1AccessToken.key, oAuth1AccessToken.secret);
        }
    }

    private static void saveAuth(Context context, String accessKey, String accessSecret) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.ACCOUNT_PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(Constants.ACCESS_KEY_NAME, accessKey);
        edit.putString(Constants.ACCESS_SECRET_NAME, accessSecret);
        edit.apply();

    }

    public static void clearKeys(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.ACCOUNT_PREFS_NAME,0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.apply();
    }

    public static AndroidAuthSession buildSession(Context context) {
        AppKeyPair appKeyPair = new AppKeyPair(Constants.APP_KEY, Constants.APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session, context);
        return session;
    }
}
