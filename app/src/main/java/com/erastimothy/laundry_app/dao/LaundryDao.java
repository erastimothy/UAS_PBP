package com.erastimothy.laundry_app.dao;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.erastimothy.laundry_app.api.LaundryAPI;
import com.erastimothy.laundry_app.api.LayananAPI;
import com.erastimothy.laundry_app.model.Laundry;
import com.erastimothy.laundry_app.model.Layanan;
import com.erastimothy.laundry_app.preferences.LaundryPreferences;
import com.erastimothy.laundry_app.preferences.LayananPreferences;
import com.erastimothy.laundry_app.preferences.TokoPreferences;
import com.erastimothy.laundry_app.preferences.UserPreferences;
import com.erastimothy.laundry_app.user.OrderDetailActivity;

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

import static com.android.volley.Request.Method.GET;
import static com.android.volley.Request.Method.POST;
import static com.android.volley.Request.Method.PUT;

public class LaundryDao {
    private Activity activity;
    private Laundry laundry;
    private SharedPreferences laundrySP;
    private SharedPreferences.Editor editor;
    private List<Laundry> laundryList;


    public LaundryDao(){}
    public LaundryDao(Activity myActivity){
        activity = myActivity;

        laundryList = new ArrayList<>();
    }
    public void save(Laundry laundry){

        RequestQueue queue = Volley.newRequestQueue(activity);

        //Memulai membuat permintaan request menghapus data ke jaringan
        StringRequest stringRequest = new StringRequest(POST, LaundryAPI.URL_ADD, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Disini bagian jika response jaringan berhasil tidak terdapat ganguan/error
                try {
                    //Mengubah response string menjadi object
                    JSONObject obj = new JSONObject(response);
                    if(obj.getString("message").equals("Add laundry Success")){
                        Toast.makeText(activity, "Orderan berhasil diterima !", Toast.LENGTH_SHORT).show();

                        JSONObject data = obj.getJSONObject("data");
                        LaundryPreferences laundryPreferences = new LaundryPreferences(activity);
                        laundryPreferences.createLaundry(laundry);

                        Intent intent = new Intent(activity, OrderDetailActivity.class);
                        Bundle bundle = new Bundle();

                        bundle.putString("alamat",data.getString("address"));
                        bundle.putString("biaya_antar",data.getString("shippingcost"));
                        bundle.putString("harga",obj.getJSONObject("service").getString("price"));
                        bundle.putString("total_pembayaran",data.getString("total"));
                        bundle.putString("jenis",obj.getJSONObject("service").getString("name"));
                        bundle.putString("kuantitas", data.getString("quantity"));
                        bundle.putString("order_id",data.getString("id"));
                        bundle.putString("nama",obj.getJSONObject("user").getString("name"));
                        bundle.putString("tanggal",data.getString("created_at"));
                        bundle.putString("id",data.getString("id"));
                        bundle.putString("from","order");
                        bundle.putString("status",data.getString("status"));
                        intent.putExtra("laundry",bundle);

                        activity.startActivity(intent);
                        activity.finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Disini bagian jika response jaringan terdapat ganguan/error
                Toast.makeText(activity, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                UserPreferences userSP = new UserPreferences(activity);

                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put("Accept","application/json");
                params.put("Authorization", "Bearer " + userSP.getAccesToken());
                return params;
            }
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("service_id",String.valueOf(laundry.getService_id()));
                params.put("quantity", String.valueOf(laundry.getQuantity()));
                params.put("shippingcost", String.valueOf(laundry.getShippingcost()));
                params.put("total", String.valueOf(laundry.getTotal()));
                params.put("address", laundry.getAddress());

                return params;
            }

        };

        queue.add(stringRequest);

    }

    public void setAllDataLaundry(){
        RequestQueue queue = Volley.newRequestQueue(activity);

        //Meminta tanggapan string dari URL yang telah disediakan menggunakan method GET

        final JsonObjectRequest stringRequest = new JsonObjectRequest(GET, LaundryAPI.URL_SELECT
                , null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Disini bagian jika response jaringan berhasil tidak terdapat ganguan/error
                try {
                    Log.d("Laundry", response.getString("message"));

                    JSONArray jsonArray = response.getJSONArray("data");

                    if(response.getString("message").equals("Retrive All laundry Success")){
                        if(!laundryList.isEmpty())
                            laundryList.clear();

                        for (int i =0 ;i<jsonArray.length(); i++){
                            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                            int id = jsonObject.getInt("id");
                            int service_id = jsonObject.getInt("service_id");
                            int user_id = jsonObject.getInt("user_id");
                            float quantity = Float.parseFloat(jsonObject.getString("quantity"));
                            String status = jsonObject.getString("status");
                            String address = jsonObject.getString("address");
                            String date = jsonObject.getString("created_at");
                            double shippingcost = jsonObject.getDouble("shippingcost");
                            double total = jsonObject.getDouble("total");

                            Laundry l = new Laundry(id,service_id,user_id,quantity,shippingcost,total,status,address,date);
                            laundryList.add(l);

                        }
                        LaundryPreferences laundryPreferences = new LaundryPreferences(activity);
                        laundryPreferences.setAllLaundry(laundryList);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("Laundry",response.optString("message"));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Disini bagian jika response jaringan terdapat ganguan/error
                Toast.makeText(activity, error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                UserPreferences userSP = new UserPreferences(activity);

                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put("Accept","application/json");
                params.put("Authorization", "Bearer " + userSP.getAccesToken());
                return params;
            }
        };

        queue.add(stringRequest);

    }

    public void update(Laundry laundry,int id){

        RequestQueue queue = Volley.newRequestQueue(activity);

        //Memulai membuat permintaan request menghapus data ke jaringan
        StringRequest stringRequest = new StringRequest(PUT, LaundryAPI.URL_UPDATE+id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Disini bagian jika response jaringan berhasil tidak terdapat ganguan/error
                try {
                    //Mengubah response string menjadi object
                    JSONObject obj = new JSONObject(response);
                    if(obj.getString("message").equals("Update laundry Success")){
                        Toast.makeText(activity, "Orderan berhasil diubah !", Toast.LENGTH_SHORT).show();

                        JSONObject data = obj.getJSONObject("data");
                        LaundryPreferences laundryPreferences = new LaundryPreferences(activity);
                        laundryPreferences.createLaundry(laundry);

                        Intent intent = new Intent(activity, OrderDetailActivity.class);
                        Bundle bundle = new Bundle();

                        bundle.putString("alamat",data.getString("address"));
                        bundle.putString("biaya_antar",data.getString("shippingcost"));
                        bundle.putString("harga",obj.getJSONObject("service").getString("price"));
                        bundle.putString("total_pembayaran",data.getString("total"));
                        bundle.putString("jenis",obj.getJSONObject("service").getString("name"));
                        bundle.putString("kuantitas", data.getString("quantity"));
                        bundle.putString("order_id",data.getString("id"));
                        bundle.putString("nama",obj.getJSONObject("user").getString("name"));
                        bundle.putString("tanggal",data.getString("created_at"));
                        bundle.putString("id",data.getString("id"));
                        bundle.putString("from","order");
                        bundle.putString("status",data.getString("status"));
                        intent.putExtra("laundry",bundle);

                        activity.startActivity(intent);
                        activity.finish();
                    }
                    Log.e("UPDATE LAUNDRY",obj.getString("message"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Disini bagian jika response jaringan terdapat ganguan/error
                Toast.makeText(activity, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                UserPreferences userSP = new UserPreferences(activity);

                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                params.put("Accept","application/json");
                params.put("Authorization", "Bearer " + userSP.getAccesToken());
                return params;
            }
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("service_id",String.valueOf(laundry.getService_id()));
                params.put("quantity", String.valueOf(laundry.getQuantity()));
                params.put("shippingcost", String.valueOf(laundry.getShippingcost()));
                params.put("total", String.valueOf(laundry.getTotal()));
                params.put("address", laundry.getAddress());
                params.put("status", laundry.getStatus());

                return params;
            }

        };

        queue.add(stringRequest);

    }



}
