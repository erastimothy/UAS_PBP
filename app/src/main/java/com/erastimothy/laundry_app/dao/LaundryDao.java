package com.erastimothy.laundry_app.dao;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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

public class LaundryDao {
    private Activity activity;
    private ProgressDialog progressDialog;
    private Laundry laundry;
    private SharedPreferences laundrySP;
    private SharedPreferences.Editor editor;
    private List<Laundry> laundryList;


    public LaundryDao(){}
    public LaundryDao(Activity myActivity){
        activity = myActivity;

        laundryList = new ArrayList<>();
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Loading...");
    }
    public void save(Laundry laundry){

        progressDialog.show();
        RequestQueue queue = Volley.newRequestQueue(activity);

        //Memulai membuat permintaan request menghapus data ke jaringan
        StringRequest stringRequest = new StringRequest(POST, LaundryAPI.URL_ADD, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Disini bagian jika response jaringan berhasil tidak terdapat ganguan/error
                progressDialog.dismiss();
                try {
                    //Mengubah response string menjadi object
                    JSONObject obj = new JSONObject(response);
                    if(obj.getString("message").equals("Add laundry Success")){
                        Toast.makeText(activity, "Orderan berhasil diterima !", Toast.LENGTH_SHORT).show();
                        LaundryPreferences laundryPreferences = new LaundryPreferences(activity);
                        laundryPreferences.createLaundry(laundry);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Disini bagian jika response jaringan terdapat ganguan/error
                progressDialog.dismiss();
                Toast.makeText(activity, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams()
            {
                /*
                    Disini adalah proses memasukan/mengirimkan parameter key dengan data value,
                    dan nama key nya harus sesuai dengan parameter key yang diminta oleh jaringan
                    API.
                */
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

        progressDialog.setProgressStyle(android.app.ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        final JsonObjectRequest stringRequest = new JsonObjectRequest(GET, LaundryAPI.URL_SELECT
                , null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Disini bagian jika response jaringan berhasil tidak terdapat ganguan/error
                progressDialog.dismiss();
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
                Toast.makeText(activity, response.optString("message"), Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Disini bagian jika response jaringan terdapat ganguan/error
                progressDialog.dismiss();
                Toast.makeText(activity, error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        //Disini proses penambahan request yang sudah kita buat ke reuest queue yang sudah dideklarasi
        queue.add(stringRequest);

    }



}
