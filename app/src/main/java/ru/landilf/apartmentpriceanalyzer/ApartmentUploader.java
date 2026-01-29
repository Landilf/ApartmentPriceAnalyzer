package ru.landilf.apartmentpriceanalyzer;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ApartmentUploader {

    private Context context;
    private FirebaseFirestore db;

    public ApartmentUploader(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    public void uploadApartments() {
        String json = loadJSONFromAsset();
        if (json == null) return;

        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Map<String, Object> data = jsonToMap(obj);
                
                db.collection("apartments")
                        .add(data)
                        .addOnSuccessListener(documentReference -> Log.d("Uploader", "DocumentSnapshot added with ID: " + documentReference.getId()))
                        .addOnFailureListener(e -> {
                            Log.w("Uploader", "Error adding document", e);
                            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
            Toast.makeText(context, "Data upload started...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = context.getAssets().open("apartments.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private Map<String, Object> jsonToMap(JSONObject json) throws Exception {
        Map<String, Object> map = new HashMap<>();
        
        java.util.Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = json.get(key);
            
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = jsonToMap((JSONObject) value);
            } else if (value == JSONObject.NULL) {
                value = null;
            }
            map.put(key, value);
        }
        return map;
    }

    private java.util.List<Object> toList(JSONArray array) throws Exception {
        java.util.List<Object> list = new java.util.ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = jsonToMap((JSONObject) value);
            } else if (value == JSONObject.NULL) {
                value = null;
            }
            list.add(value);
        }
        return list;
    }
}
