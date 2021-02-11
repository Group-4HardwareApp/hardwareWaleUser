package com.example.hardwarewale;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hardwarewale.adapter.BuyCartAdapter;
import com.example.hardwarewale.api.CartService;
import com.example.hardwarewale.bean.BuyCart;
import com.example.hardwarewale.bean.Cart;
import com.example.hardwarewale.databinding.BuyScreenBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuyActivity extends AppCompatActivity {
    BuyScreenBinding binding;
    ArrayList<Cart> cartList, updatedCartList;
    BuyCart buyCart;
    String currentUserId;
    BuyCartAdapter adapter;
    double total = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = BuyScreenBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        Intent in = getIntent();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        cartList = (ArrayList<Cart>) in.getSerializableExtra("cartlist");
        buyCart = new BuyCart();
        buyCart.setCartList(cartList);

        binding.backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        CartService.CartApi cartApi = CartService.getCartApiInstance();
        Call<BuyCart> call = cartApi.getCartProductWithQty(buyCart);
        call.enqueue(new Callback<BuyCart>() {
            @Override
            public void onResponse(Call<BuyCart> call, Response<BuyCart> response) {
                Log.e("Response code", "=========>" + response.code());
                if (response.code() == 200) {
                    cartList = new ArrayList<Cart>();
                    buyCart = response.body();
                    updatedCartList = buyCart.getCartList();
                    Log.e("cart list size", "=========>" + updatedCartList.size());
                    adapter = new BuyCartAdapter(BuyActivity.this, updatedCartList, binding.tvAmt);
                    binding.rvBuy.setLayoutManager(new LinearLayoutManager(BuyActivity.this));
                    binding.rvBuy.setAdapter(adapter);

                    for (Cart c : updatedCartList) {
                        total = total + c.getPrice();
                        binding.tvAmt.setText("" + total);
                        Log.e("shopkeeper id", "==>" + c.getShopkeeperId());
                    }
                }
            }

            @Override
            public void onFailure(Call<BuyCart> call, Throwable t) {
                Log.e("Error : ", "" + t);
                Toast.makeText(BuyActivity.this, "" + t, Toast.LENGTH_SHORT).show();
            }
        });

        binding.ivContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (updatedCartList.size() != 0) {
                    Intent in = new Intent(BuyActivity.this, PlaceOrderActivity.class);
                    in.putExtra("updatedCartList", updatedCartList);
                    String total = binding.tvAmt.getText().toString();
                    double amount = Double.parseDouble(total);
                    in.putExtra("total", amount);
                    startActivity(in);
                }
            }
        });

        binding.ivBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewBill bill = new ViewBill(updatedCartList);
                bill.show(getSupportFragmentManager(), "show bill");
            }
        });
    }
}