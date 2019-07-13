package com.uploadimage;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class AddActivity extends AppCompatActivity {
    String inputName;
    String inputURL;

    private EditText editTextName;
    private EditText editTextURL;
    private Button addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        editTextName = findViewById(R.id.editTextName);
        editTextURL = findViewById(R.id.editTextURL);
        addButton = findViewById(R.id.addButton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddActivity.this, MainActivity.class);
                intent.putExtra("EXTRA", editTextURL.getText().toString());

                inputName = editTextName.getText().toString();
                inputURL = editTextURL.getText().toString();

                addToDatabase();
                startActivity(intent);
            }
        });


    }

    public void addToDatabase() {
        try {
            SQLiteDatabase _dataDB;
            _dataDB = openOrCreateDatabase("UploadImageDatabase", MODE_PRIVATE, null);
            SQLiteStatement s = _dataDB.compileStatement("SELECT count(*) FROM Url " +
                    "WHERE Name='" + inputName + "' AND UrlLink = '" + inputURL + "';");

            long count = s.simpleQueryForLong();
            _dataDB.close();

            if (count >= 1) {
                Toast.makeText(getApplicationContext(), "This url already exist." , Toast.LENGTH_LONG).show();
            }
            else {
                try{
                    _dataDB = openOrCreateDatabase("UploadImageDatabase", MODE_PRIVATE, null);
                    String sql = "INSERT INTO Url " +
                            "VALUES('" + inputName + "', '" + inputURL  + "');";
                    SQLiteStatement st = _dataDB.compileStatement(sql);
                    st.executeInsert();
                    _dataDB.close();

                    Toast.makeText(getApplicationContext(), "New url added with success!", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
