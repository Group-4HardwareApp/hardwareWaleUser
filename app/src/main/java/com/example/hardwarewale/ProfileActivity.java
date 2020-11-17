 package com.example.hardwarewale;
 import androidx.annotation.Nullable;
 import androidx.appcompat.app.AppCompatActivity;
 import androidx.core.app.ActivityCompat;
 import androidx.core.content.PermissionChecker;

 import android.Manifest;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;

 import android.net.NetworkInfo;
 import android.net.Uri;

 import android.os.Bundle;
 import android.util.Log;
 import android.util.Patterns;
 import android.view.LayoutInflater;
 import android.view.View;

 import android.widget.Toast;


 import com.basgeekball.awesomevalidation.AwesomeValidation;
 import com.example.hardwarewale.api.UserService;
 import com.example.hardwarewale.bean.User;
 import com.example.hardwarewale.databinding.ActivityCreateProfileBinding;


 import java.io.File;

 import okhttp3.MediaType;
 import okhttp3.MultipartBody;
 import okhttp3.RequestBody;
 import retrofit2.Call;
 import retrofit2.Callback;
 import retrofit2.Response;

 import static com.basgeekball.awesomevalidation.ValidationStyle.BASIC;


 public class ProfileActivity extends AppCompatActivity {

     Uri imageUri;
     ActivityCreateProfileBinding binding;
     SharedPreferences sp = null;
     AwesomeValidation awesomeValidation;
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         LayoutInflater inflater = LayoutInflater.from(ProfileActivity.this);
         binding =  binding.inflate(inflater);
         sp = getSharedPreferences("user",MODE_PRIVATE);
         View view = binding.getRoot();
         setContentView(view);
         awesomeValidation  = new AwesomeValidation(BASIC);

         binding.etbtnEdit.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if(ActivityCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PermissionChecker.PERMISSION_GRANTED){
                     ActivityCompat.requestPermissions(ProfileActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},11);
                 }
                 else {
                     Intent in = new Intent();
                     in.setAction(Intent.ACTION_GET_CONTENT);
                     in.setType("image/*");
                     startActivityForResult(in, 1);
                 }
             }
         });

         awesomeValidation.addValidation(binding.etName,"[^\\s*$][a-zA-Z\\s]+","Enter correct name");
         awesomeValidation.addValidation(binding.etEmail, Patterns.EMAIL_ADDRESS,"Enter correct Email");
         awesomeValidation.addValidation(binding.etMobile,"^[0-9]{10}$","Enter correct  Mobile number");
         awesomeValidation.addValidation(binding.etAddress,"[^\\s*$][a-zA-Z0-9,/\\s]+","Enter correct Address");

         binding.btnSave.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (isConnected()) {
                     Log.e("isConnected","TRUE");
                     Log.e("VALIDATION",""+awesomeValidation.validate());

                     if(awesomeValidation.validate())
                     {
                         String address =binding.etAddress.getText().toString().trim();
                         String email = binding.etEmail.getText().toString().trim();
                         String name = binding.etName.getText().toString().trim();
                         String mobile = binding.etMobile.getText().toString().trim();
                         String token = "tok_qe2k3";
                         if(imageUri!=null){
                             File file =  FileUtils.getFile(ProfileActivity.this,imageUri);
                             RequestBody requestFile =
                                     RequestBody.create(
                                             MediaType.parse(getContentResolver().getType(imageUri)),
                                             file
                                     );

                             MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
                             RequestBody username = RequestBody.create(okhttp3.MultipartBody.FORM,name);
                             RequestBody useraddress = RequestBody.create(okhttp3.MultipartBody.FORM,address);
                             RequestBody useremail = RequestBody.create(okhttp3.MultipartBody.FORM,email);
                             RequestBody usermobile = RequestBody.create(okhttp3.MultipartBody.FORM,mobile);
                             RequestBody usertoken = RequestBody.create(okhttp3.MultipartBody.FORM,token);

                             UserService.UserApi userApi =UserService.getUserApiInstance();
                             Call<User> call = userApi.saveUser(body,username,usermobile,useraddress,useremail,usertoken);
                             call.enqueue(new Callback<User>() {
                                 @Override
                                 public void onResponse(Call<User> call, Response<User> response) {
                                     if(response.code()==200) {
                                         User user = response.body();
                                         Toast.makeText(ProfileActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                         SharedPreferences.Editor editor = sp.edit();
                                         editor.putString("name",user.getName());
                                         editor.putString("token",user.getToken());
                                         editor.putString("userId",user.getUserId());
                                         editor.putString("mobile",user.getMobile());
                                         editor.putString("imageUrl",user.getImageUrl());
                                         editor.putString("address",user.getAddress());
                                         editor.putString("email",user.getEmail());
                                         editor.commit();
                                         finish();
                                     }
                                     else
                                         Toast.makeText(ProfileActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                                 }

                                 @Override
                                 public void onFailure(Call<User> call, Throwable t) {
                                     Toast.makeText(ProfileActivity.this, "Failure", Toast.LENGTH_SHORT).show();
                                     Log.e("THROWED",""+t);
                                 }
                             });
                         }
                         else
                         {
                             Toast.makeText(ProfileActivity.this, "Please select image", Toast.LENGTH_SHORT).show();
                         }

                     }
                     else
                     {
                         Toast.makeText(ProfileActivity.this, "Enter Correct input", Toast.LENGTH_SHORT).show();
                     }
                 }
                 else {
                     Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                 }
             }
         });
     }


     @Override
     protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if(requestCode == 1 && resultCode == RESULT_OK){
             imageUri = data.getData();
             binding.etbtnEdit.setImageURI(imageUri);
             Toast.makeText(this, "FIRST"+imageUri, Toast.LENGTH_SHORT).show();
             binding.civImage.setImageURI(imageUri);
         }
     }
     public boolean isConnected() {
         boolean connected = false;
         try {
             ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
             NetworkInfo nInfo = cm.getActiveNetworkInfo();
             connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
             return connected;
         } catch (Exception e) {
             Log.e("Connectivity Exception", e.getMessage());
         }
         return connected;
     }

 }

