package com.erastimothy.laundry_app.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.erastimothy.laundry_app.R;
import com.erastimothy.laundry_app.dao.LayananDao;
import com.erastimothy.laundry_app.model.Laundry;
import com.erastimothy.laundry_app.model.Layanan;
import com.erastimothy.laundry_app.model.User;
import com.erastimothy.laundry_app.preferences.LayananPreferences;
import com.erastimothy.laundry_app.preferences.UserPreferences;
import com.erastimothy.laundry_app.user.OrderDetailActivity;
import com.erastimothy.laundry_app.user.OrderLaundryActivity;
import com.erastimothy.laundry_app.user.UserMainActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LaundryRecyclerViewAdapter extends RecyclerView.Adapter<LaundryRecyclerViewAdapter.LaundryViewHolder> implements Filterable {
    private Context context;
    private List<Laundry> laundryList;
    private List<Laundry> laundryListFull;
    private LayananDao layananDao;
    private LayananPreferences layananSP;
    private List<Layanan> layananList;
    private UserPreferences userSP;
    private User user;

    public LaundryRecyclerViewAdapter(Context context,List<Laundry> _list){
        this.laundryList = _list;
        this.laundryListFull = new ArrayList<>(laundryList);
        this.context = context;
        userSP = new UserPreferences(context);
        user = userSP.getUserLoginFromSharedPrefernces();

        layananSP = new LayananPreferences(context);
        layananDao = new LayananDao(context);

        layananDao.setAllDataLayanan();
        layananList = layananSP.getListLayananFromSharedPreferences();
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public LaundryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_myorder, parent, false);
        return new LaundryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LaundryRecyclerViewAdapter.LaundryViewHolder holder, int position) {
        Laundry laundry = laundryList.get(position);
        holder.noOrder_tv.setText(String.valueOf(laundry.getId()));
        holder.total_tv.setText(String.valueOf(laundry.getTotal()));
        holder.status_tv.setText(laundry.getStatus());

        if(laundry.getStatus().trim().equalsIgnoreCase("Pesanan Selesai")){
            holder.noOrder_tv.setBackgroundColor(Color.parseColor("#02c39a"));
        }else if(laundry.getStatus().trim().equalsIgnoreCase("Pesanan Batal"))
            holder.noOrder_tv.setBackgroundColor(Color.parseColor("#ff6b6b"));
        else if(laundry.getStatus().trim().equalsIgnoreCase("Menunggu Penjemputan"))
            holder.noOrder_tv.setBackgroundColor(Color.parseColor("#ffe66d"));
        else if(laundry.getStatus().trim().equalsIgnoreCase("Pesanan Sedang Diproses"))
            holder.noOrder_tv.setBackgroundColor(Color.parseColor("#00a8e8"));
    }

    @Override
    public int getItemCount() {
        return laundryList.size();
    }

    @Override
    public Filter getFilter() {
        return laundryFilter;
    }

    private Filter laundryFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Laundry> filterLaundryList = new ArrayList<>();

            if(charSequence == null || charSequence.length() == 0 || charSequence.toString().equalsIgnoreCase("")){
                filterLaundryList.addAll(laundryListFull);
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();

                Layanan layananTemp = new Layanan();

                for (Laundry item : laundryList){

                    for (int i =0 ;i< layananList.size(); i++){
                        if(layananList.get(i).getId() == item.getService_id()){
                            layananTemp = layananList.get(i);
                        }
                    }

                    if(String.valueOf(item.getId()).toLowerCase().contains(filterPattern) || item.getStatus().toLowerCase().contains(filterPattern) ||  layananTemp.getName().toLowerCase().contains(filterPattern)) {
                        filterLaundryList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filterLaundryList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            laundryList.clear();
            laundryList.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public class LaundryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView noOrder_tv,total_tv,status_tv;
        LaundryViewHolder(View view){
            super(view);
            noOrder_tv = view.findViewById(R.id.noOrder_tv);
            total_tv = view.findViewById(R.id.total_tv);
            status_tv = view.findViewById(R.id.status_tv);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Laundry laundry = laundryList.get(getAdapterPosition());

            Intent intent = new Intent(view.getContext(), OrderDetailActivity.class);
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
            bundle.putString("service_id",String.valueOf(layananTemp.getId()));
            bundle.putString("kuantitas", String.valueOf(laundry.getQuantity()));
            bundle.putString("order_id",String.valueOf(laundry.getId()));
            bundle.putString("nama",user.getName());
            bundle.putString("tanggal",laundry.getDate());
            bundle.putString("id",String.valueOf(laundry.getId()));
            bundle.putString("status",laundry.getStatus());
            intent.putExtra("laundry",bundle);

            view.getContext().startActivity(intent);

        }
    }
}
