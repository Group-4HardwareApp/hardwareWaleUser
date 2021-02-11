package com.example.hardwarewale;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.example.hardwarewale.api.UserService;
import com.example.hardwarewale.bean.User;
import com.example.hardwarewale.databinding.ActivityCreateProfileBinding;
import com.example.hardwarewale.utility.FileUtils;
import com.example.hardwarewale.utility.InternetConnectivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingActivity extends AppCompatActivity {
    ActivityCreateProfileBinding binding;
    SharedPreferences sp = null;
    String currentUserId, userId, token;
    InternetConnectivity connectivity;
    Uri imageUri;
    ProgressDialog pd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent in = getIntent();
        sp = getSharedPreferences("user", MODE_PRIVATE);
        LayoutInflater inflater = LayoutInflater.from(SettingActivity.this);
        binding = ActivityCreateProfileBinding.inflate(inflater);
        View v = binding.getRoot();
        setContentView(v);
        binding.tvActivityName.setText("Update profile");

        binding.backPress.setVisibility(View.VISIBLE);
        binding.backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        UserService.UserApi userApi = UserService.getUserApiInstance();
        Call<User> call = userApi.getUserDetails(currentUserId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    User user = response.body();
                    binding.etMobile.setText("" + user.getMobile());
                    binding.etName.setText("" + user.getName());
                    binding.etAddress.setText("" + user.getAddress());
                    binding.etEmail.setText("" + user.getEmail());
                    Picasso.get().load(user.getImageUrl()).placeholder(R.drawable.default_photo_icon).into(binding.civImage);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });

        binding.tvBtnName.setText("Update");

        binding.civImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(SettingActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SettingActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 11);
                } else {
                    Intent in = new Intent();
                    in.setAction(Intent.ACTION_GET_CONTENT);
                    in.setType("image/*");
                    startActivityForResult(Intent.createChooser(in, "Select image"), 111);
                }
            }
        });

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectivity = new InternetConnectivity();
                if (!connectivity.isConnectedToInternet(SettingActivity.this)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                    builder.setMessage("Please connect to the Internet to Proceed Further").setCancelable(false);
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            System.exit(0);
                        }
                    }).setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent in = new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
                            startActivity(in);
                        }
                    });
                    builder.show();
                } else {
                    try {
                        String address = binding.etAddress.getText().toString();
                        String name = binding.etName.getText().toString();
                        String email = binding.etEmail.getText().toString();
                        String number = binding.etMobile.getText().toString();
                        if (TextUtils.isEmpty(name)) {
                            binding.etName.setError("Enter name");
                            return;
                        }
                        if (TextUtils.isEmpty(address)) {
                            binding.etAddress.setError("Enter Address");
                            return;
                        }
                        if (TextUtils.isEmpty(email)) {
                            binding.etEmail.setError("Enter Email");
                            return;
                        }
                        if (TextUtils.isEmpty(number)) {
                            binding.etMobile.setError("Enter Number");
                            return;
                        }
                        userId = sp.getString("userId", "");
                        token = sp.getString("token", "");
                        if (imageUri != null) {
                            pd = new ProgressDialog(SettingActivity.this);
                            pd.setTitle("Updating");
                            pd.setMessage("Please wait");
                            pd.show();
                            File file = FileUtils.getFile(SettingActivity.this, imageUri);
                            RequestBody requestFile = RequestBody.create(MediaType.parse(Objects.
                                    requireNonNull(getContentResolver().getType(imageUri))), file);

                            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
                            RequestBody userName = RequestBody.create(okhttp3.MultipartBody.FORM, name);
                            RequestBody userNumber = RequestBody.create(okhttp3.MultipartBody.FORM, number);
                            RequestBody userEmail = RequestBody.create(okhttp3.MultipartBody.FORM, email);
                            RequestBody userAddress = RequestBody.create(okhttp3.MultipartBody.FORM, address);
                            RequestBody userToken = RequestBody.create(okhttp3.MultipartBody.FORM, token);
                            RequestBody userID = RequestBody.create(okhttp3.MultipartBody.FORM, userId);

                            UserService.UserApi serviceApi = UserService.getUserApiInstance();
                            Call<User> call = serviceApi.updateUser(body, userID, userName, userNumber, userEmail, userAddress, userToken);
                            call.enqueue(new Callback<User>() {
                                @Override
                                public void onResponse(Call<User> call, Response<User> response) {
                                    if (response.code() == 200) {
                                        pd.dismiss();
                                        User user = response.body();
                                        SharedPreferences.Editor editor = sp.edit();
                                        editor.putString("userId", user.getUserId());
                                        editor.putString("address", user.getAddress());
                                        editor.putString("email", user.getEmail());
                                        editor.putString("mobile", user.getMobile());
                                        editor.putString("token", user.getToken());
                                        editor.putString("imageUrl", user.getImageUrl());
                                        editor.putString("name", user.getName());
                                        editor.commit();
                                        Toast.makeText(SettingActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }

                                @Override
                                public void onFailure(Call<User> call, Throwable t) {

                                }
                            });
                        } else {
                            pd = new ProgressDialog(SettingActivity.this);
                            pd.setTitle("Updating");
                            pd.setMessage("Please wait...");
                            pd.show();
                            User s = new User(userId, name, address, number, email, token);
                            UserService.UserApi serviceApi = UserService.getUserApiInstance();
                            Call<User> call = serviceApi.updateUserWithoutImage(s);
                            call.enqueue(new Callback<User>() {
                                @Override
                                public void onResponse(Call<User> call, Response<User> response) {
                                    if (response.code() == 200) {
                                        pd.dismiss();
                                        User user = response.body();
                                        SharedPreferences.Editor editor = sp.edit();
                                        Gson gson = new Gson();
                                        String json = gson.toJson(user);
                                        editor.putString(currentUserId, json);
                                        editor.commit();
                                        Toast.makeText(SettingActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }

                                @Override
                                public void onFailure(Call<User> call, Throwable t) {
                                    Log.e("==>", "==>" + t);
                                    Toast.makeText(SettingActivity.this, "" + t, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("Error ", "==> " + e);
                        Toast.makeText(SettingActivity.this, "" + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(binding.civImage);
            Toast.makeText(this, "" + imageUri, Toast.LENGTH_SHORT).show();
        }
    }

    private void initComponent() {
        binding.toolbar.setTitle("User");
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
