package com.example.sayar.imageuploadingapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.commons.MenuSheetView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kishan.askpermission.AskPermission;
import com.kishan.askpermission.ErrorCallback;
import com.kishan.askpermission.PermissionCallback;
import com.kishan.askpermission.PermissionInterface;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements PermissionCallback, ErrorCallback {
    private Button selectFile, btnUpload,showingList;
    private ImageView imageView;
    EditText editText;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private Uri filePath;
    FirebaseStorage storage;
    StorageReference storageReference;
    private final int PICK_IMAGE_REQUEST = 71;
    List<ImageUrl> uploadList;
    private Uri picUri;
    int PIC_CROP=0;
    int galleryImage=0;
    File file;
    final int RC_TAKE_PHOTO = 1;
    String name;
    private static final int PICKFILE_REQUEST_CODE = 1234;
    BottomSheetLayout bottomSheet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        btnUpload = (Button) findViewById(R.id.btnUpload);
        imageView = (ImageView) findViewById(R.id.imgView);
        editText=findViewById(R.id.imagename);
        showingList=findViewById(R.id.showList);
        bottomSheet= (BottomSheetLayout) findViewById(R.id.bottomsheet);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        selectFile=findViewById(R.id.chooseFile);
        storageReference.child("images");


        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
        showingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,ShowingList.class);
                startActivity(intent);
            }
        });
        uploadList = new ArrayList<>();

        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = MainActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                MenuSheetView menuSheetView =
                        new MenuSheetView(MainActivity.this, MenuSheetView.MenuType.LIST, "Choose...", new MenuSheetView.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                if (bottomSheet.isSheetShowing()) {
                                    bottomSheet.dismissSheet();
                                }
                                if (item.getItemId()==R.id.imageGalley){
                                    chooseImage();
                                    return true;
                                }
                                else if (item.getItemId()==R.id.camera){
                                    galleryImage=2;
                                    reqPermissionCamera();
                                    return true;
                                }return true;
                            }
                        });
                menuSheetView.inflateMenu(R.menu.navigation);
                bottomSheet.showWithSheetView(menuSheetView);


            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("requestCode",""+requestCode);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            Log.d("filePath",filePath.toString());
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                Glide.with(this).load(filePath).into(imageView);
                //imageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        if(requestCode == RC_TAKE_PHOTO ){
            Uri uri=picUri;
            Uri file_uri = Uri.parse(String.valueOf(uri));
            filePath=file_uri;
            String real_path = file_uri.getPath();
            Glide.with(this).load(real_path).into(imageView);
            Log.d("uri",real_path);
            Log.d("cameInthisBlock","true");
            performCrop();
        }
        else if(requestCode == PIC_CROP){
            Uri uri=picUri;
            Uri file_uri = Uri.parse(String.valueOf(uri));
            filePath=file_uri;
            String real_path = file_uri.getPath();
            Glide.with(this).load(real_path).into(imageView);
        }
    }
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    private void performCrop(){
        try {

            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            PIC_CROP = 2;
            File file=getOutputMediaFile(1);
            picUri = Uri.fromFile(file); // create
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT,picUri);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        catch(ActivityNotFoundException anfe){
            //display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void uploadImage() {

        try {

            if (filePath.equals("")) {
                Toast.makeText(this, "please select a file to upload", Toast.LENGTH_SHORT).show();
                return;
            } else {
                name = editText.getText().toString();
                if (name.equals("")) {
                    Toast.makeText(MainActivity.this, "please provide name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (InternetConnection.checkConnection(MainActivity.this)) {
                    // Its Available...
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setTitle("Uploading...");
                    progressDialog.show();

                    StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
                    ref.putFile(filePath)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    progressDialog.dismiss();

                                    Log.d("name", "name");
                                    Uri downloadUri = taskSnapshot.getMetadata().getDownloadUrl();
                                    String url = downloadUri.toString();
                                    writeNewImageInfoToDB(url, name);
                                    Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                                    Intent intent=new Intent(MainActivity.this,MainActivity.class);
                                    startActivity(intent);


                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(MainActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                            .getTotalByteCount());
                                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                                }
                            });
                } else {
                    // Not Available...

                    Toast.makeText(this, "please check your internet connection!", Toast.LENGTH_SHORT).show();

                }



            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }
    private void writeNewImageInfoToDB(String name, String url) {
        ImageUrl info = new ImageUrl(name, url);

        String key = databaseReference.push().getKey();
        databaseReference.child(key).setValue(info);
    }

    @Override
    public void onShowRationalDialog(final PermissionInterface permissionInterface, int requestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("We need permissions for this app.");
        builder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                permissionInterface.onDialogShown();
            }
        });
        builder.setNegativeButton(R.string.btn_cancel, null);
        builder.show();
    }

    @Override
    public void onShowSettings(final PermissionInterface permissionInterface, int requestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("We need permissions for this app. Open setting screen?");
        builder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                permissionInterface.onSettingsShown();
            }
        });
        builder.setNegativeButton(R.string.btn_cancel, null);
        builder.show();
    }

    @Override
    public void onPermissionsGranted(int requestCode) {
        if (galleryImage==2){

            Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

            File file=getOutputMediaFile(1);
            picUri = Uri.fromFile(file); // create
            i.putExtra(MediaStore.EXTRA_OUTPUT,picUri); // set the image file

            startActivityForResult(i, RC_TAKE_PHOTO);
        }

    }

    @Override
    public void onPermissionsDenied(int requestCode) {
        Toast.makeText(MainActivity.this,getString(R.string.permission_camera_denied),Toast.LENGTH_SHORT).show();
        return;
    }
    private void reqPermissionCamera() {
        new AskPermission.Builder(this).setPermissions(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .setCallback(this)
                .setErrorCallback(this)
                .request(PICKFILE_REQUEST_CODE);
    }
    private  File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyApplication");

        /**Create the storage directory if it does not exist*/
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }

        /**Create a media file name*/
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == 1){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".png");
        } else {
            return null;
        }

        return mediaFile;
    }
    public static class InternetConnection {

        /** CHECK WHETHER INTERNET CONNECTION IS AVAILABLE OR NOT */
        static boolean checkConnection(Context context) {
            final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();

            if (activeNetworkInfo != null) { // connected to the internet
                //Toast.makeText(context, activeNetworkInfo.getTypeName(), Toast.LENGTH_SHORT).show();

                if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    // connected to wifi
                    return true;
                } else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    // connected to the mobile provider's data plan
                    return true;
                }
            }
            return false;
        }
    }

        }


