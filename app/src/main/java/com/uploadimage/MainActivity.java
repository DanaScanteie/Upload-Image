package com.uploadimage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String FILE_NAME = "uploadFile.txt";

    private ImageView imageView;
    private Button chooseButton;
    private Button rotateButton;
    private Button uploadButton;

    private String UPLOAD_URL = "http://vega.unitbv.ro/~carpm/tst/index.php";
    private static final int PICK_IMAGE = 100;
    private int ANGLE = 0;
    private Uri imageUri;
    private Bitmap bitmap;
    ArrayAdapter<String> adapter;
    List<String> list;
    HashMap<String,String> urls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageView = (ImageView) findViewById(R.id.imageView);
        chooseButton = (Button) findViewById(R.id.chooseButton);
        rotateButton = (Button) findViewById(R.id.rotateButton);
        uploadButton = (Button) findViewById(R.id.uploadButton);

        Spinner spinner = findViewById(R.id.spinner1);

        list = new ArrayList<>();
        urls = new HashMap<>();
        loadFile();

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
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
            loadFile();
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
                    uploadImage();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Image not selected!", Toast.LENGTH_LONG).show();
                }
            }
        });
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

    public void loadFile() {
        FileInputStream fis = null;

        try {
            fis = openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            list.clear();
            urls.clear();
            while ((text = br.readLine()) != null) {
                String[] str = text.split(" ");
                list.add(str[0]);
                urls.put(str[0], str[str.length-1]);
                sb.append(text).append("\n");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }





    void handleSendImage(Intent intent) {
        imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null ) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                resizeImage(bitmap);
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

    private void resizeImage(Bitmap bitmapVar) {
        int deviceWidth = getWindowManager().getDefaultDisplay().getWidth();
        int deviceHeight = getWindowManager().getDefaultDisplay().getHeight();
        int bitmapHeight = bitmapVar.getHeight();
        int bitmapWidth = bitmapVar.getWidth();

        if (bitmapWidth > deviceWidth) {
            int scaledWidth = deviceWidth;
            int scaledHeight = (scaledWidth * bitmapHeight) / bitmapWidth;
            if (scaledHeight > deviceHeight)
                scaledHeight = deviceHeight;
            Bitmap bitmapScaled = Bitmap.createScaledBitmap(bitmapVar, scaledWidth, scaledHeight, true);
            bitmap = bitmapScaled;
        }
        else if (bitmapHeight > deviceHeight) {
            int scaledHeight = deviceHeight;
            int scaledWidth = (scaledHeight * bitmapWidth) / bitmapHeight;
            if (scaledWidth > deviceWidth)
                scaledWidth = deviceWidth;
            Bitmap bitmapScaled = Bitmap.createScaledBitmap(bitmapVar, scaledWidth, scaledHeight, true);
            bitmap = bitmapScaled;
        }

        imageView.setImageBitmap(bitmap);
    }


    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
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
                Toast.makeText(getApplicationContext(),"Image Uploaded",Toast.LENGTH_LONG).show();
            }

            @Override
            protected String doInBackground(Bitmap... params) {
                Bitmap bitmap = params[0];
                String uploadImage = getStringImage(bitmap);

                HashMap<String,String> data = new HashMap<>();
                data.put("thumb_file", uploadImage);
                data.put("up_file", getFileName(imageUri));

                String result = requestHandler.postRequest(UPLOAD_URL, data);
                return result;
            }
        }

        UploadImage ui = new UploadImage();
        ui.execute(bitmap);
    }

    String getFileName(Uri uri){
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
}
