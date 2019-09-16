package com.uploadimage;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class EditActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextUrl;
    private Button editButton;

    private ArrayList<URLDetails> urls;
    private ListAdapter adapter;
    private ListView listView;
    private URLDetails selectedUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        editTextName = findViewById(R.id.etFirstName);
        editTextUrl = findViewById(R.id.etLastName);
        listView = findViewById(R.id.listView);

        urls = new ArrayList<>();
        getData();

        adapter = new ListAdapter(this, R.layout.adapter_view_layout, urls);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                selectedUrl = (URLDetails) listView.getItemAtPosition(position);
                editTextName.setText(selectedUrl.getName());
                editTextUrl.setText(selectedUrl.getUrl());
            }
        });

        editButton = findViewById(R.id.editButton);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Intent newIntent = new Intent(EditActivity.this, MainActivity.class);
                    SQLiteDatabase dataDB = openOrCreateDatabase("UploadImageDatabase", MODE_PRIVATE, null);

                    String sql = "UPDATE Url " + "SET Name ='" + editTextName.getText().toString() + "', UrlLink = '" +
                            editTextUrl.getText().toString() + "' WHERE Name ='" + selectedUrl.getName() + "' AND UrlLink ='" +
                            selectedUrl.getUrl() + "';";
                    dataDB.execSQL(sql);
                    dataDB.close();
                    startActivity(newIntent);

                } catch (Exception e) {

                }
            }
        });
    }

    public void getData() {

        SQLiteDatabase dataDB = openOrCreateDatabase("UploadImageDatabase", MODE_PRIVATE, null);
        Cursor cursor = dataDB.rawQuery("SELECT * FROM Url", null);

        if (cursor.moveToFirst()) {
            do {
                String strName = cursor.getString(cursor.getColumnIndex("Name"));
                String strURL = cursor.getString(cursor.getColumnIndex("UrlLink"));

                URLDetails url = new URLDetails(strName, strURL);
                urls.add(url);
            } while (cursor.moveToNext());

        }
        cursor.close();
    }

}
