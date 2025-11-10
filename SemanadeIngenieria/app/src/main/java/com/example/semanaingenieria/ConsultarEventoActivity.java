package com.example.semanaingenieria;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ConsultarEventoActivity extends AppCompatActivity {

    private EditText etCodBuscar;
    private TextView tvResultado;
    private Button btnBuscar, btnVolver;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_evento);

        db = new DatabaseHelper(this);

        etCodBuscar = findViewById(R.id.etCodBuscar);
        tvResultado = findViewById(R.id.tvResultado);
        btnBuscar = findViewById(R.id.btnBuscar);
        btnVolver = findViewById(R.id.btnVolver);

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarEvento();
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
                StringBuilder sb = new StringBuilder();
                sb.append("INFORMACIÓN DEL EVENTO\n");
                sb.append("======================\n\n");
                sb.append("Código: ").append(evento.getCod()).append("\n\n");
                sb.append("Nombre: ").append(evento.getNombre()).append("\n\n");
                sb.append("Descripción: ").append(evento.getDescrip()).append("\n");

                tvResultado.setText(sb.toString());
            } else {
                tvResultado.setText("No se encontró evento con código " + cod);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Código inválido", Toast.LENGTH_SHORT).show();
        }
    }
}