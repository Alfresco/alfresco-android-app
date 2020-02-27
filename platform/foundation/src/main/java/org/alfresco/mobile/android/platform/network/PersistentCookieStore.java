package org.alfresco.mobile.android.platform.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for cookies. CookieManager will store cookies of every incoming HTTP response into
 * CookieStore, and retrieve cookies for every outgoing HTTP request.
 * <p/>
 * Cookies are stored in {@link android.content.SharedPreferences} and will persist on the
 * user's device between application session. {@link com.google.gson.Gson} is used to serialize
 * the cookies into a json string in order to be able to save the cookie to
 * {@link android.content.SharedPreferences}team
 * <p/>
 */
public class PersistentCookieStore implements CookieStore
{
    private final static String PREF_DEFAULT_STRING = "";
    private final static String PREFS_NAME = PersistentCookieStore.class.getName();
    private final static String PREF_KEY_NAME = "data";

    private static final Object LOCK = new Object();
    private static PersistentCookieStore mSharedInstance;

    private CookieStore mStore;
    private Context mContext;

    private PersistentCookieStore()
    {
    }

    public static PersistentCookieStore getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mSharedInstance == null)
            {
                mSharedInstance = new PersistentCookieStore();
            }
            mSharedInstance.setContext(context);

            return mSharedInstance;
        }
    }


    private void setContext(Context context)
    {
        if (context != null)
        {
            mContext = context.getApplicationContext();
            init();
        }
    }

    private void init()
    {
        // get the default in memory store and if there is a cookie stored in shared preferences,
        // we added it to the cookie store
        mStore = new CookieManager().getCookieStore();
        String jsonSessionCookie = getJsonCookieString();
        if (!jsonSessionCookie.equals(PREF_DEFAULT_STRING))
        {
            Gson gson = new Gson();
            Type cookieListToken = new TypeToken<ArrayList<HttpCookie>>() {}.getType();

            try {
                List<HttpCookie> cookieList = gson.fromJson(jsonSessionCookie, cookieListToken);
                for (HttpCookie cookie : cookieList) {
                    mStore.add(URI.create(cookie.getDomain()), cookie);
                }
            } catch (Exception ex) {
                // may happen. creating an empty store.
            }
        }
    }

    @Override
    public void add(URI uri, HttpCookie cookie)
    {
        // Replace existing cookies of version 0
        URI domain = URI.create(cookie.getDomain());
        for (HttpCookie existingCookie : mStore.get(domain))
        {
            if (existingCookie.getName().equals(cookie.getName()) && cookie.getVersion() == 0)
            {
                mStore.remove(domain, existingCookie);
                break;
            }
        }
        mStore.add(domain, cookie);
        sync();
    }

    @Override
    public List<HttpCookie> get(URI uri)
    {
        return mStore.get(uri);
    }

    @Override
    public List<HttpCookie> getCookies()
    {
        return mStore.getCookies();
    }

    @Override
    public List<URI> getURIs()
    {
        return mStore.getURIs();
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie)
    {
        boolean result = mStore.remove(uri, cookie);
        sync();
        return result;
    }

    @Override
    public boolean removeAll()
    {
        boolean result = mStore.removeAll();
        sync();
        return result;
    }

    private String getJsonCookieString()
    {
        return getPrefs().getString(PREF_KEY_NAME, PREF_DEFAULT_STRING);
    }

    private void sync() {
        List<HttpCookie> cookies = mStore.getCookies();
        Gson gson = new Gson();
        String jsonCookies = gson.toJson(cookies);
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putString(PREF_KEY_NAME, jsonCookies);
        editor.apply();
    }

    private SharedPreferences getPrefs()
    {
        return mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
