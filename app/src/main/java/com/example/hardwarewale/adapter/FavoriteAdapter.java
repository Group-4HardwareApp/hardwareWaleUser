package com.example.hardwarewale.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hardwarewale.FavoriteActivity;
import com.example.hardwarewale.api.CartService;
import com.example.hardwarewale.api.FavoriteService;
import com.example.hardwarewale.bean.Cart;
import com.example.hardwarewale.bean.Favorite;
import com.example.hardwarewale.databinding.ActivityCartItemBinding;
import com.example.hardwarewale.databinding.ActivityFavoriteItemBinding;
import com.example.hardwarewale.utility.InternetConnectivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {
    List<Favorite> favoriteList;
    Context context;
    private OnRecyclerViewClick listener;
    ProgressDialog pd;
    InternetConnectivity connectivity = new InternetConnectivity();

    public FavoriteAdapter(Context context, List<Favorite> favoriteList) {
        this.context = context;
        this.favoriteList = favoriteList;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ActivityFavoriteItemBinding binding = ActivityFavoriteItemBinding.inflate(LayoutInflater.from(context), parent, false);
        return new FavoriteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, final int position) {
        final Favorite favorite = favoriteList.get(position);
        Picasso.get().load(favorite.getImageUrl()).into(holder.binding.productImage);
        holder.binding.tvProductName.setText("" + favorite.getName());
        holder.binding.tvProductPrice.setText("₹" + favorite.getPrice());
        holder.binding.tvProductDescription.setText("" + favorite.getDescription());
        holder.binding.ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectivity.isConnectedToInternet(context)) {
                    AlertDialog.Builder ab = new AlertDialog.Builder(context);
                    ab.setMessage("Are you sure ?");
                    ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            pd = new ProgressDialog(context);
                            pd.setTitle("Removing");
                            pd.setMessage("Please wait...");
                            pd.show();
                            FavoriteService.FavoriteApi favoriteApi = FavoriteService.getFavoriteApiInstance();
                            Call<Favorite> call = favoriteApi.removeFavorite(favorite.getFavoriteId());
                            call.enqueue(new Callback<Favorite>() {
                                @Override
                                public void onResponse(Call<Favorite> call, Response<Favorite> response) {
                                    if (response.code() == 200) {
                                        Favorite favorite1 = response.body();
                                        Log.e("==========", "Seccessful" + response);
                                        favoriteList.remove(position);
                                        pd.dismiss();
                                        notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Favorite> call, Throwable t) {
                                    Log.e("failed", "=========" + t);
                                }
                            });
                        }
                    });
                    ab.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    ab.show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    public class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ActivityFavoriteItemBinding binding;
        public FavoriteViewHolder(ActivityFavoriteItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Favorite f = favoriteList.get(position);
                    if (position != RecyclerView.NO_POSITION && listener != null)
                        listener.onItemClick(f, position);
                }
            });
        }
    }


    public interface OnRecyclerViewClick {
        void onItemClick(Favorite favorite, int position);
    }

    public void setOnItemClicklistner(OnRecyclerViewClick listener) {
        this.listener = listener;
    }
}
