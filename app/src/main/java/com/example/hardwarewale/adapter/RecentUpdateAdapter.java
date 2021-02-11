package com.example.hardwarewale.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hardwarewale.ProductDescriptionActivity;
import com.example.hardwarewale.R;
import com.example.hardwarewale.bean.Product;
import com.example.hardwarewale.databinding.ShowDiscountsBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RecentUpdateAdapter extends RecyclerView.Adapter<RecentUpdateAdapter.RecentUpdateViewHolder> {
    Context context;
    ArrayList<Product> recentProductList;
    private OnRecyclerViewClick listener;

    public RecentUpdateAdapter(Context context, ArrayList<Product> productList){
        this.context = context;
        this.recentProductList = productList;
    }

    @NonNull
    @Override
    public RecentUpdateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ShowDiscountsBinding binding = ShowDiscountsBinding.inflate(LayoutInflater.from(context),parent,false);
        return new RecentUpdateViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentUpdateViewHolder holder, int position) {
         Product product = recentProductList.get(position);
         Picasso.get().load(product.getImageUrl()).placeholder(R.drawable.default_photo_icon).into(holder.binding.ivProductImage);
         holder.binding.tvProductName.setText("" + product.getName());
         holder.binding.tvProductPrice.setTextColor(context.getResources().getColor(R.color.dark_green));
         holder.binding.tvProductPrice.setText("₹ " + product.getPrice());
    }

    @Override
    public int getItemCount() {
        return recentProductList.size();
    }

    public class RecentUpdateViewHolder extends RecyclerView.ViewHolder {
        ShowDiscountsBinding binding;
        public RecentUpdateViewHolder(ShowDiscountsBinding binding){
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Product p = recentProductList.get(position);
                    if(position != RecyclerView.NO_POSITION && listener != null)
                        listener.onItemClick(p,position);
                }
            });
        }
    }

    public interface OnRecyclerViewClick{
        public void onItemClick(Product product, int position);
    }
    public void setOnItemClick(OnRecyclerViewClick listener){
        this.listener = listener;

    }
}
