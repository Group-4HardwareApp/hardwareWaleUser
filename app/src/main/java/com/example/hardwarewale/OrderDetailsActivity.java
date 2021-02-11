package com.example.hardwarewale;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.hardwarewale.adapter.OrderDetailAdapter;
import com.example.hardwarewale.bean.OrderItems;
import com.example.hardwarewale.databinding.OrderDetailItemListBinding;
import com.example.hardwarewale.databinding.OrderDetailScreenBinding;

import java.util.ArrayList;

public class OrderDetailsActivity extends AppCompatActivity {
    OrderDetailScreenBinding binding;
    OrderDetailItemListBinding itemBinding;
    OrderDetailAdapter adapter;
    ArrayList<OrderItems> items;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = OrderDetailScreenBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        Intent in = getIntent();
        items = (ArrayList<OrderItems>) in.getSerializableExtra("item");
        
        for(OrderItems o : items) {
            Log.e("items : " , "==>" + o);

            adapter = new OrderDetailAdapter(this, items);
            binding.rvOrderDetails.setAdapter(adapter);
            binding.rvOrderDetails.setLayoutManager(new LinearLayoutManager(this));
        }
/*
        binding.btnbuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(OrderDetailsActivity.this,ReOrderActivity.class);
                //in.putExtra("items",);
            }
        });
*/
        binding.backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}