/*import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.example.hardwarewale.api.UserService;
import com.example.hardwarewale.bean.User;
import com.example.hardwarewale.databinding.ActivityCreateProfileBinding;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.io.File;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    ActivityCreateProfileBinding binding;
    Uri croppedImageUri;
AwesomeValidation awesomeValidation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateProfileBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
    awesomeValidation = new AwesomeValidation(ValidationStyle.BASIC);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PermissionChecker.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},11);
        }
        binding.etbtnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(ProfileActivity.this);

            }
        });
        awesomeValidation.addValidation(binding.etName,"[^\\s*$][a-zA-Z\\s]+","Enter correct name");
        awesomeValidation.addValidation(binding.etEmail, Patterns.EMAIL_ADDRESS,"Enter correct Email");
        awesomeValidation.addValidation(binding.etMobile,"^[0-9]{10}$","Enter correct  Mobile number");
        awesomeValidation.addValidation(binding.etAddress,"[^\\s*$][a-zA-Z0-9,/\\s]+","Enter correct Address");

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   if (isConnected()) {

                       if (awesomeValidation.validate()) {
                           String name = binding.etName.getText().toString();
                           String mobile = binding.etMobile.getText().toString();
                           String address = binding.etAddress.getText().toString();
                           String email = binding.etEmail.getText().toString();
                           String token = "gf57bjn8";

                           if (croppedImageUri != null) {
                               File file = FileUtils.getFile(ProfileActivity.this, croppedImageUri);
                               RequestBody requestFile =
                                       RequestBody.create(
                                               MediaType.parse("image/*"),
                                               file);
                               MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
                               RequestBody userName = RequestBody.create(MultipartBody.FORM, name);
                               RequestBody userAddress = RequestBody.create(MultipartBody.FORM, address);
                               RequestBody userMobile = RequestBody.create(MultipartBody.FORM, mobile);
                               RequestBody userEmail = RequestBody.create(MultipartBody.FORM, email);
                               RequestBody userToken = RequestBody.create(MultipartBody.FORM, token);
                               UserService.UserApi userApi = UserService.getUserApiInstance();

                               Call<User>call = userApi.saveUser(body, userName, userAddress, userMobile, userEmail, userToken);
                               call.enqueue(new Callback<User>() {
                                   @Override
                                   public void onResponse(Call<User> call, Response<User> response) {

                                       User user = response.body();
                                       Toast.makeText(ProfileActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                   }

                                   @Override
                                   public void onFailure(Call<User> call, Throwable t) {
                                       Toast.makeText(ProfileActivity.this, "Failure"+t, Toast.LENGTH_SHORT).show();
                                       Log.e("THROWED",""+t);
                                   }
                               });
                           }
                           else
                           {
                               Toast.makeText(ProfileActivity.this, "Please select image", Toast.LENGTH_SHORT).show();
                           }

                       }
                       else
                       {
                           Toast.makeText(ProfileActivity.this, "Enter Correct input", Toast.LENGTH_SHORT).show();
                       }
                   }
                   else {
                       Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                   }
               }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                croppedImageUri = result.getUri();
                Log.e("URI",""+croppedImageUri);
                binding.civImage.setImageURI(croppedImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e("ERRORIMAGE",""+error);

            }
        }
    }


    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }

}
*/