package com.example.user_pc.ag_scanner;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
public class MainActivity extends AppCompatActivity {
    private Button proceedbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        proceedbtn=(Button)findViewById(R.id.button);
        proceedbtn.setOnClickListener(new buttonClick());
    }

    private class buttonClick implements View.OnClickListener{
        public void onClick(View v){
            Intent intent=new Intent(MainActivity.this,mainMenuActivity.class);
            startActivity(intent);
        }
    }
}
