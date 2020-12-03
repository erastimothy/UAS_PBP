package com.erastimothy.laundry_app.dao;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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
import com.erastimothy.laundry_app.LoginActivity;
import com.erastimothy.laundry_app.MainActivity;
import com.erastimothy.laundry_app.api.LayananAPI;
import com.erastimothy.laundry_app.api.UserAPI;
import com.erastimothy.laundry_app.model.Layanan;
import com.erastimothy.laundry_app.model.User;
import com.erastimothy.laundry_app.preferences.LayananPreferences;
import com.erastimothy.laundry_app.preferences.UserPreferences;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.volley.Request.Method.GET;
import static com.android.volley.Request.Method.POST;
import static com.android.volley.Request.Method.PUT;

public class UserDao {
    private Activity activity;
    private String name,phoneNumber,email,password;
    private ProgressDialog progressDialog;
    private User user;
    List<User> listUser;

    public UserDao(){}
    public UserDao(Activity myActivity) {
        activity = myActivity;

        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Loading...");

    }


    public void login(String email, String password){
        progressDialog.show();
        RequestQueue queue = Volley.newRequestQueue(activity);

        //Memulai membuat permintaan request menghapus data ke jaringan
        StringRequest stringRequest = new StringRequest(POST, UserAPI.URL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Disini bagian jika response jaringan berhasil tidak terdapat ganguan/error
                progressDialog.dismiss();
                try {
                    //Mengubah response string menjadi object
                    JSONObject obj = new JSONObject(response);

                    Toast.makeText(activity, obj.getString("message"), Toast.LENGTH_SHORT).show();

                    //obj.getString("message") digunakan untuk mengambil pesan status dari response
                    if(obj.getString("message").equalsIgnoreCase("Login Success"))
                    {
                        JSONObject data = new JSONObject(obj.getString("data"));
                        int id = data.getInt("id");
                        int role_id = data.getInt("role_id");
                        String name = data.getString("name");
                        String avatar = data.getString("avatar");
                        String email = data.getString("email");
                        String phoneNumber = data.getString("phoneNumber");
                        String access_token = obj.getString("access_token");


                        JSONObject role = new JSONObject(data.getString("role"));
                        String role_name = role.getString("name");

                        UserPreferences sessionUser = new UserPreferences(activity);
                        sessionUser.createLoginUser(id,email,access_token,name,phoneNumber,role_id,role_name,avatar);

                        Intent intent = new Intent(activity, MainActivity.class);
                        //define intent from login so doesnt show splash screen
                        intent.putExtra("login",true);
                        activity.startActivity(intent);
                    }
                    //obj.getString("message") digunakan untuk mengambil pesan message dari response
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Disini bagian jika response jaringan terdapat ganguan/error
                progressDialog.dismiss();
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
                    Toast.makeText(activity, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
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
                params.put("email", email);
                params.put("password", password);

                return params;
            }
        };

        queue.add(stringRequest);
    }




    public void registerAuth(String email, String password,String name,String phoneNumber){

        progressDialog.show();
        RequestQueue queue = Volley.newRequestQueue(activity);

        //Memulai membuat permintaan request menghapus data ke jaringan
        StringRequest stringRequest = new StringRequest(POST, UserAPI.URL_REGISTER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Disini bagian jika response jaringan berhasil tidak terdapat ganguan/error
                progressDialog.dismiss();
                Log.e("REGISTER",response);
                try {
                    //Mengubah response string menjadi object
                    JSONObject obj = new JSONObject(response);

                    //obj.getString("message") digunakan untuk mengambil pesan message dari response
                    Toast.makeText(activity, obj.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Disini bagian jika response jaringan terdapat ganguan/error
                progressDialog.dismiss();
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
                    Toast.makeText(activity, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
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
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);
                params.put("phoneNumber", phoneNumber);

                return params;
            }
        };

        queue.add(stringRequest);

        progressDialog.dismiss();
    }

    public void updateUser(int id, String name, String email, String password, String phoneNumber){
        progressDialog.show();
        RequestQueue queue = Volley.newRequestQueue(activity);

        //Memulai membuat permintaan request menghapus data ke jaringan
        StringRequest stringRequest = new StringRequest(PUT, UserAPI.URL_UPDATE+id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Disini bagian jika response jaringan berhasil tidak terdapat ganguan/error
                progressDialog.dismiss();

                try {
                    JSONObject obj = new JSONObject(response);

                    Toast.makeText(activity, obj.getString("message"), Toast.LENGTH_SHORT).show();

                    JSONObject data = new JSONObject(obj.getString("data"));
                    int id = data.getInt("id");
                    int role_id = data.getInt("role_id");
                    String name = data.getString("name");
                    String avatar = data.getString("avatar");
                    String email = data.getString("email");
                    String phoneNumber = data.getString("phoneNumber");
                    String access_token = obj.getString("access_token");


                    JSONObject role = new JSONObject(data.getString("role"));
                    String role_name = role.getString("name");

                    UserPreferences sessionUser = new UserPreferences(activity);
                    sessionUser.createLoginUser(id,email,access_token,name,phoneNumber,role_id,role_name,avatar);

                    activity.onBackPressed();
//                    Intent intent = new Intent(activity, MainActivity.class);
//                    //define intent from login so doesnt show splash screen
//                    intent.putExtra("login",true);
//                    activity.startActivity(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Disini bagian jika response jaringan terdapat ganguan/error
                progressDialog.dismiss();
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
                    Toast.makeText(activity, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
            }
        })
        {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);
                params.put("phoneNumber", phoneNumber);

                return params;
            }
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
        progressDialog.dismiss();

        queue.add(stringRequest);

    }

    public void getUserFromDb(int id,User u){
        RequestQueue queue = Volley.newRequestQueue(activity);

        final JsonObjectRequest stringRequest = new JsonObjectRequest(GET, UserAPI.URL_SHOW+id
                , null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Disini bagian jika response jaringan berhasil tidak terdapat ganguan/error
                try {
                    JSONObject data = response.getJSONObject("data");

                    if(response.getString("message").equals("Retrive Data Success")){

                        int id = data.getInt("id");
                        int role_id = data.getInt("role_id");
                        String name = data.getString("name");
                        String avatar = data.getString("avatar");
                        String email = data.getString("email");
                        String phoneNumber = data.getString("phoneNumber");

                        JSONObject role = new JSONObject(data.getString("role"));
                        String role_name = role.getString("name");

                        User user =  new User(id,role_id,name,email,null,phoneNumber,avatar,role_name);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Disini bagian jika response jaringan terdapat ganguan/error
                Toast.makeText(activity, error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        })
        {
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

        //Disini proses penambahan request yang sudah kita buat ke reuest queue yang sudah dideklarasi
        queue.add(stringRequest);
    }

}
