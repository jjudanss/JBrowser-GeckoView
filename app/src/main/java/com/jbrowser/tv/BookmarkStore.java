package com.jbrowser.tv;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.GCMParameterSpec;

/** Local bookmarks. Private bookmarks are AES-GCM encrypted with an Android Keystore key. */
public final class BookmarkStore {
    public static final class Bookmark {
        public final String title;
        public final String url;
        public Bookmark(String title, String url) { this.title = title; this.url = url; }
    }

    private static final String PREFS = "jbrowser_bookmarks";
    private static final String PUBLIC_KEY = "public_list";
    private static final String PRIVATE_KEY = "private_list_encrypted";
    private static final String PIN_SALT = "private_pin_salt";
    private static final String PIN_HASH = "private_pin_hash";
    private static final String KEY_ALIAS = "JBrowserPrivateBookmarksKey";
    private final SharedPreferences prefs;

    public BookmarkStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<Bookmark> getPublicBookmarks() { return decode(prefs.getString(PUBLIC_KEY, "[]")); }
    public boolean hasPrivatePin() { return prefs.contains(PIN_HASH); }

    public boolean setPrivatePin(String pin) {
        if (!validPin(pin)) return false;
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            byte[] hash = derivePin(pin, salt);
            prefs.edit().putString(PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
                    .putString(PIN_HASH, Base64.encodeToString(hash, Base64.NO_WRAP)).apply();
            return true;
        } catch (Exception e) { return false; }
    }

    public boolean verifyPin(String pin) {
        try {
            String saltText = prefs.getString(PIN_SALT, null);
            String hashText = prefs.getString(PIN_HASH, null);
            if (saltText == null || hashText == null) return false;
            byte[] expected = Base64.decode(hashText, Base64.NO_WRAP);
            byte[] actual = derivePin(pin, Base64.decode(saltText, Base64.NO_WRAP));
            return MessageDigest.isEqual(expected, actual);
        } catch (Exception e) { return false; }
    }

    public List<Bookmark> getPrivateBookmarks() {
        String encrypted = prefs.getString(PRIVATE_KEY, "");
        if (encrypted.isEmpty()) return new ArrayList<>();
        try {
            byte[] packed = Base64.decode(encrypted, Base64.NO_WRAP);
            byte[] iv = new byte[12];
            System.arraycopy(packed, 0, iv, 0, iv.length);
            byte[] ciphertext = new byte[packed.length - iv.length];
            System.arraycopy(packed, iv.length, ciphertext, 0, ciphertext.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(128, iv));
            String plain = new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
            return decode(plain);
        } catch (Exception e) { return new ArrayList<>(); }
    }

    public void add(String title, String url, boolean privateBookmark) throws Exception {
        List<Bookmark> list = privateBookmark ? getPrivateBookmarks() : getPublicBookmarks();
        for (Bookmark b : list) if (b.url.equals(url)) return;
        list.add(new Bookmark(title == null || title.trim().isEmpty() ? url : title.trim(), url));
        String json = encode(list);
        if (privateBookmark) {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
            byte[] iv = cipher.getIV();
            byte[] encrypted = cipher.doFinal(json.getBytes(StandardCharsets.UTF_8));
            byte[] packed = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, packed, 0, iv.length);
            System.arraycopy(encrypted, 0, packed, iv.length, encrypted.length);
            prefs.edit().putString(PRIVATE_KEY, Base64.encodeToString(packed, Base64.NO_WRAP)).apply();
        } else {
            prefs.edit().putString(PUBLIC_KEY, json).apply();
        }
    }

    public void remove(Bookmark bookmark, boolean privateBookmark) throws Exception {
        List<Bookmark> list = privateBookmark ? getPrivateBookmarks() : getPublicBookmarks();
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).url.equals(bookmark.url)) list.remove(i);
        }
        String json = encode(list);
        if (privateBookmark) {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
            byte[] iv = cipher.getIV();
            byte[] encrypted = cipher.doFinal(json.getBytes(StandardCharsets.UTF_8));
            byte[] packed = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, packed, 0, iv.length);
            System.arraycopy(encrypted, 0, packed, iv.length, encrypted.length);
            prefs.edit().putString(PRIVATE_KEY, Base64.encodeToString(packed, Base64.NO_WRAP)).apply();
        } else prefs.edit().putString(PUBLIC_KEY, json).apply();
    }

    private SecretKey getOrCreateKey() throws Exception {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);
        java.security.Key existing = ks.getKey(KEY_ALIAS, null);
        if (existing instanceof SecretKey) return (SecretKey) existing;
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build());
        return generator.generateKey();
    }

    private static byte[] derivePin(String pin, byte[] salt) throws Exception {
        // PBKDF2-HMAC-SHA256 implemented with the standard Mac API so it also works
        // on older Android releases where SecretKeyFactory's PBKDF2-SHA256 provider
        // may not be present.
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(pin.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] blockInput = new byte[salt.length + 4];
        System.arraycopy(salt, 0, blockInput, 0, salt.length);
        blockInput[blockInput.length - 1] = 1;
        byte[] u = mac.doFinal(blockInput);
        byte[] result = u.clone();
        for (int i = 1; i < 60000; i++) {
            u = mac.doFinal(u);
            for (int j = 0; j < result.length; j++) result[j] ^= u[j];
        }
        return result;
    }

    private static boolean validPin(String pin) { return pin != null && pin.matches("[0-9]{4,8}"); }

    private static String encode(List<Bookmark> list) throws Exception {
        JSONArray array = new JSONArray();
        for (Bookmark bookmark : list) {
            JSONObject item = new JSONObject();
            item.put("title", bookmark.title);
            item.put("url", bookmark.url);
            array.put(item);
        }
        return array.toString();
    }

    private static List<Bookmark> decode(String json) {
        List<Bookmark> out = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                String title = item.optString("title", "Bookmark");
                String url = item.optString("url", "");
                if (!url.isEmpty()) out.add(new Bookmark(title, url));
            }
        } catch (Exception ignored) { }
        return out;
    }
}
