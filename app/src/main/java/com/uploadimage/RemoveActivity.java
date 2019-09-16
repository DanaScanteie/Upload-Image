package com.uploadimage;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RemoveActivity extends AppCompatActivity {

    private EditText editTextName;
    private Button removeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove);

        editTextName = findViewById(R.id.editTextName);
        removeButton = findViewById(R.id.removeButton);

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RemoveActivity.this, MainActivity.class);
                intent.putExtra("EXTRA", editTextName.getText().toString());

                try{
                    SQLiteDatabase dataDB = openOrCreateDatabase("UploadImageDatabase", MODE_PRIVATE, null);
                    String sql = "DELETE FROM Url WHERE Name ='" +  editTextName.getText().toString() + "';";
                    dataDB.execSQL(sql);
                    dataDB.close();

                } catch (Exception e) {

                }
                startActivity(intent);
            }
        });
    }
}
