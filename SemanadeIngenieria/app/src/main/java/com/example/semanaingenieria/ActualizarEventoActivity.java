package com.example.semanaingenieria;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ActualizarEventoActivity extends AppCompatActivity {

    private EditText etCodBuscar, etNombre, etDescrip;
    private Button btnBuscar, btnActualizar, btnVolver;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar_evento);

        db = new DatabaseHelper(this);

        etCodBuscar = findViewById(R.id.etCodBuscar);
        etNombre = findViewById(R.id.etNombre);
        etDescrip = findViewById(R.id.etDescrip);
        btnBuscar = findViewById(R.id.btnBuscar);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnVolver = findViewById(R.id.btnVolver);

        // Deshabilitar campos hasta buscar
        etNombre.setEnabled(false);
        etDescrip.setEnabled(false);
        btnActualizar.setEnabled(false);

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarEvento();
            }
        });

        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizarEvento();
            }
        });

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void buscarEvento() {
        String codStr = etCodBuscar.getText().toString().trim();

        if (codStr.isEmpty()) {
            Toast.makeText(this, "Ingrese un código", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int cod = Integer.parseInt(codStr);
            Evento evento = db.consultarEvento(cod);

            if (evento != null) {
                etNombre.setText(evento.getNombre());
                etDescrip.setText(evento.getDescrip());

                // Habilitar campos para editar
                etNombre.setEnabled(true);
                etDescrip.setEnabled(true);
                btnActualizar.setEnabled(true);

                Toast.makeText(this, "Evento encontrado. Puede modificarlo", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No se encontró evento con código " + cod, Toast.LENGTH_SHORT).show();
                limpiarCampos();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Código inválido", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarEvento() {
        String codStr = etCodBuscar.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();
        String descrip = etDescrip.getText().toString().trim();

        if (nombre.isEmpty() || descrip.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int cod = Integer.parseInt(codStr);
            boolean resultado = db.actualizarEvento(cod, nombre, descrip);

            if (resultado) {
                Toast.makeText(this, "Evento actualizado exitosamente", Toast.LENGTH_LONG).show();
                limpiarCampos();
            } else {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Error en código", Toast.LENGTH_SHORT).show();
        }
    }

    private void limpiarCampos() {
        etCodBuscar.setText("");
        etNombre.setText("");
        etDescrip.setText("");
        etNombre.setEnabled(false);
        etDescrip.setEnabled(false);
        btnActualizar.setEnabled(false);
    }
}