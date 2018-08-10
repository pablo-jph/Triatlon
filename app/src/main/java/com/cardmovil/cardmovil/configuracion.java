package com.cardmovil.cardmovil;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class configuracion extends AppCompatActivity {

    EditText etCuentaEmail, etCuentaName, etCuentaApaterno, etCuentaAmaterno, etCuentaNumeroDocumento;
    Spinner sCuentaTipoDocumento;
    Button btnGuardarDatos;
    private TabHost tabs;
    String id_usuario_configuracion, url_edit_datos = "";
    Bundle bolsa;
    ArrayList<String> listTipodocumento = new ArrayList<String>();
    ArrayList<String> listIdTipoDocumento = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etCuentaEmail = (EditText)findViewById(R.id.etCuentaEmail);
        etCuentaName = (EditText)findViewById(R.id.etCuentaName);
        etCuentaApaterno = (EditText)findViewById(R.id.etCuentaApaterno);
        etCuentaAmaterno = (EditText)findViewById(R.id.etCuentaAmaterno);
        etCuentaNumeroDocumento = (EditText)findViewById(R.id.etCuentaNumeroDocumento);
        sCuentaTipoDocumento = (Spinner) findViewById(R.id.sCuentaTipoDocumento);
        btnGuardarDatos = (Button) findViewById(R.id.btnGuardarDatos);

        bolsa = getIntent().getExtras();
        if (bolsa != null) {
            id_usuario_configuracion = bolsa.getString("usuario");
            final String protocolo = "http://";
            String ip = getResources().getString(R.string.ipweb);
            String puerto = getResources().getString(R.string.puertoweb);
            puerto = puerto.equals("") ? "" : ":" + puerto;
            String url = protocolo + ip + puerto + "/";
            String url_tipo_documento = protocolo + ip + puerto + "/";

            url_tipo_documento = protocolo + ip + puerto +  "/pagador/get_datos_registro";
            new JSONGetDatosTask().execute(url_tipo_documento);

            new JSONTaskDatos().execute(url + "/pagador/get_datos_pagador", id_usuario_configuracion);
        }

        tabs = (TabHost) findViewById(R.id.thConfiguracion);
        tabs.setVisibility(View.VISIBLE);
        tabs.setup();
        TabHost.TabSpec spec = tabs.newTabSpec("tabCuenta");

        View tabIndicator = LayoutInflater.from(this).inflate(R.layout.indicator_cuenta, tabs.getTabWidget(), false);
        TextView title = (TextView) tabIndicator.findViewById(R.id.titleCuenta);
        title.setText("Cuenta");
        tabIndicator.setTag("tabCuenta");

        spec.setIndicator(tabIndicator);
        spec.setContent(R.id.tabCuenta);
        tabs.addTab(spec);

        spec = tabs.newTabSpec("tabUbicacion");

        View tabIndicatorU = LayoutInflater.from(this).inflate(R.layout.indicator_cuenta, tabs.getTabWidget(), false);
        TextView titleU = (TextView) tabIndicatorU.findViewById(R.id.titleCuenta);
        titleU.setText("Ubicación");
        tabIndicatorU.setTag("tabUbicacion");

        spec.setIndicator(tabIndicatorU);
        spec.setContent(R.id.tabUbicacion);
        tabs.addTab(spec);

        spec = tabs.newTabSpec("tabPassword");

        View tabIndicatorP = LayoutInflater.from(this).inflate(R.layout.indicator_cuenta, tabs.getTabWidget(), false);
        TextView titleP = (TextView) tabIndicatorP.findViewById(R.id.titleCuenta);
        titleP.setText("Contraseña");
        tabIndicatorP.setTag("tabPassword");

        spec.setIndicator(tabIndicatorP);
        spec.setContent(R.id.tabPassword);
        tabs.addTab(spec);

        btnGuardarDatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset errors.
                etCuentaEmail.setError(null);
                etCuentaName.setError(null);
                etCuentaApaterno.setError(null);
                etCuentaAmaterno.setError(null);
                etCuentaNumeroDocumento.setError(null);

                String email = etCuentaEmail.getText().toString();
                String nombre = etCuentaName.getText().toString();
                String a_paterno = etCuentaApaterno.getText().toString();
                String a_materno = etCuentaAmaterno.getText().toString();
                String numero_documento = etCuentaNumeroDocumento.getText().toString();

                boolean cancel = false;
                View focusView = null;

                if (TextUtils.isEmpty(email)) {
                    etCuentaEmail.setError("Email Obligatorio");
                    focusView = etCuentaEmail;
                    cancel = true;
                } else if (TextUtils.isEmpty(nombre)) {
                    etCuentaName.setError("Nombre Obligatorio");
                    focusView = etCuentaName;
                    cancel = true;
                }else if (TextUtils.isEmpty(a_paterno)) {
                    etCuentaApaterno.setError("Apellido Obligatorio");
                    focusView = etCuentaApaterno;
                    cancel = true;
                }else if (TextUtils.isEmpty(a_materno)) {
                    etCuentaAmaterno.setError("Apellido Obligatorio");
                    focusView = etCuentaAmaterno;
                    cancel = true;
                }else if (TextUtils.isEmpty(numero_documento)) {
                    etCuentaNumeroDocumento.setError("Numero de documento Obligatorio");
                    focusView = etCuentaNumeroDocumento;
                    cancel = true;
                }else if(listIdTipoDocumento.get(sCuentaTipoDocumento.getSelectedItemPosition()).equals("0")){
                    Toast.makeText(getApplicationContext(), "Seleccionar Tipo Documento", Toast.LENGTH_SHORT).show();
                    focusView = sCuentaTipoDocumento;
                    cancel = true;
                }

                if (cancel) {
                    focusView.requestFocus();
                } else {
                    final String protocolo = "http://";
                    String ip = getResources().getString(R.string.ipweb);
                    String puerto = getResources().getString(R.string.puertoweb);
                    puerto = puerto.equals("") ? "" : ":" + puerto;

                    url_edit_datos = protocolo + ip + puerto + "/pagador/update_pagador";
                    new JSONTaskUpdateDatos().execute(url_edit_datos);
                }

            }
        });
    }

    public class JSONTaskUpdateDatos extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);

                JSONObject postDataParams = new JSONObject();

                postDataParams.put("id_usuario", id_usuario_configuracion);
                postDataParams.put("email", etCuentaEmail.getText());
                postDataParams.put("name", etCuentaName.getText());
                postDataParams.put("a_materno", etCuentaApaterno.getText());
                postDataParams.put("a_paterno", etCuentaAmaterno.getText());
                postDataParams.put("tipo_documento", listIdTipoDocumento.get(sCuentaTipoDocumento.getSelectedItemPosition()));
                postDataParams.put("num_documento", etCuentaNumeroDocumento.getText());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                } else {
                    return new String("false : " + responseCode);
                }

            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                View focusView = null;
                JSONObject jsonRpta = new JSONObject(result);
                Boolean ok = jsonRpta.getBoolean("status");
