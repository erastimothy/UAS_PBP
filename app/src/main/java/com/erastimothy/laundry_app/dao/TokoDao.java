package com.erastimothy.laundry_app.dao;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.erastimothy.laundry_app.api.TokoAPI;
import com.erastimothy.laundry_app.model.Toko;
import com.erastimothy.laundry_app.preferences.TokoPreferences;
import com.erastimothy.laundry_app.preferences.UserPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.android.volley.Request.Method.GET;

public class TokoDao {
    private Context context;
    private ProgressDialog progressDialog;
    private Toko toko;

    public TokoDao(Context context) {
        this.context = context;
    }

    public void saveToko(Toko toko) {
        TokoPreferences tokoPreferences = new TokoPreferences(context);
        tokoPreferences.createToko(toko.getId(), toko.getName(), toko.getAlamat(), toko.getLongitude(), toko.getLatitude(), toko.getTelp());
    }

    public void setTokoFromDatabase() {
        //Pendeklarasian queue
        RequestQueue queue = Volley.newRequestQueue(context);

        //Meminta tanggapan string dari URL yang telah disediakan menggunakan method GET

        JsonObjectRequest stringRequest = new JsonObjectRequest(GET, TokoAPI.URL_SELECT
                , null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Disini bagian jika response jaringan berhasil tidak terdapat ganguan/error

                try {

                    TokoPreferences tokoPreferences = new TokoPreferences(context);
                    JSONObject data = response.optJSONObject("data");

                    int id = data.getInt("id");
                    String name = data.getString("name");
                    String phoneNumber = data.getString("phoneNumber");
                    Double latitude = data.getDouble("latitude");
                    Double longitude = data.getDouble("longitude");
                    String address = data.getString("address");
                    tokoPreferences.createToko(id, name, address, longitude, latitude, phoneNumber);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("TOKO",response.optString("message"));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Disini bagian jika response jaringan terdapat ganguan/error
                if (error == null || error.networkResponse == null) {
                    return;
                }
                String body = "";
                //get status code here
                final String statusCode = String.valueOf(error.networkResponse.statusCode);
                //get response body and parse with appropriate encoding
                try {
                    body = new String(error.networkResponse.data,"UTF-8");
                    JSONObject jsonObject = new JSONObject(body);
                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }){
            public Map<String, String> getHeaders() throws AuthFailureError {
                UserPreferences userSP = new UserPreferences(context);

                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put("Accept","application/json");
                params.put("Authorization", "Bearer " + userSP.getAccesToken());
                return params;
            }
        };


        //Disini proses penambahan request yang sudah kita buat ke reuest queue yang sudah dideklarasi
        queue.add(stringRequest);
    }

}
