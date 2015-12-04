/**
 * Copyright (C) © 2014 深圳市掌玩网络技术有限公司
 * LibCommon
 * SharedPreferenceUtils.java
 */

package cn.vszone.ko.plugin.sdk.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;

/**
 * SharedPreference 存取的工具类，支持按用户(uid)存取
 * 
 * @author Stanley.Luo
 * @firstCreate 2014年6月28日
 * @lastModify
 */
public class SharedPreferenceUtils {

    // ===========================================================
    // Constants
    // +==========================================================
    private static final int    UID_APP     = 0;

    /**
     * 为了版本兼容， 这个不能修改!
     */
    public static final String  NAME_PREFIX = "kobox";

    // -==========================================================

    // ===========================================================
    // Methods
    // +==========================================================


    public static Editor getSharedPreferencesEditor(Context pContext) {
        SharedPreferences sp = getSharedPreferences(pContext);
        return sp.edit();
    }

    public static Editor getSharedPreferencesEditor(Context pContext, int pUid) {
        SharedPreferences sp = getSharedPreferences(pContext, pUid);
        return sp.edit();
    }

    public static Editor getSharedPreferencesEditor(Context pContext, String pPrefsName) {
        SharedPreferences sp = getSharedPreferences(pContext, pPrefsName);
        return sp.edit();
    }
    public static SharedPreferences getdefaultSharedPreferences(Context pContext) {
        return PreferenceManager.getDefaultSharedPreferences(pContext);
    }
    public static SharedPreferences getSharedPreferences(Context pContext) {
        return getSharedPreferences(pContext, UID_APP);
    }
    
    public static SharedPreferences getSharedPreferences(Context pContext, int pUid) {
        return getSharedPreferences(pContext, String.valueOf(UID_APP));
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static SharedPreferences getSharedPreferences(Context pContext, int pUid, int pMode) {
        String spName = SharedPreferenceUtils.NAME_PREFIX + "." + String.valueOf(UID_APP) + ".sp";
        SharedPreferences sp = pContext.getSharedPreferences(spName, pMode);
        return sp;
    }

    public static SharedPreferences getSharedPreferences(Context pContext, String pPrefsName) {
        return getSharedPreferences(pContext, pPrefsName, Context.MODE_PRIVATE);
    }
    
    /**
     * 
     * @param pContext
     * @param pPrefsName SharedPreference 的文件名
     * @param pSPMode SharedPreference 的可读写权限范围
     * {@link Context.MODE_MULTI_PROCESS}, {@link Context.MODE_PRIVATE}
     */
    public static SharedPreferences getSharedPreferences(Context pContext, String pPrefsName, int pSPMode) {
        //SharedPreferences.Editor editor = getSharedPreferencesEditor(pContext, pPrefsName)
        String spName = SharedPreferenceUtils.NAME_PREFIX + "." + pPrefsName + ".sp";
        SharedPreferences sp = pContext.getSharedPreferences(spName, pSPMode);
        return sp;
    }
    
    /**
     * 
     * @param pContext
     * @param pPrefsName SharedPreference 的文件名
     * @param pSPMode SharedPreference 的可读写权限范围
     * {@link Context.MODE_MULTI_PROCESS}, {@link Context.MODE_PRIVATE}
     */
    public static SharedPreferences.Editor getSharedPreferencesEditor(Context pContext, String pPrefsName, int pSPMode) {
        SharedPreferences sp = getSharedPreferences(pContext, pPrefsName, pSPMode);
        return sp.edit();
    }

    public static void clear(Context pContext, int pUid) {
        Editor editor = getSharedPreferencesEditor(pContext, pUid);
        clear(editor, pUid);
    }
    
    public static void clear(Editor pEditor, int pUid) {
        if(pEditor != null){
            pEditor.clear().commit();
        }
    }

    public static void setObject(Context pContext, String pKey, Object o) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(pContext);
        setObject(editor, pKey, o);
    }
//    
//    public static void setStringSet(Context pContext, String pKey,Set<String> pValues){
//        SharedPreferences.Editor editor = getSharedPreferencesEditor(pContext);
//        setStringSet(editor, pKey, pValues);
//    }
//    
//    public static Set<String> getStringSet(Context pContext,String pKey,Set<String> pDefaultSet){
//        SharedPreferences preferences = getSharedPreferences(pContext);
//        return preferences.getStringSet(pKey, pDefaultSet);
//    }
//    
//    public static void setStringSet(Editor pEditor, String pKey,Set<String> pValues){
//        if(pValues!=null&&!pValues.isEmpty()){
//            pEditor.putStringSet(pKey, pValues);
//            pEditor.commit();
//        }
//    }
    public static void setObject(Editor pEditor, String pKey, Object o) {
        String str = null;
        try {
            str = SharedPreferenceUtils.writeObject(o);
        } catch (Exception e) {
            //LOG.e("setObject fail:" + pKey, e);
        }
        pEditor.putString(pKey, str);
        pEditor.commit();
    }

    public static Object getObject(Context pContext, String pKey) {
        SharedPreferences preferences = getSharedPreferences(pContext);
        return getObject(preferences, pKey);
    }
    
