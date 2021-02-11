package com.example.hardwarewale;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hardwarewale.bean.Product;
import com.example.hardwarewale.databinding.BuyProductScreenBinding;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

public class BuyProductActivity extends AppCompatActivity {
    BuyProductScreenBinding binding;
    String brand, name, productId, imageUrl, shopkeeperId, categoryId, description;
    Product product,cart;
    Double price, tot, discount;
    Integer qty = 1, qtyInStock;
    int q;
    long timestamp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = BuyProductScreenBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        Intent in = getIntent();
        product = (Product) in.getSerializableExtra("product");
        cart = product;
        binding.backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if(product!=null) {
            name = product.getName();
            imageUrl = product.getImageUrl();
            description = product.getDescription();
            shopkeeperId = product.getShopkeeperId();
            productId = product.getProductId();
            categoryId = product.getCategoryId();
            brand = product.getBrand();
            discount = product.getDiscount();
            price = product.getPrice();
            qtyInStock = product.getQtyInStock();
            product.setQty(qty);
            timestamp = Calendar.getInstance().getTimeInMillis();

            Picasso.get().load(imageUrl).placeholder(R.drawable.default_photo_icon).into(binding.productImage);
            binding.tvProductName.setText("" + name);
            binding.tvProductPrice.setText("" + price);
            binding.tvProductQty.setText("Available : " + qtyInStock);
            binding.tvQty.getText().toString();

            binding.ivAdd.setColorFilter(getResources().getColor(R.color.dark_green));
            binding.ivAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    q = Integer.parseInt(binding.tvQty.getText().toString());
                    if (q < qtyInStock) {
                        q++;
                        binding.tvQty.setText("" + q);
                        product.setQty(q);
                    }
                    double tot = price * q;
                    product.setTotalAmt(tot);
                }
            });

            binding.ivSubrtact.setColorFilter(getResources().getColor(R.color.red));
            binding.ivSubrtact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    q = Integer.parseInt(binding.tvQty.getText().toString());
                    if (q > 1) {
                        q--;
                        binding.tvQty.setText("" + q);
                    }
                    double qun = (double) q;
                    product.setQty(q);
                    tot = price * q;
                    product.setTotalAmt(tot);
                }
            });
            binding.btnBuy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BuyProductActivity.this, PlaceProductActivity.class);
                    intent.putExtra("p", product);
                    startActivity(intent);
                }
            });
        }
    }
}