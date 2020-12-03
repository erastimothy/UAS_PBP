package com.erastimothy.laundry_app.user;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.erastimothy.laundry_app.api.ApiURL;
import com.erastimothy.laundry_app.dao.UserDao;
import com.erastimothy.laundry_app.model.User;
import com.erastimothy.laundry_app.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MyAccountActivity extends AppCompatActivity {
    User user;
    String nama,phoneNumber;
    int id;
    private TextInputEditText name_et,email_et,password_et,phoneNumber_et;
    private ImageView ivAvatar;
    private MaterialButton saveBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        Bundle bundle = getIntent().getExtras();

        name_et = findViewById(R.id.name_et);
        email_et = findViewById(R.id.email_et);
        password_et = findViewById(R.id.password_et);
        phoneNumber_et = findViewById(R.id.phoneNumber_et);
        saveBtn = findViewById(R.id.saveBtn);
        ivAvatar = findViewById(R.id.ivAvatar);

        id = bundle.getInt("id");
        name_et.setText(bundle.getString("name"));
        email_et.setText(bundle.getString("email"));
        phoneNumber_et.setText(bundle.getString("phoneNumber"));

        Glide.with(getApplicationContext())
                .load(ApiURL.URL_IMAGE+bundle.getString("avatar"))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(ivAvatar);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateField()){
                    UserDao userDao = new UserDao(MyAccountActivity.this);
                    userDao.updateUser(id,name_et.getText().toString(),email_et.getText().toString(),password_et.getText().toString(),phoneNumber_et.getText().toString());
                }
            }
        });

    }

    private boolean validateField(){
        if(email_et.getText().toString().equalsIgnoreCase("")){
            Toast.makeText(this, "Email tidak bole kosong", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(name_et.getText().toString().equalsIgnoreCase("")){
            Toast.makeText(this, "Nama tidak bole kosong", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(name_et.getText().length() < 3 ){
            Toast.makeText(this, "Nama minimal 3", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(phoneNumber_et.getText().length()<8 || phoneNumber_et.getText().length()>14){
            Toast.makeText(this, "Phone number 8-14 karakter", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!password_et.getText().toString().equalsIgnoreCase("") && password_et.getText().length()<6){
            Toast.makeText(this, "Password minimal 6", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

//    public void updateUser(View view){
//        TextInputEditText name_et = view.findViewById(R.id.nama_et);
//        TextInputEditText phoneNumber_et = view.findViewById(R.id.phoneNumber_et);
//
//        String _uid = user.getUid();
//        String _name = name_et.getText().toString().trim();
//        String _phoneNumber = phoneNumber_et.getText().toString().trim();
//        user.setName(_name);
//        user.setPhoneNumber(_phoneNumber);
//        UserDao userDao = new UserDao(MyAccountActivity.this);
//        userDao.updateUser(user,_uid);
//        Toast.makeText(MyAccountActivity.this, "Data Berhasil diubah !", Toast.LENGTH_SHORT).show();
//
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(MyAccountActivity.this,UserMainActivity.class));
        finish();
    }
}