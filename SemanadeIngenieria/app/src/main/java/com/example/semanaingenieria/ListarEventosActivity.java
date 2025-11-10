package com.example.semanaingenieria;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class ListarEventosActivity extends AppCompatActivity {

    private TextView tvListado;
    private Button btnVolver;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_eventos);

        db = new DatabaseHelper(this);

        tvListado = findViewById(R.id.tvListado);
        btnVolver = findViewById(R.id.btnVolver);

        // Cargar y mostrar eventos
        cargarEventos();

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void cargarEventos() {
        List<Evento> eventos = db.listarEventos();

        if (eventos.isEmpty()) {
            tvListado.setText("No hay eventos registrados");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("LISTADO DE EVENTOS\n");
            sb.append("==================\n\n");

            for (Evento evento : eventos) {
                sb.append(evento.toString());
                sb.append("------------------\n");
            }

            tvListado.setText(sb.toString());
        }
    }
}