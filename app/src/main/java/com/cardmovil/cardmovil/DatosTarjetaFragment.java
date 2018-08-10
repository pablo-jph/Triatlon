package com.cardmovil.cardmovil;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.sa.tonisa.tonisa.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DatosTarjetaFragment extends Fragment {

    public EditText etDatosNumbreTarjeta, etDatosFechaExpiracionTarjeta, etDatosNombre, etDatosApellidos;
    JSONObject finalObjectMov;
    JSONArray parentArrayMov;

    //TABLA
    TableLayout tlMovimientos;
    TableRow tr;
    TextView tvFecha, tvMonto, tvDesc;
    String[] idsTarjetas;

    public DatosTarjetaFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_datos_tarjeta, container, false);

        datos_tarjeta ma = new datos_tarjeta();

        AppCompatActivity aca = (AppCompatActivity) datos_tarjeta.getContext();
        datos_tarjeta cont = (datos_tarjeta) aca;

        etDatosNumbreTarjeta = (EditText)view.findViewById(R.id.etDatosNumbreTarjeta);
        etDatosFechaExpiracionTarjeta = (EditText)view.findViewById(R.id.etDatosFechaExpiracionTarjeta);
        etDatosNombre = (EditText)view.findViewById(R.id.etDatosNombre);
        etDatosApellidos = (EditText)view.findViewById(R.id.etDatosApellidos);

        tlMovimientos = (TableLayout) view.findViewById(R.id.tlPresupuesto);

        tlMovimientos.setColumnStretchable(0, true);
        tlMovimientos.setColumnStretchable(1, true);
        tlMovimientos.setColumnStretchable(2, true);

        etDatosNumbreTarjeta.setEnabled(false);
        etDatosFechaExpiracionTarjeta.setEnabled(false);
        etDatosNombre.setEnabled(false);
        etDatosApellidos.setEnabled(false);

        etDatosNumbreTarjeta.setText(ma.getDatosNumbreTarjeta());
        etDatosFechaExpiracionTarjeta.setText(ma.getDatosFechaExpiracionTarjeta());
        etDatosNombre.setText(ma.getDatosNombre());
        etDatosApellidos.setText(ma.getDatosApellidos());

        parentArrayMov = ma.getParentArrayMov();
        finalObjectMov = null;
        if(parentArrayMov != null) {
            for (int i = 0; i < parentArrayMov.length(); i++) {
                try {
                    finalObjectMov = parentArrayMov.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                tr = new TableRow(cont);
                tvFecha = new TextView(cont);
                tvMonto = new TextView(cont);
                tvDesc = new TextView(cont);

                try {
                    tvFecha.setText(finalObjectMov.getString("Tmov_Fecha"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                tvFecha.setTextSize(15);
                tvFecha.setGravity(Gravity.CENTER);
                tvFecha.setTextColor(Color.BLACK);
                try {
                    tvMonto.setText(finalObjectMov.getString("Tmov_Monto"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                tvMonto.setTextSize(15);
                tvMonto.setGravity(Gravity.CENTER);
                tvMonto.setTextColor(Color.BLACK);
                try {
                    tvDesc.setText(finalObjectMov.getString("Tmov_Desc"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                tvDesc.setTextSize(15);
                tvDesc.setGravity(Gravity.LEFT);
                tvDesc.setTextColor(Color.BLACK);
                tvDesc.setPadding(5, 0, 0, 0);

                tr.addView(tvDesc);
                tr.addView(tvFecha);
                tr.addView(tvMonto);

                tlMovimientos.addView(tr);
            }
        }

        return view;
    }

    public EditText getEtDatosNumbreTarjeta() {
        return etDatosNumbreTarjeta;
    }

    public void setEtDatosNumbreTarjeta(EditText etDatosNumbreTarjeta) {
        this.etDatosNumbreTarjeta = etDatosNumbreTarjeta;
    }
}
