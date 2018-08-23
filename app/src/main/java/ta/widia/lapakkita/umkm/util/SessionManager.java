package ta.widia.lapakkita.umkm.util;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import ta.widia.lapakkita.umkm.LoginUmkm;

/**
 * Created by taufik on 29/05/18.
 */

public class SessionManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "LapakKitaUMKMPref";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    public static final String KEY_ID_UMKM = "key_id_umkm";
    public static final String KEY_NM_PMLK = "key_nm_pmlk";
    public static final String KEY_NM_UMKM = "key_nm_umkm";
    public static final String KEY_NOKTP = "key_noktp";
    public static final String KEY_ALAMAT = "key_alamat";
    public static final String KEY_NOHP = "key_nohp";
    public static final String KEY_EMAIL = "key_email";
    public static final String KEY_DESK = "key_desk";
    public static final String KEY_LOGO = "key_logo";
    public static final String KEY_LAT = "key_lat";
    public static final String KEY_LON = "key_lon";

    // Constructor
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     */

    public void createLoginSession(String id_umkm,
                                   String nm_pmlk,
                                   String nm_umkm,
                                   String noktp,
                                   String alamat,
                                   String nohp,
                                   String email,
                                   String desk,
                                   String logo,
                                   String lat,
                                   String lon) {
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_ID_UMKM, id_umkm);
        editor.putString(KEY_NM_PMLK, nm_pmlk);
        editor.putString(KEY_NM_UMKM, nm_umkm);
        editor.putString(KEY_NOKTP, noktp);
        editor.putString(KEY_ALAMAT, alamat);
        editor.putString(KEY_NOHP, nohp);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_DESK, desk);
        editor.putString(KEY_LOGO, logo);
        editor.putString(KEY_LAT, lat);
        editor.putString(KEY_LON, lon);

        // commit changes
        editor.commit();
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     */
    public void checkLogin() {
        // Check login status
        if (!this.isLoggedIn()) {
            //Anda belum login

            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginUmkm.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);

        }
        else{
            //Anda sudah login
        }

    }


    /**
     * Get stored session data
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();

        user.put(KEY_ID_UMKM, pref.getString(KEY_ID_UMKM, null));
        user.put(KEY_NM_PMLK, pref.getString(KEY_NM_PMLK, null));
        user.put(KEY_NM_UMKM, pref.getString(KEY_NM_UMKM, null));
        user.put(KEY_NOKTP, pref.getString(KEY_NOKTP, null));
        user.put(KEY_ALAMAT, pref.getString(KEY_ALAMAT, null));
        user.put(KEY_NOHP, pref.getString(KEY_NOHP, null));
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        user.put(KEY_DESK, pref.getString(KEY_DESK, null));
        user.put(KEY_LOGO, pref.getString(KEY_LOGO, null));
        user.put(KEY_LAT, pref.getString(KEY_LAT, null));
        user.put(KEY_LON, pref.getString(KEY_LON, null));
        // return user
        return user;
    }

    /**
     * Clear session details
     */
    public void logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
        Intent i = new Intent(_context, LoginUmkm.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }

    /**
     * Quick check for login
     **/
    // Get Login State
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }
}
