package com.erastimothy.laundry_app.dao;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.erastimothy.laundry_app.MainActivity;
import com.erastimothy.laundry_app.api.LayananAPI;
import com.erastimothy.laundry_app.api.TokoAPI;
import com.erastimothy.laundry_app.api.UserAPI;
import com.erastimothy.laundry_app.model.Laundry;
import com.erastimothy.laundry_app.model.Layanan;
import com.erastimothy.laundry_app.preferences.LaundryPreferences;
import com.erastimothy.laundry_app.preferences.LayananPreferences;
import com.erastimothy.laundry_app.preferences.TokoPreferences;
import com.erastimothy.laundry_app.preferences.UserPreferences;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.android.volley.Request.Method.DELETE;
import static com.android.volley.Request.Method.GET;
import static com.android.volley.Request.Method.POST;
import static com.android.volley.Request.Method.PUT;

public class LayananDao {
    private Context context;
    private Layanan layanan;
    private SharedPreferences layananSP;
    private SharedPreferences.Editor editor;
    private List<Layanan> layananList;

    public LayananDao(Context context) {
        this.context = context;

        layananList = new ArrayList<>();
    }

    public void save(Layanan layanan) {
        LayananPreferences layananPreferences = new LayananPreferences(context);

        RequestQueue queue = Volley.newRequestQueue(context);

        //Memulai membuat permintaan request menghapus data ke jaringan
        StringRequest stringRequest = new StringRequest(POST, LayananAPI.URL_ADD, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Disini bagian jika response jaringan berhasil tidak terdapat ganguan/error
                try {
                    //Mengubah response string menjadi object
                    JSONObject obj = new JSONObject(response);

                    layananPreferences.createLayanan(layanan);
                    setAllDataLayanan();
                    Toast.makeText(context, obj.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Disini bagian jika response jaringan terdapat ganguan/error
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                UserPreferences userSP = new UserPreferences(context);

                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put("Accept","application/json");
                params.put("Authorization", "Bearer " + userSP.getAccesToken());
                return params;
            }
            @Override
            protected Map<String, String> getParams()
            {
                /*
                    Disini adalah proses memasukan/mengirimkan parameter key dengan data value,
                    dan nama key nya harus sesuai dengan parameter key yang diminta oleh jaringan
                    API.
                */
                Map<String, String>  params = new HashMap<String, String>();
                params.put("name", layanan.getName());
                params.put("price", String.valueOf(layanan.getHarga()));

                return params;
            }
        };

        queue.add(stringRequest);

    }

    public void update(Layanan layanan, int id) {
        LayananPreferences layananPreferences = new LayananPreferences(context);

        RequestQueue queue = Volley.newRequestQueue(context);

        //Memulai membuat permintaan request menghapus data ke jaringan
        StringRequest stringRequest = new StringRequest(PUT, LayananAPI.URL_UPDATE+id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Disini bagian jika response jaringan berhasil tidak terdapat ganguan/error
                try {
                    //Mengubah response string menjadi object
                    JSONObject obj = new JSONObject(response);

                    layananPreferences.createLayanan(layanan);
                    setAllDataLayanan();
                    Toast.makeText(context, obj.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Disini bagian jika response jaringan terdapat ganguan/error
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError
                {
                    UserPreferences userSP = new UserPreferences(context);

                    Map<String,String> params = new HashMap<String, String>();
                    params.put("Content-Type","application/x-www-form-urlencoded");
                    params.put("Accept","application/json");
                    params.put("Authorization", "Bearer " + userSP.getAccesToken());
                    return params;
                }
                @Override
                protected Map<String, String> getParams()
                {
                    /*
                        Disini adalah proses memasukan/mengirimkan parameter key dengan data value,
                        dan nama key nya harus sesuai dengan parameter key yang diminta oleh jaringan
                        API.
                    */
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("name", layanan.getName());
                    params.put("price", String.valueOf(layanan.getHarga()));

                    return params;
                }
        };

        queue.add(stringRequest);

    }

    public void setAllDataLayanan() {
        RequestQueue queue = Volley.newRequestQueue(context);

        final JsonObjectRequest stringRequest = new JsonObjectRequest(GET, LayananAPI.URL_SELECT
                , null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Disini bagian jika response jaringan berhasil tidak terdapat ganguan/error
                try {
                    Log.d("Layanan", response.getString("message"));

                    JSONArray jsonArray = response.getJSONArray("data");

                    if(response.getString("message").equals("Retrive All Service Success")){
                        if(!layananList.isEmpty())
                            layananList.clear();

                        for (int i =0 ;i<jsonArray.length(); i++){
                            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                            int id = jsonObject.getInt("id");
                            String name = jsonObject.getString("name");
                            double price = jsonObject.getDouble("price");

                            Layanan l = new Layanan(name,id,price);
                            layananList.add(l);
                            LayananPreferences layananPreferences = new LayananPreferences(context);
                            layananPreferences.setAllLayanan(layananList);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("LAYANAN",response.optString("message"));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Disini bagian jika response jaringan terdapat ganguan/error
                Toast.makeText(context, error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e("LAYANAN ",error.getMessage());
            }
        })
        {
            @Override
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

    public void deleteLayanan(int id){
        LayananPreferences layananPreferences = new LayananPreferences(context);

        RequestQueue queue = Volley.newRequestQueue(context);

        //Memulai membuat permintaan request menghapus data ke jaringan
        StringRequest stringRequest = new StringRequest(DELETE, LayananAPI.URL_DELETE+id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Disini bagian jika response jaringan berhasil tidak terdapat ganguan/error
                try {
                    JSONObject obj = new JSONObject(response);
                    Toast.makeText(context, obj.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Disini bagian jika response jaringan terdapat ganguan/error
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                UserPreferences userSP = new UserPreferences(context);

                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put("Accept","application/json");
                params.put("Authorization", "Bearer " + userSP.getAccesToken());
                return params;
            }
        };

        queue.add(stringRequest);

        setAllDataLayanan();
    }

}
