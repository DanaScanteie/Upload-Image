package com.uploadimage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final int PICK_IMAGE = 100;

    private ImageView imageView;
    private Button chooseButton;
    private Button rotateButton;
    private Button uploadButton;

    private String UPLOAD_URL;
    private String keyForUpload;
    private int ANGLE = 0;
    private Uri imageUri;
    private Bitmap bitmap;
    private ArrayAdapter<String> adapter;
    private List<String> list;
    private HashMap<String,String> urls;
    private Boolean resize = false;


    private static final int REQUEST_WRITE_PERMISSION = 786;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("preferences", MODE_PRIVATE);
        boolean firstStart = prefs.getBoolean("firstStart", true);
        keyForUpload = prefs.getString("key", null);
        resize = prefs.getBoolean("resize", false);

        if (firstStart) {
            showStartDialog();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageView = findViewById(R.id.imageView);
        chooseButton = findViewById(R.id.chooseButton);
        rotateButton = findViewById(R.id.rotateButton);
        uploadButton = findViewById(R.id.uploadButton);

        Spinner spinner = findViewById(R.id.spinner1);

        initializeDataBase();

        list = new ArrayList<>();
        urls = new HashMap<>();
        getData();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);


        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendImage(intent);
            }
        }

        if (getIntent().getExtras() != null) {
            getData();
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(this);
        }

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickOnGallery();
            }
        });

        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ANGLE = ANGLE + 90;
                imageView.setRotation(ANGLE);
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageUri!=null) {
                    if(keyForUpload.equals("key")) {
                        uploadImage();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "You don't have access to upload on this server!", Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Image not selected!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            CropImage.startPickImageActivity(MainActivity.this);
        }
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUrii = CropImage.getPickImageResultUri(this, data);
            Log.i("RESPONSE getPath", imageUrii.getPath());
            Log.i("RESPONSE getScheme", imageUrii.getScheme());
            Log.i("RESPONSE PathSegments", imageUrii.getPathSegments().toString());

            //NOW CROP IMAGE URI
            CropImage.activity(imageUrii)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setMultiTouchEnabled(true)
                    .setRequestedSize(800, 800)
                    .setAspectRatio(1,1)
                    .start(this);
            imageUri = imageUrii;
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Log.i("RESPONSE getUri", result.getUri().toString());
                imageView.setImageURI(result.getUri());
                imageUri = result.getUri();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            if (imageUri != null ) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id)
        {
            case R.id.action_add_url:
            {
                Intent newIntent = new Intent(MainActivity.this, AddActivity.class);
                startActivity(newIntent);

                return true;
            }
            case R.id.action_remove_url:
            {
                Intent newIntent = new Intent(MainActivity.this, RemoveActivity.class);
                startActivity(newIntent);

                return true;
            }
            case R.id.action_settings:
            {
                Intent newIntent = new Intent(MainActivity.this, SettingsActivity.class);
                newIntent.putExtra("CHECKBOX_STATE", resize);
                startActivity(newIntent);

                return true;
            }

            case R.id.action_edit_url:
            {
                Intent newIntent = new Intent(MainActivity.this, EditActivity.class);
                startActivity(newIntent);
                return true;
            }

            case R.id.action_edit_key:
            {
                Intent newIntent = new Intent(MainActivity.this, EditKeyActivity.class);
                newIntent.putExtra("EXTRA_KEY", keyForUpload);
                startActivity(newIntent);
                return true;
            }

            case R.id.action_crop_image:
            {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    //START REQUEST PERMISSION
//                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
//                } else {
//                    //ELSE BELOW START OPEN PICKER
//                    //CropImage.startPickImageActivity(MainActivity.this);
//                    CropImage.activity(imageUri).start(this);
//                }
                CropImage.activity(imageUri).start(this);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        UPLOAD_URL = urls.get(text);
        Toast.makeText(this, UPLOAD_URL, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void getData() {
        list.clear();
        urls.clear();
        SQLiteDatabase dataDB = openOrCreateDatabase("UploadImageDatabase", MODE_PRIVATE, null);

        Cursor cursor = dataDB.rawQuery("SELECT * FROM Url", null);

        if (cursor.moveToFirst()) {
            do {
                String strName = cursor.getString(cursor.getColumnIndex("Name"));
                String strPassword = cursor.getString(cursor.getColumnIndex("UrlLink"));

                list.add(strName);
                urls.put(strName, strPassword);
            } while (cursor.moveToNext());

        }
        cursor.close();
    }




    void handleSendImage(Intent intent) {
        imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null ) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                //resizeImage(bitmap);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void clickOnGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    public static Bitmap resizeImage(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min((float) maxImageSize / realImage.getWidth(), (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap bitmapScaled = Bitmap.createScaledBitmap(realImage, width, height, filter);
        return bitmapScaled;
    }

    private String getFileName(Uri uri){
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private void uploadImage()  {
        class UploadImage extends AsyncTask<Bitmap,Void,String> {
            ProgressDialog dialog;
            RequestHandler requestHandler = new RequestHandler();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = ProgressDialog.show(MainActivity.this, "Uploading Image", "Please wait...",true,true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                dialog.dismiss();
                Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
            }

            @Override
            protected String doInBackground(Bitmap... params) {
                Bitmap bitmap = params[0];
                String filename = getFileName(imageUri);
                String result = null;
                try {
                    result = requestHandler.postRequest(UPLOAD_URL, bitmap, getDateFromUri() + filename);
                    Log.i("myapp", getDateFromUri() + filename);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                return result;
            }
        }

        UploadImage ui = new UploadImage();
        if(resize==true) {
            bitmap = resizeImage(bitmap, 1024, true);
        }
        ui.execute(bitmap);
    }

    public String getDateFromUri() throws URISyntaxException {
        String filePath=PathUtil.getPath(getApplicationContext(),imageUri);
        File file = new File(filePath);
        String dateString = null;
        if(file.exists()) //Extra check, Just to validate the given path
        {
            ExifInterface intf = null;
            try
            {
                intf = new ExifInterface(imageUri.getPath());
                if(intf != null)
                {
                    dateString = intf.getAttribute(ExifInterface.TAG_DATETIME);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            if(intf == null)
            {
                Date lastModDate = new Date(file.lastModified());
                dateString = lastModDate.toString();
            }
        }
        return dateString;
    }

    public void initializeDataBase() {
        SQLiteDatabase dataDB = openOrCreateDatabase("UploadImageDatabase", MODE_PRIVATE, null);

        dataDB.execSQL("CREATE TABLE IF NOT EXISTS Url(Name Text(20) PRIMARY KEY ," + " UrlLink Text(50));");
        dataDB.close();
    }

    private void showStartDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this);
        View view = layoutInflaterAndroid.inflate(R.layout.alert_dialog, null);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setView(view);

        final EditText editTextDialog = (EditText) view.findViewById(R.id.editDialog);
        alertDialog
                .setTitle("Key for upload")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        SharedPreferences prefs = getSharedPreferences("preferences", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("key", editTextDialog.getText().toString());
                        editor.apply();
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        AlertDialog alertDialogAndroid = alertDialog.create();
        alertDialogAndroid.show();

        SharedPreferences prefs = getSharedPreferences("preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstStart", false);
        editor.apply();
    }
}
