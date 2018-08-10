package com.cardmovil.cardmovil;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.cardmovil.cardmovil.models.list_tarjetas;
import com.google.gson.Gson;
import com.sa.tonisa.tonisa.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

public class datos_tarjeta extends AppCompatActivity {

//    public EditText etDatosNumbreTarjeta, etDatosFechaExpiracionTarjeta, etDatosNombre, etDatosApellidos;
    public static String datosNumbreTarjeta = "", datosFechaExpiracionTarjeta = "", datosNombre = "", datosApellidos = "";
    Button btnTarjetaAtras, btnNuevaTarjeta, btnAdminTarjetaDuplicada, btnTarjetaAdelante;
    private final static String TAG = "DashBoardActivity";
    Bundle bolsa;
    private int n = 1;
    String id_tarjeta;
    JSONObject finalObject, finalObjectMov;
    public static JSONArray parentArrayMov;
    private static Context mContext;

    //TABLA
//    TableLayout tlMovimientos;
//    TableRow tr;
//    TextView tvFecha, tvMonto, tvDesc;
    String[] idsTarjetas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos_tarjeta);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

//        etDatosNumbreTarjeta = (EditText) findViewById(R.id.etDatosNumbreTarjeta);
//        etDatosFechaExpiracionTarjeta = (EditText) findViewById(R.id.etDatosFechaExpiracionTarjeta);
//        etDatosNombre = (EditText) findViewById(R.id.etDatosNombre);
//        etDatosApellidos = (EditText) findViewById(R.id.etDatosApellidos);

//        tlMovimientos = (TableLayout) findViewById(R.id.tlPresupuesto);
//
//        tlMovimientos.setColumnStretchable(0, true);
//        tlMovimientos.setColumnStretchable(1, true);
//        tlMovimientos.setColumnStretchable(2, true);

        this.mContext = this;

        btnTarjetaAtras = (Button) findViewById(R.id.btnTarjetaAtras);
        btnNuevaTarjeta = (Button) findViewById(R.id.btnNuevaTarjeta);
        btnAdminTarjetaDuplicada = (Button) findViewById(R.id.btnAdminTarjetaDuplicada);
        btnTarjetaAdelante = (Button) findViewById(R.id.btnTarjetaAdelante);

        View.OnClickListener listener = new View.OnClickListener() {
            public void onClick(View view) {
                if (view == findViewById(R.id.btnTarjetaAdelante)) {
                    n++;
                    rellenarFragmento(idsTarjetas, n);
                } else {
                    n--;
                    rellenarFragmento(idsTarjetas, n);
                }
            }
        };

        btnTarjetaAtras.setOnClickListener(listener);
        btnTarjetaAdelante.setOnClickListener(listener);

        bolsa = getIntent().getExtras();
        if (bolsa != null) {
            String json = bolsa.getString("l_tarjetasModel");
            list_tarjetas list_tarjetasModel = new Gson().fromJson(json, list_tarjetas.class);
            idsTarjetas = list_tarjetasModel.getIDs_cuenta().split("-");
            id_tarjeta = list_tarjetasModel.getTar_Codigo();

            n = Arrays.asList(idsTarjetas).indexOf(id_tarjeta);

            final String protocolo = "http://";
            String ip = getResources().getString(R.string.ipweb);
            String puerto = getResources().getString(R.string.puertoweb);
            puerto = puerto.equals("") ? "" : ":" + puerto;
            String url = protocolo + ip + puerto + "/";

            new JSONTask().execute(url + "/tarjetas/get_tarjeta_by_id", id_tarjeta);
        }

        btnNuevaTarjeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), add_tarjeta.class);
                Bundle bolsa = new Bundle();
