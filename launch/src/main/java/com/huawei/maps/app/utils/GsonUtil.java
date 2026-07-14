package com.huawei.maps.app.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GsonUtil {

    private static volatile Gson gson = new Gson();

    private GsonUtil() {
        throw new UnsupportedOperationException();
    }

    public static Gson getGson() {
        if (gson == null) {
            synchronized (GsonUtil.class) {
                if (gson == null) {
                    gson = new Gson();
                }
            }
        }
        return gson;
    }

    public static JsonObject toElement(String json) {
        return new JsonParser().parse(json).getAsJsonObject();
    }


    public static JsonElement getAsJsonElement(JsonObject jo, String memberName) {
        if (jo == null || jo.isJsonNull()) {
            return null;
        }
        if (jo.has(memberName)) {
            JsonElement je = jo.get(memberName);
            if (je == null || je.isJsonNull()) {
                return null;
            }
            return je;
        } else {
            return null;
        }
    }

    public static JsonObject getAsJsonObject(JsonObject jo, String memberName) {
        if (jo == null || jo.isJsonNull()) {
            return null;
        }
        if (jo.has(memberName)) {
            JsonElement je = jo.get(memberName);
            if (je == null || je.isJsonNull()) {
                return null;
            }
            return je.getAsJsonObject();
        } else {
            return null;
        }
    }

    public static String getAsString(JsonObject jo, String memberName) {
        if (jo == null || jo.isJsonNull()) {
            return "";
        }
        if (jo.has(memberName)) {
            JsonElement je = jo.get(memberName);
            if (je == null || je.isJsonNull()) {
                return "";
            }
            return je.getAsString();
        } else {
            return "";
        }
    }

    public static String toJson(Object obj) {
        return getGson().toJson(obj);
    }

    public static <T> List<T> fromJson2ListData(String json, Type type) {
        if (TextUtils.isEmpty(json)) {
            return new ArrayList<>();
        }
        return gson.fromJson(json, type);
    }

    public static List<String> fromJson2ListString(String strList) {
        if (TextUtils.isEmpty(strList)) {
            return new ArrayList<>();
        }
        return getGson().fromJson(strList, new TypeToken<List<String>>() {
        }.getType());
    }

    public static List<String> fromJsonArray2ListString(JsonArray jsonArray) {
        if (jsonArray == null) {
            return new ArrayList<>();
        }
        return getGson().fromJson(jsonArray, new TypeToken<List<String>>() {
        }.getType());
    }

    public static String formatList2Json(List<String> strings) {
        return getGson().toJson(strings, new TypeToken<List<String>>() {
        }.getType());
    }

    public static int getAsInt(JsonObject jo, String memberName) {
        if (jo == null || jo.isJsonNull()) {
            return 0;
        }
        if (jo.has(memberName)) {
            JsonElement je = jo.get(memberName);
            if (je == null || je.isJsonNull()) {
                return 0;
            }
            return je.getAsInt();
        } else {
            return 0;
        }
    }

    public static boolean isInvalid(JsonElement jo) {
        return jo == null || jo.isJsonNull();
    }

    public static boolean isInvalidJO(JsonElement jo) {
        return jo == null || jo.isJsonNull() || !jo.isJsonObject();
    }
}
