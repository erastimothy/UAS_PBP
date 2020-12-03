package com.erastimothy.laundry_app.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.erastimothy.laundry_app.R;
import com.erastimothy.laundry_app.admin.EditOrderLaundryActivity;
import com.erastimothy.laundry_app.dao.LayananDao;
import com.erastimothy.laundry_app.model.Laundry;
import com.erastimothy.laundry_app.model.Layanan;
import com.erastimothy.laundry_app.model.User;
import com.erastimothy.laundry_app.preferences.LayananPreferences;
import com.erastimothy.laundry_app.preferences.UserPreferences;

import java.util.ArrayList;
import java.util.List;

public class OrderanLaundryAdapter extends RecyclerView.Adapter<OrderanLaundryAdapter.LaundryViewHolder> implements Filterable {
    private Context context;
    private List<Laundry> laundryList;
    private List<Laundry> laundryListFull;
    private LayananDao layananDao;
    private LayananPreferences layananSP;
    private List<Layanan> layananList;
    private UserPreferences userSP;
    private User user;

    public OrderanLaundryAdapter(Context context,List<Laundry> _list){
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
    public OrderanLaundryAdapter.LaundryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_orderan, parent, false);
        return new OrderanLaundryAdapter.LaundryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderanLaundryAdapter.LaundryViewHolder holder, int position) {
        Laundry laundry = laundryList.get(position);

        holder.noOrder_tv.setText(laundry.getId()+" - "+laundry.getDate());
        holder.nama_tv.setText(user.getName());
        holder.total_tv.setText(String.valueOf(laundry.getTotal()));

        if(laundry.getStatus().trim().equalsIgnoreCase("Pesanan Selesai")){
            holder.noOrder_tv.setBackgroundColor(Color.parseColor("#02c39a"));
        }else if(laundry.getStatus().trim().equalsIgnoreCase("Pesanan Batal"))
            holder.noOrder_tv.setBackgroundColor(Color.parseColor("#ff6b6b"));
        else if(laundry.getStatus().trim().equalsIgnoreCase("Menunggu Penjemputan"))
            holder.noOrder_tv.setBackgroundColor(Color.parseColor("#ffe66d"));
        else if(laundry.getStatus().trim().equalsIgnoreCase("Sedang  Diproses"))
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
        TextView noOrder_tv,total_tv,nama_tv;
        LaundryViewHolder(View view){
            super(view);
            noOrder_tv = view.findViewById(R.id.noOrder_tv);
            total_tv = view.findViewById(R.id.total_tv);
            nama_tv = view.findViewById(R.id.nama_tv);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Laundry laundry = laundryList.get(getAdapterPosition());

            Intent intent = new Intent(view.getContext(), EditOrderLaundryActivity.class);
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

            view.getContext().startActivity(intent);

        }
    }
}
