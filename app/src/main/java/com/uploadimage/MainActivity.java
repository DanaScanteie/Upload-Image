package com.uploadimage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final int PICK_IMAGE = 100;

    private ImageView imageView;
    private Button chooseButton;
    private Button rotateButton;
    private Button uploadButton;

    private String UPLOAD_URL = "http://vega.unitbv.ro/~carpm/tst/index.php";
    private int ANGLE = 0;
    private Uri imageUri;
    private Bitmap bitmap;
    ArrayAdapter<String> adapter;
    List<String> list;
    HashMap<String,String> urls;
    private Boolean resize = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageView = findViewById(R.id.imageView);
        chooseButton = findViewById(R.id.chooseButton);
        rotateButton = findViewById(R.id.rotateButton);
        uploadButton = findViewById(R.id.uploadButton);

        Spinner spinner = findViewById(R.id.spinner1);

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
            resize = getIntent().getExtras().getBoolean("EXTRA_RESIZE");
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
                    uploadImage();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Image not selected!", Toast.LENGTH_LONG).show();
                }
            }
        });

        initializeDataBase();
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

            case R.id.action_results:
            {
                Intent newIntent = new Intent(MainActivity.this, EditActivity.class);
                startActivity(newIntent);
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
        SQLiteDatabase _dataDB;
        _dataDB = openOrCreateDatabase("UploadImageDatabase", MODE_PRIVATE, null);

        Cursor cursor = _dataDB.rawQuery("SELECT * FROM Url", null);

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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();
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

    private void uploadImage(){
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
                String result = requestHandler.postRequest(UPLOAD_URL, bitmap, filename);
                return result;
            }
        }

        UploadImage ui = new UploadImage();
        Log.i("myapp", "Without resize:" + String.valueOf(bitmap.getAllocationByteCount()));
        if(resize==true)
        {    bitmap = resizeImage(bitmap, 1024, true);
            Log.i("myapp", "With resize" + String.valueOf(bitmap.getAllocationByteCount()));
        }
        ui.execute(bitmap);
    }

    public void initializeDataBase() {
        SQLiteDatabase dataDB = openOrCreateDatabase("UploadImageDatabase", MODE_PRIVATE, null);

        dataDB.execSQL("CREATE TABLE IF NOT EXISTS Url(Name Text(20) PRIMARY KEY ," + " UrlLink Text(50));");
        dataDB.close();
    }
}