    public static Object getObject(SharedPreferences pPreferences, String pKey) {
        Object o = null;
        if (pPreferences != null) {
            String str = pPreferences.getString(pKey, null);
            if (!TextUtils.isEmpty(str)) {
                try {
                    o = SharedPreferenceUtils.readObject(str);
                } catch (Exception e) {
                    e.printStackTrace();
                    //LOG.e("getObject error :" + pKey, e);
                }
            }
        }
        return o;
    }
    
    public static void delete(Context pContext, String pKey) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(pContext);
        delete(editor, pKey);
    }
    
    public static void delete(Editor pEditor, String pKey) {
        if(pEditor != null){
            pEditor.remove(pKey);
            pEditor.commit();
        }
    }

    public static void setBoolean(Context pContext, String pKey, boolean pValue) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(pContext);
        setBoolean(editor, pKey, pValue);
    }

    /**
     * 用在2.3以上版本
     * 
     * @param pContext
     * @param pKey
     * @param pValue
     * @param pMode
     *            context类型（Context.）
     */
    @SuppressLint("CommitPrefEdits")
    public static void setBoolean(Context pContext, String pKey, boolean pValue, int pMode) {
        SharedPreferences sp = getSharedPreferences(pContext, UID_APP, pMode);
        SharedPreferences.Editor editor = sp.edit();
        setBoolean(editor, pKey, pValue);
    }

    public static void setBoolean(Editor pEditor, String pKey, boolean pValue) {
        if(pEditor != null){
            pEditor.putBoolean(pKey, pValue);
            pEditor.commit();
        }
    }

    public static boolean getBoolean(Context pContext, String pKey, boolean pDefaultValue) {
        SharedPreferences preferences = getSharedPreferences(pContext);
        return getBoolean(preferences, pKey, pDefaultValue);
    }

    /**
     * 用在2.3以上版本
     * 
     * @param pContext
     * @param pKey
     * @param pDefaultValue
     * @param pMode
     *            context类型（Context.）
     * @return
     */
    public static boolean getBoolean(Context pContext, String pKey, boolean pDefaultValue, int pMode) {
        SharedPreferences preferences = getSharedPreferences(pContext, UID_APP, pMode);
        return getBoolean(preferences, pKey, pDefaultValue);
    }

    public static boolean getBoolean(SharedPreferences pPreferences, String pKey, boolean pDefaultValue) {
        if(pPreferences != null){
            return pPreferences.getBoolean(pKey, pDefaultValue);
        }
        return pDefaultValue;
    }

    public static void setString(Context pContext, String pKey, String pValue) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(pContext);
        setString(editor, pKey, pValue);
    }
    
    public static void setString(Editor pEditor, String pKey, String pValue) {
        if(pEditor != null){
            pEditor.putString(pKey, pValue);
            pEditor.commit();
        }
    }

    public static String getString(Context pContext, String pKey, String pDefaultValue) {
        SharedPreferences preferences = getSharedPreferences(pContext);
        return getString(preferences, pKey, pDefaultValue);
    }
    
    public static String getString(SharedPreferences pPreferences, String pKey, String pDefaultValue) {
        if(pPreferences != null){
            return pPreferences.getString(pKey, pDefaultValue);
        }
        return pDefaultValue;
    }

    public static void setInt(Context pContext, String pKey, int pValue) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(pContext);
        setInt(editor, pKey, pValue);
    }
    
    public static void setInt(Editor pEditor, String pKey, int pValue) {
        if(pEditor != null){
            pEditor.putInt(pKey, pValue);
            pEditor.commit();
        }
    }

    public static int getInt(Context pContext, String pKey, int pDefaultValue) {
        SharedPreferences preferences = getSharedPreferences(pContext);
        return getInt(preferences, pKey, pDefaultValue);
    }
    
    public static int getInt(SharedPreferences pPreferences, String pKey, int pDefaultValue) {
        if(pPreferences != null){
            return pPreferences.getInt(pKey, pDefaultValue);
        }
        return pDefaultValue;
    }

    public static void setLong(Context pContext, String pKey, long pValue) {
        SharedPreferences.Editor editor = getSharedPreferencesEditor(pContext);
        setLong(editor, pKey, pValue);
    }
    
    public static void setLong(Editor pEditor, String pKey, long pValue) {
        if(pEditor != null){
            pEditor.putLong(pKey, pValue);
            pEditor.commit();
        }
    }
    
    public static long getLong(Context pContext, String pKey, long pDefaultValue) {
        SharedPreferences preferences = getSharedPreferences(pContext);
        return getLong(preferences, pKey, pDefaultValue);
    }
    
    public static long getLong(SharedPreferences pPreferences, String pKey, long pDefaultValue) {
        if(pPreferences != null){
            return pPreferences.getLong(pKey, pDefaultValue);
        }
        return pDefaultValue;
    }

    /**
     * 序列化对象为String字符串
     * 
     * @param o
     *            Object
     * @return String
     * @throws Exception
     */
    public static String writeObject(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o); 
        oos.flush();
        oos.close();
        bos.close();
        return Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);
    }

    /**
     * 反序列化字符串为对象
     * 
     * @param object
     *            String
     * @return
     * @throws Exception
     */
    public static Object readObject(String object) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decode(object, Base64.DEFAULT));
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object o = ois.readObject();
        bis.close();
        ois.close();
        return o;
    }

    // -==========================================================
}
