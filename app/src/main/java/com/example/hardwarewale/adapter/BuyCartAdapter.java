package com.example.hardwarewale.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.example.hardwarewale.R;
import com.example.hardwarewale.api.CartService;
import com.example.hardwarewale.bean.Cart;
import com.example.hardwarewale.bean.OrderItems;
import com.example.hardwarewale.databinding.CartItemListBinding;
import com.example.hardwarewale.utility.InternetConnectivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuyCartAdapter extends RecyclerView.Adapter<BuyCartAdapter.BuyCartViewHolder> {
    private OnRecyclerViewClick listener;
    Context context;
    ArrayList<Cart> cartList;
    ArrayList<OrderItems> itemList;
    InternetConnectivity connectivity = new InternetConnectivity();
    ProgressDialog pd;
    TextView tvAmt;
    double price, tot, total;

    public BuyCartAdapter(Context context, ArrayList<Cart> cartList, TextView tvAmt) {
        this.context = context;
        this.cartList = cartList;
        this.tvAmt = tvAmt;
    }

    @NonNull
    @Override
    public BuyCartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CartItemListBinding binding = CartItemListBinding.inflate(LayoutInflater.from(context), parent, false);
        return new BuyCartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final BuyCartViewHolder holder, final int position) {
        final Cart cart = cartList.get(position);
        final double qtyInStock = cart.getQtyInStock();
        final int quantity = (int) qtyInStock;
        int qty = 1;
        holder.binding.tvQty.setText("" + qty);
        cart.setQty(qty);

        Picasso.get().load(cart.getImageUrl()).placeholder(R.drawable.default_photo_icon).into(holder.binding.productImage);
        holder.binding.tvProductName.setText("" + cart.getName());

        holder.binding.tvProductPrice.setText("₹ " + cart.getPrice());
        holder.binding.tvProductQty.setText("In stock : " + quantity);

        holder.binding.ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                price = cart.getPrice();
                holder.binding.tvQty.getText().toString();
                int q = Integer.parseInt(holder.binding.tvQty.getText().toString());
                if (q < qtyInStock) {
                    q++;
                    holder.binding.tvQty.setText("" + q);
                    cart.setQty(q);
                    total = Double.parseDouble(tvAmt.getText().toString());
                    total = total + (price);
                    tvAmt.setText("" + total);
                    cart.setTotal(q*price);
                }
            }
        });

        holder.binding.ivSubrtact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                price = cart.getPrice();
                holder.binding.tvQty.getText().toString();
                int q = Integer.parseInt(holder.binding.tvQty.getText().toString());
                if (q > 1) {
                    q--;
                    holder.binding.tvQty.setText("" + q);
                    cart.setQty(q);
                    double total = Double.parseDouble(tvAmt.getText().toString());
                    total = total - (price);
                    tvAmt.setText("" + total);
                    cart.setTotal(q*price);
                }
            }
        });

        holder.binding.ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectivity.isConnectedToInternet(context)) {
                    final AlertDialog ab = new AlertDialog.Builder(context).create();
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = inflater.inflate(R.layout.alert_dialog, null);
                    ab.setView(view);

                    TextView tvtitleMsg = view.findViewById(R.id.tvTilteMsg);
                    tvtitleMsg.setText("You want to remove this product from cart");
                    CardView btnCancel = view.findViewById(R.id.btnCancel);
                    CardView btnOkay = view.findViewById(R.id.btnOkay);

                    btnOkay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pd = new ProgressDialog(context);
                            pd.setTitle("Removing");
                            pd.setMessage("Please wait...");
                            pd.show();
                            CartService.CartApi cartApi = CartService.getCartApiInstance();
                            Call<Cart> call = cartApi.removeProductFormCart(cart.getCartId());
                            call.enqueue(new Callback<Cart>() {
                                @Override
                                public void onResponse(Call<Cart> call, Response<Cart> response) {
                                    Log.e("data", "========>" + response);
                                    if (response.code() == 200) {
                                        Cart c = response.body();
                                        cartList.remove(position);
                                        pd.dismiss();
                                        notifyDataSetChanged();
                                        ab.dismiss();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Cart> call, Throwable t) {
                                    Log.e("failed", "=========>" + t);
                                }
                            });
                        }
                    });
                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ab.dismiss();
                        }
                    });
                    ab.show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }


    public class BuyCartViewHolder extends RecyclerView.ViewHolder {
        CartItemListBinding binding;

        public BuyCartViewHolder(CartItemListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Cart c = cartList.get(position);
                    if (position != RecyclerView.NO_POSITION && listener != null)
                        listener.onItemClick(c, position);
                }
            });
        }
    }

    public interface OnRecyclerViewClick {
        void onItemClick(Cart cart, int position);
    }

    public void setOnItemClicklistner(OnRecyclerViewClick listener) {
        this.listener = listener;
    }
}
