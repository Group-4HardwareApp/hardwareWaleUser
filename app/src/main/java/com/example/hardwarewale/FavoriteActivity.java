package com.example.hardwarewale;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.hardwarewale.adapter.FavoriteAdapter;
import com.example.hardwarewale.api.FavoriteService;
import com.example.hardwarewale.bean.Favorite;
import com.example.hardwarewale.databinding.FavoriteScreenBinding;
import com.example.hardwarewale.utility.InternetConnectivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteActivity extends AppCompatActivity {
    FavoriteScreenBinding binding;
    FavoriteAdapter adapter;
    String currentUserId;
    InternetConnectivity connectivity = new InternetConnectivity();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FavoriteScreenBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        Intent in = new Intent();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        showFavoriteProduct();

        binding.backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void showFavoriteProduct() {
        if (connectivity.isConnectedToInternet(this)) {
            FavoriteService.FavoriteApi favoriteApi = FavoriteService.getFavoriteApiInstance();
            Call<List<Favorite>> call = favoriteApi.getFavorite(currentUserId);
            call.enqueue(new Callback<List<Favorite>>() {
                @Override
                public void onResponse(Call<List<Favorite>> call, Response<List<Favorite>> response) {
                    if (response.code() == 200) {
                        List<Favorite> favoriteList = response.body();
                        for (Favorite f : favoriteList)
                            Log.e("======", "data" + f);
                        if(favoriteList.size() == 0){
                            binding.favrouiteLayout.setVisibility(View.VISIBLE);
                            binding.rvFavoriteScreen.setVisibility(View.GONE);
                            binding.btnbuy.setVisibility(View.GONE);
                        }
                        adapter = new FavoriteAdapter(FavoriteActivity.this, favoriteList);
                        binding.rvFavoriteScreen.setAdapter(adapter);
                        binding.rvFavoriteScreen.setLayoutManager(new LinearLayoutManager(FavoriteActivity.this));

                        adapter.setOnItemClicklistner(new FavoriteAdapter.OnRecyclerViewClick() {
                            @Override
                            public void onItemClick(Favorite favorite, int position) {
                                Intent in = new Intent(FavoriteActivity.this, FavoriteProductDescription.class);
                                in.putExtra("favorite",favorite);
                                startActivity(in);
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<List<Favorite>> call, Throwable t) {
                    Toast.makeText(FavoriteActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }    else
            Toast.makeText(this, "Please Connect Your Internet", Toast.LENGTH_SHORT).show();
    }
}