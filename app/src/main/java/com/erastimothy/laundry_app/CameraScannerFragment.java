package com.erastimothy.laundry_app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.PermissionRequest;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.erastimothy.laundry_app.admin.EditOrderLaundryActivity;
import com.erastimothy.laundry_app.dao.LaundryDao;
import com.erastimothy.laundry_app.dao.LayananDao;
import com.erastimothy.laundry_app.model.Laundry;
import com.erastimothy.laundry_app.model.Layanan;
import com.erastimothy.laundry_app.model.User;
import com.erastimothy.laundry_app.preferences.LaundryPreferences;
import com.erastimothy.laundry_app.preferences.LayananPreferences;
import com.erastimothy.laundry_app.preferences.UserPreferences;
import com.erastimothy.laundry_app.user.OrderLaundryActivity;
import com.google.zxing.Result;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.ArrayList;
import java.util.List;

public class CameraScannerFragment extends Fragment {

    private CodeScanner mCodeScanner;
    private final int REQUEST_CAMERA_CODE = 102;
    private int status_permission = 0;
    private List<Laundry> laundryList;
    private LayananDao layananDao;
    private LayananPreferences layananSP;
    private List<Layanan> layananList;
    private UserPreferences userSP;
    private User user;

    public CameraScannerFragment() {
        // Required empty public constructor
    }

    public static CameraScannerFragment newInstance(String param1, String param2) {
        CameraScannerFragment fragment = new CameraScannerFragment();
        Bundle args = new Bundle();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        laundryList = new ArrayList<>();

        userSP = new UserPreferences(getContext());
        user = userSP.getUserLoginFromSharedPrefernces();
        layananSP = new LayananPreferences(getActivity());
        layananDao = new LayananDao(getActivity());

        layananDao.setAllDataLayanan();
        layananList = layananSP.getListLayananFromSharedPreferences();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final Activity activity = getActivity();

        if(activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA},REQUEST_CAMERA_CODE);
            if(activity.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                status_permission = 1;
        }else {
            status_permission = 1;
        }

        View root = inflater.inflate(R.layout.fragment_camera_scanner, container, false);
        CodeScannerView scannerView = root.findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(activity, scannerView);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String order_id = result.getText();
                        List<Laundry> laundryList = new ArrayList<>();
                        LaundryDao laundryDao = new LaundryDao(getActivity());
                        laundryDao.setAllDataLaundry();
                        LaundryPreferences laundryPreferences = new LaundryPreferences(getActivity());
                        laundryList = laundryPreferences.getListLaundryFromSharedPreferences();
                        Laundry laundry = new Laundry();

                        for (int i=0; i < laundryList.size(); i++){
                            if(laundryList.get(i).getId() == Integer.parseInt(order_id)){
                                laundry = laundryList.get(i);
                            }
                        }

                        if(laundry != null){
                            Intent intent = new Intent(getContext(), EditOrderLaundryActivity.class);
                            Bundle bundle = new Bundle();


                            Layanan layananTemp = new Layanan();

                            for (int i =0 ;i< layananList.size(); i++){
                                if(layananList.get(i).getId() == laundry.getService_id()){
                                    layananTemp = layananList.get(i);
                                }
                            }

                            bundle.putString("alamat",laundry.getAddress());
                            bundle.putString("biaya_antar",String.valueOf(laundry.getShippingcost()));
                            bundle.putString("harga",String.valueOf(layananTemp.getHarga()));
                            bundle.putString("total_pembayaran",String.valueOf(laundry.getTotal()));
                            bundle.putString("jenis",layananTemp.getName());
                            bundle.putString("kuantitas", String.valueOf(laundry.getQuantity()));
                            bundle.putString("order_id",String.valueOf(laundry.getId()));
                            bundle.putString("nama",user.getName());
                            bundle.putString("tanggal",laundry.getDate());
                            bundle.putString("uid",String.valueOf(laundry.getId()));
                            bundle.putString("status",laundry.getStatus());
                            intent.putExtra("laundry",bundle);

                            getActivity().startActivity(intent);
                        }else {
                            Toast.makeText(activity, "Cannot find Order of this QR code", Toast.LENGTH_SHORT).show();
                        }


                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(status_permission==1)
            mCodeScanner.startPreview();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(status_permission==1)
            mCodeScanner.releaseResources();
    }

}