//                RPTA_msg = jsonRpta.getString("menssage");
//                if(ok) {
                    Toast.makeText(getApplicationContext(), "Actualizado correctamente", Toast.LENGTH_SHORT).show();
//                    focusView = registrar;
//                    etEmail.setText("");
//                    etPassword.setText("");
//                    etName.setText("");
//                    etPLastName.setText("");
//                    etSLastName.setText("");
//                    etDireccion.setText("");
//                    etTelefono.setText("");
//                    etNumbreDocument.setText("");

//                    final String protocolo = "http://";
//                    String ip = getResources().getString(R.string.ipweb);
//                    String puerto = getResources().getString(R.string.puertoweb);
//                    puerto = puerto.equals("") ? "" : ":" + puerto;

//                    url = protocolo + ip + puerto +  "/pagador/get_datos_registro";
//                    new Registrar.JSONGetDatosTask().execute(url);
//                }else if(RPTA_msg.equals("Email registrado")){
//                    etEmail.setError(RPTA_msg);
//                    etEmail.setText("");
//                    focusView = etEmail;
//                }else if(RPTA_msg.equals("DNI registrado")){
//                    etNumbreDocument.setError(RPTA_msg);
//                    etNumbreDocument.setText("");
//                    focusView = etNumbreDocument;
//                }
//                focusView.requestFocus();
            } catch (JSONException e) {
                e.printStackTrace();
            }

//            finish();
//            startActivity(getIntent());
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

    public class JSONTaskDatos extends AsyncTask<String, String, JSONObject> {

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
                postDataParams.put("usuario", params[1]);

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
                    JSONArray parentArray = parentObject.getJSONArray("datos");

                    StringBuffer finalBufferData = new StringBuffer();
                    Gson gson = new Gson();
                    JSONObject finalObject = null;
                    for (int i = 0; i < parentArray.length(); i++) {
                        finalObject = parentArray.getJSONObject(i);
                        respuesta_horario = finalObject.toString();
                    }
                    return finalObject;
                }else{
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
                    etCuentaEmail.setText(result.getString("Pag_email"));
                    etCuentaName.setText(result.getString("Pag_nombre1"));
                    etCuentaApaterno.setText(result.getString("Pag_apepat"));
                    etCuentaAmaterno.setText(result.getString("Pag_apemat"));
                    etCuentaNumeroDocumento.setText(result.getString("Pag_numidentidad"));
                    sCuentaTipoDocumento.setSelection(Integer.parseInt(result.getString("TDoc_codigo")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Sin conección a internet", Toast.LENGTH_SHORT).show();
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

    public class JSONGetDatosTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String finalJson = buffer.toString();

                JSONObject parentObject = new JSONObject(finalJson);
                JSONArray tDocumentoArray = parentObject.getJSONArray("tipo_documento");

                listTipodocumento.clear();
                listIdTipoDocumento.clear();

                listTipodocumento.add("Seleccionar Tipo de Documento");
                listIdTipoDocumento.add("0");


                for (int i = 0; i < tDocumentoArray.length(); i++) {
                    JSONObject finalObject = tDocumentoArray.getJSONObject(i);
                    listTipodocumento.add(finalObject.getString("TDoc_desc"));
                    listIdTipoDocumento.add(finalObject.getString("TDoc_codigo"));
                }
                return true;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
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
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            if (result) {
                cargarTipoDocumento();
            } else {
                Toast.makeText(getApplicationContext(), "Sin Conexion a Internet", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void cargarTipoDocumento(){
        ArrayAdapter<String> ad = new ArrayAdapter<String>(this , android.R.layout.simple_spinner_dropdown_item, listTipodocumento );
        sCuentaTipoDocumento.setAdapter(ad);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
