package com.example.semanaingenieria;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnIngresar, btnListar, btnConsultar, btnActualizar, btnSalir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar botones
        btnIngresar = findViewById(R.id.btnIngresar);
        btnListar = findViewById(R.id.btnListar);
        btnConsultar = findViewById(R.id.btnConsultar);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnSalir = findViewById(R.id.btnSalir);

        // Configurar listeners
        btnIngresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegistrarEventoActivity.class);
                startActivity(intent);
            }
        });

        btnListar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListarEventosActivity.class);
                startActivity(intent);
            }
        });

        btnConsultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ConsultarEventoActivity.class);
                startActivity(intent);
            }
        });

        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ActualizarEventoActivity.class);
                startActivity(intent);
            }
        });

        btnSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                System.exit(0);
            }
        });
    }
}