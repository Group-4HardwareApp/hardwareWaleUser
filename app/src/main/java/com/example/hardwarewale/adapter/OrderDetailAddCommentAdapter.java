package com.example.hardwarewale.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hardwarewale.R;
import com.example.hardwarewale.api.CommentService;
import com.example.hardwarewale.api.UserService;
import com.example.hardwarewale.bean.Comment;
import com.example.hardwarewale.bean.OrderItems;
import com.example.hardwarewale.bean.Product;
import com.example.hardwarewale.bean.User;
import com.example.hardwarewale.databinding.OrderDetailItemListBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailAddCommentAdapter extends RecyclerView.Adapter<OrderDetailAddCommentAdapter.OrderDetailViewHolder> {
    ArrayList<OrderItems> itemList;
    Context context;
    String userId, rating, date, text, productId, commentId, userName, userImag, coment;
    int flag = 0;
    long timestamp;

    public OrderDetailAddCommentAdapter(Context context, ArrayList<OrderItems> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        OrderDetailItemListBinding binding = OrderDetailItemListBinding.inflate(LayoutInflater.from(context), parent, false);
        return new OrderDetailViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final OrderDetailViewHolder holder, int position) {
        final OrderItems item = itemList.get(position);
        holder.binding.tvProductName.setText("" + item.getName());
        holder.binding.tvProductName.setTextColor(context.getResources().getColor(R.color.black));
        holder.binding.tvProductPrice.setText("₹ " + item.getPrice());
        //holder.binding.tvProductPrice.setTextColor(context.getResources().getColor(R.color.red));
        holder.binding.tvProductQty.setText("Qty : " + item.getQty());
        holder.binding.tvProductQty.setTextColor(context.getResources().getColor(R.color.black));
        Picasso.get().load(item.getImageUrl()).placeholder(R.drawable.default_photo_icon).into(holder.binding.ivProductImage);

        SpannableString content = new SpannableString("Add Comment");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        holder.binding.tvAddComment.setText(content);
        holder.binding.tvAddComment.setVisibility(View.VISIBLE);

        getCommnets();
        if (flag == 1) {
            SpannableString content1 = new SpannableString("Update comment");
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            holder.binding.tvAddComment.setText(content1);

        }

        holder.binding.tvAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog ab = new AlertDialog.Builder(context).create();
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View view = inflater.inflate(R.layout.activity_rating, null);
                ab.setView(view);

                final EditText etComment = view.findViewById(R.id.etComment);
                final RatingBar ratingBar = view.findViewById(R.id.ratingBar);
                final ImageView ivCancel = view.findViewById(R.id.ivCancel);
                final CardView btnComment = view.findViewById(R.id.btnAddComment);

                userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                productId = item.getProductId();

                Calendar cDate = Calendar.getInstance();
                SimpleDateFormat sd = new SimpleDateFormat("dd/MM/yyyy");
                date = sd.format(cDate.getTime());
                timestamp = Calendar.getInstance().getTimeInMillis();

                UserService.UserApi userApi = UserService.getUserApiInstance();
                Call<User> call = userApi.getUserDetails(userId);
                call.enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.code() == 200) {
                            User user = response.body();
                            userName = user.getName();
                            userImag = user.getImageUrl();
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Toast.makeText(context, "" + t, Toast.LENGTH_SHORT).show();
                    }
                });
                btnComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        coment = etComment.getText().toString().trim();
                        if (TextUtils.isEmpty(coment)) {
                            etComment.setError("Enter comment");
                            return;
                        }
                        Comment comment = new Comment();
                        comment.setProductId(item.getProductId());
                        comment.setTimestamp(timestamp);
                        comment.setComment(coment);
                        comment.setDate(date);
                        Float rating = Float.valueOf((Float) ratingBar.getRating());
                        comment.setRating(rating);
                        Log.e("Rating", "=====>" + rating);

                        comment.setUserId(userId);
                        comment.setUserImg(userImag);
                        comment.setUserName(userName);
                        CommentService.CommentApi commentApi = CommentService.getCommentApiInstance();
                        Call<Comment> call = commentApi.commentProduct(comment);
                        call.enqueue(new Callback<Comment>() {
                            @Override
                            public void onResponse(Call<Comment> call, Response<Comment> response) {
                                if (response.code() == 200) {
                                    Comment comment = response.body();
                                    ab.dismiss();
                                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Comment> call, Throwable t) {
                                Log.e("Failure", "==>" + t);
                            }
                        });
                    }
                });

                ivCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ab.dismiss();
                    }
                });
                ab.show();
            }
        });
    }

    private void getCommnets() {
        CommentService.CommentApi api = CommentService.getCommentApiInstance();
        Call<ArrayList<Comment>> call2 = api.getCommentOfProduct(productId);
        call2.enqueue(new Callback<ArrayList<Comment>>() {
            @Override
            public void onResponse(Call<ArrayList<Comment>> call, Response<ArrayList<Comment>> response) {
                if (response.isSuccessful()) {
                    ArrayList<Comment> commentList = response.body();
                    for (Comment c : commentList) {
                        commentId = c.getCommentId();
                        if (userId.equals(c.getUserId())) {
                            flag = 1;
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Comment>> call, Throwable t) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class OrderDetailViewHolder extends RecyclerView.ViewHolder {
        OrderDetailItemListBinding binding;

        public OrderDetailViewHolder(OrderDetailItemListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