//                bolsa.putString("usuario", usuario_id);
                i.putExtras(bolsa);
                startActivity(i);
            }
        });

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public static Context getContext() {
        return mContext;
    }

    private void rellenarFragmento(String[] lista_ids, int indice) {
        if (indice < 0)
            n = lista_ids.length - 1;
        else if (indice >= lista_ids.length)
            n = 0;
//        else if(indice == 0)
//            n = 0;
//        else
//            n = n - 1;

        final String protocolo = "http://";
        String ip = getResources().getString(R.string.ipweb);
        String puerto = getResources().getString(R.string.puertoweb);
        puerto = puerto.equals("") ? "" : ":" + puerto;
        String url = protocolo + ip + puerto + "/";

        new JSONTask().execute(url + "/tarjetas/get_tarjeta_by_id", lista_ids[n]);
    }

    public class JSONTask extends AsyncTask<String, String, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            int respuesta = 0;
            String respuesta_horario = "";

            try {
                URL url = new URL(params[0]);
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("tarjetaid", params[1]);

                connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                respuesta = connection.getResponseCode();

                if (respuesta == HttpURLConnection.HTTP_OK) {

                    InputStream stream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuffer buffer = new StringBuffer();
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    String finalJson = buffer.toString();

                    JSONObject parentObject = new JSONObject(finalJson);
                    //DATOS TARJETA
                    JSONArray parentArray = parentObject.getJSONArray("tarjetas");
                    //MOVIMIENTOS
                    parentArrayMov = parentObject.getJSONArray("movimientos");
                    setParentArrayMov(parentObject.getJSONArray("movimientos"));

                    StringBuffer finalBufferData = new StringBuffer();
                    Gson gson = new Gson();
                    finalObject = null;
                    for (int i = 0; i < parentArray.length(); i++) {
                        finalObject = parentArray.getJSONObject(i);
                    }

                    return finalObject;
                } else {
                    return null;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            if (result != null) {
                try {
//                    etDatosNumbreTarjeta.setText(" XXXX - XXXX - XXXX - " + result.getString("Tar_NumTarjeta").substring(result.getString("Tar_NumTarjeta").length() - 4, result.getString("Tar_NumTarjeta").length()));
//                    etDatosFechaExpiracionTarjeta.setText("  " + result.getString("Tar_MesVenci") + "/" + result.getString("Tar_Ano_Venci"));
//                    etDatosNombre.setText(" " + result.getString("Tar_NomTitular"));
//                    etDatosApellidos.setText(" " + result.getString("Tar_ApelTitular"));

                    setDatosNumbreTarjeta(" XXXX - XXXX - XXXX - " + result.getString("Tar_NumTarjeta").substring(result.getString("Tar_NumTarjeta").length() - 4, result.getString("Tar_NumTarjeta").length()));
                    setDatosFechaExpiracionTarjeta("  " + result.getString("Tar_MesVenci") + "/" + result.getString("Tar_Ano_Venci"));
                    setDatosNombre(" " + result.getString("Tar_NomTitular"));
                    setDatosApellidos(" " + result.getString("Tar_ApelTitular"));

//                    finalObjectMov = null;
//                    for (int i = 0; i < parentArrayMov.length(); i++) {
//                        finalObjectMov = parentArrayMov.getJSONObject(i);
//
//                        tr = new TableRow(getApplicationContext());
//                        tvFecha = new TextView(getApplicationContext());
//                        tvMonto = new TextView(getApplicationContext());
//                        tvDesc = new TextView(getApplicationContext());
//
//                        try {
//                            tvFecha.setText(finalObjectMov.getString("Tmov_Fecha"));
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        tvFecha.setTextSize(15);
//                        tvFecha.setGravity(Gravity.CENTER);
//                        tvFecha.setTextColor(Color.BLACK);
//                        try {
//                            tvMonto.setText(finalObjectMov.getString("Tmov_Monto"));
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        tvMonto.setTextSize(15);
//                        tvMonto.setGravity(Gravity.CENTER);
//                        tvMonto.setTextColor(Color.BLACK);
//                        try {
//                            tvDesc.setText(finalObjectMov.getString("Tmov_Desc"));
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        tvDesc.setTextSize(15);
//                        tvDesc.setGravity(Gravity.LEFT);
//                        tvDesc.setTextColor(Color.BLACK);
//                        tvDesc.setPadding(5, 0, 0,0 );
//
//                        tr.addView(tvDesc);
//                        tr.addView(tvFecha);
//                        tr.addView(tvMonto);
//
//                        tlMovimientos.addView(tr);
//                    }

                    Fragment fragment = new DatosTarjetaFragment();

                    FragmentManager manager = getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                    try {
                        transaction.replace(R.id.output, fragment, TAG);
                        transaction.commit();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
//                Toast.makeText(getApplicationContext(), "Sin conección a internet", Toast.LENGTH_SHORT).show();
            }
        }

        public String getPostDataString(JSONObject params) throws Exception {

            StringBuilder result = new StringBuilder();
            boolean first = true;

            Iterator<String> itr = params.keys();

            while (itr.hasNext()) {

                String key = itr.next();
                Object value = params.get(key);

                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(key, "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(value.toString(), "UTF-8"));

            }
            return result.toString();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public String getDatosNumbreTarjeta() {
        return datosNumbreTarjeta;
    }

    public static void setDatosNumbreTarjeta(String datosNumbreTarjeta) {
        datos_tarjeta.datosNumbreTarjeta = datosNumbreTarjeta;
    }

    public String getDatosFechaExpiracionTarjeta() {
        return datosFechaExpiracionTarjeta;
    }

    public static void setDatosFechaExpiracionTarjeta(String datosFechaExpiracionTarjeta) {
        datos_tarjeta.datosFechaExpiracionTarjeta = datosFechaExpiracionTarjeta;
    }

    public String getDatosNombre() {
        return datosNombre;
    }

    public static void setDatosNombre(String datosNombre) {
        datos_tarjeta.datosNombre = datosNombre;
    }

    public String getDatosApellidos() {
        return datosApellidos;
    }

    public static void setDatosApellidos(String datosApellidos) {
        datos_tarjeta.datosApellidos = datosApellidos;
    }

    public JSONArray getParentArrayMov() {
        return parentArrayMov;
    }

    public static void setParentArrayMov(JSONArray parentArrayMov) {
        datos_tarjeta.parentArrayMov = parentArrayMov;
    }
}
