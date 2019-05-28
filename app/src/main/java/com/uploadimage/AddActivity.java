package com.uploadimage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class AddActivity extends AppCompatActivity {
    private static final String FILE_NAME = "uploadFile.txt";
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
                FileOutputStream fos = null;

                try {
                    fos = openFileOutput(FILE_NAME, MODE_APPEND);
                    fos.write(inputName.getBytes());
                    fos.write(" ".getBytes());
                    fos.write(inputURL.getBytes());
                    fos.write("\n".getBytes());

                    editTextName.getText().clear();
                    editTextURL.getText().clear();
                    Toast.makeText(getApplicationContext(), "Saved file " + FILE_NAME, Toast.LENGTH_LONG).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                startActivity(intent);
            }
        });
    }
}
