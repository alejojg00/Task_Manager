package com.example.semanaingenieria;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegistrarEventoActivity extends AppCompatActivity {

    private EditText etCod, etNombre, etDescrip;
    private Button btnGuardar, btnCancelar, btnVolver;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_evento);

        // Inicializar base de datos
        db = new DatabaseHelper(this);

        // Inicializar vistas
        etCod = findViewById(R.id.etCod);
        etNombre = findViewById(R.id.etNombre);
        etDescrip = findViewById(R.id.etDescrip);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnVolver = findViewById(R.id.btnVolver);

        // Botón Guardar
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarEvento();
            }
        });

        // Botón Cancelar (limpiar textos)
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                limpiarCampos();
            }
        });

        // Botón Volver
        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void guardarEvento() {
        String codStr = etCod.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();
        String descrip = etDescrip.getText().toString().trim();

        if (codStr.isEmpty() || nombre.isEmpty() || descrip.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int cod = Integer.parseInt(codStr);
            boolean resultado = db.insertarEvento(cod, nombre, descrip);

            if (resultado) {
                Toast.makeText(this, "Evento registrado exitosamente", Toast.LENGTH_SHORT).show();
                limpiarCampos();
            } else {
                Toast.makeText(this, "Error al registrar evento", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El código debe ser un número válido", Toast.LENGTH_SHORT).show();
        }
    }

    private void limpiarCampos() {
        etCod.setText("");
        etNombre.setText("");
        etDescrip.setText("");
        etCod.requestFocus();
    }
}