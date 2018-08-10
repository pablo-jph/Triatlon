package com.cardmovil.cardmovil;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cardmovil.cardmovil.models.list_categorias;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class Registrar extends AppCompatActivity {

    EditText etEmail, etPassword, etName, etPLastName, etSLastName, etDireccion, etTelefono, etNumbreDocument;
    CheckBox cbTerminos;
    String url;
    Button registrar;
    public String RPTA_msg = "";
    public Spinner sPais, sDepartamento, sProvincia, sDistrito, sTipoDocumento;
    ArrayList<String> listPais = new ArrayList<String>();
    ArrayList<String> listIdPais = new ArrayList<String>();
    ArrayList<String> listDepartamento = new ArrayList<String>();
    ArrayList<String> listIdDepartamento = new ArrayList<String>();
    ArrayList<String> listProvincia = new ArrayList<String>();
    ArrayList<String> listIdProvincia = new ArrayList<String>();
    ArrayList<String> listDistrito = new ArrayList<String>();
    ArrayList<String> listIdDistrito = new ArrayList<String>();
    ArrayList<String> listTipodocumento = new ArrayList<String>();
    ArrayList<String> listIdTipoDocumento = new ArrayList<String>();
    TextView tvTerminos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etName = (EditText) findViewById(R.id.etName);
        etPLastName = (EditText) findViewById(R.id.etPLastName);
        etSLastName = (EditText) findViewById(R.id.etSLastName);
        etDireccion = (EditText) findViewById(R.id.etDireccion);
        etTelefono = (EditText) findViewById(R.id.etTelefono);
        etNumbreDocument = (EditText) findViewById(R.id.etNumbreDocument);
        registrar = (Button) findViewById(R.id.btnRegistrar);
        sPais = (Spinner) findViewById(R.id.sPais);
        sDepartamento = (Spinner) findViewById(R.id.sDepartamento);
        sProvincia = (Spinner) findViewById(R.id.sProvincia);
        sDistrito = (Spinner) findViewById(R.id.sDistrito);
        sTipoDocumento = (Spinner) findViewById(R.id.sTipoDocumento);
        cbTerminos = (CheckBox) findViewById(R.id.cbTerminos);
        tvTerminos = (TextView) findViewById(R.id.tvTerminos);

        etName.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        etPLastName.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        etSLastName.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        etDireccion.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        tvTerminos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), terminosCondiciones.class);
                startActivity(i);
            }
        });

        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Reset errors.
                etEmail.setError(null);
                etPassword.setError(null);
                etName.setError(null);
                etPLastName.setError(null);
                etSLastName.setError(null);
                etDireccion.setError(null);
                etTelefono.setError(null);
                etNumbreDocument.setError(null);

                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                String nombre = etName.getText().toString();
                String a_paterno = etPLastName.getText().toString();
                String a_materno = etSLastName.getText().toString();
                String direccion = etDireccion.getText().toString();
                String telefono = etTelefono.getText().toString();
                String numero_documento = etNumbreDocument.getText().toString();

                boolean cancel = false;
                View focusView = null;

                if (TextUtils.isEmpty(email)) {
                    etEmail.setError("Email Obligatorio");
                    focusView = etEmail;
                    cancel = true;
                } else if(!email.matches("[a-zA-Z0-9._-]+@[a-z]+.[a-z]+")){
                    etEmail.setError("Email Incorrecto");
                    focusView = etEmail;
                    cancel = true;
                }else if (!isPasswordValid(password)) {
                    etPassword.setError("Password Obligatorio");
                    focusView = etPassword;
                    cancel = true;
                }else if (TextUtils.isEmpty(nombre)) {
                    etName.setError("Nombre Obligatorio");
                    focusView = etName;
                    cancel = true;
                }else if (TextUtils.isEmpty(a_paterno)) {
                    etPLastName.setError("Apellido Obligatorio");
                    focusView = etPLastName;
                    cancel = true;
                }else if (TextUtils.isEmpty(a_materno)) {
                    etSLastName.setError("Apellido Obligatorio");
                    focusView = etSLastName;
                    cancel = true;
                }else if (TextUtils.isEmpty(direccion)) {
                    etDireccion.setError("Direccion Obligatorio");
                    focusView = etDireccion;
                    cancel = true;
                }else if (TextUtils.isEmpty(telefono)) {
                    etTelefono.setError("Direccion Obligatorio");
                    focusView = etTelefono;
                    cancel = true;
                }else if (TextUtils.isEmpty(numero_documento)) {
                    etNumbreDocument.setError("Numero de documento Obligatorio");
                    focusView = etNumbreDocument;
                    cancel = true;
                }else if (numero_documento.length() < 8) {
                    etNumbreDocument.setError("Digitos Incompletos");
                    focusView = etNumbreDocument;
                    cancel = true;
                }else if(!cbTerminos.isChecked()){
                    cbTerminos.setError("Aceptar terminos y condiciones");
                    Toast.makeText(getApplicationContext(), "Aceptar terminos y condiciones", Toast.LENGTH_SHORT).show();
                    focusView = cbTerminos;
                    cancel = true;
                }else if(listIdPais.get(sPais.getSelectedItemPosition()).equals("0")){
                    Toast.makeText(getApplicationContext(), "Seleccionar Pais", Toast.LENGTH_SHORT).show();
                    focusView = sPais;
                    cancel = true;
                }else if(listIdDepartamento.get(sDepartamento.getSelectedItemPosition()).equals("0")){
                    Toast.makeText(getApplicationContext(), "Seleccionar Departamento", Toast.LENGTH_SHORT).show();
                    focusView = sDepartamento;
                    cancel = true;
                }else if(listIdProvincia.get(sProvincia.getSelectedItemPosition()).equals("0")){
                    Toast.makeText(getApplicationContext(), "Seleccionar Provincia", Toast.LENGTH_SHORT).show();
                    focusView = sProvincia;
                    cancel = true;
                }else if(listIdDistrito.get(sDistrito.getSelectedItemPosition()).equals("0")){
                    Toast.makeText(getApplicationContext(), "Seleccionar Distrito", Toast.LENGTH_SHORT).show();
                    focusView = sDistrito;
                    cancel = true;
                }else if(listIdTipoDocumento.get(sTipoDocumento.getSelectedItemPosition()).equals("0")){
                    Toast.makeText(getApplicationContext(), "Seleccionar Tipo Documento", Toast.LENGTH_SHORT).show();
                    focusView = sTipoDocumento;
                    cancel = true;
                }

                if (cancel) {
                    focusView.requestFocus();
                } else {
                    final String protocolo = "http://";
                    String ip = getResources().getString(R.string.ipweb);
                    String puerto = getResources().getString(R.string.puertoweb);
                    puerto = puerto.equals("") ? "" : ":" + puerto;

                    url = protocolo + ip + puerto + "/pagador/registro";
                    new JSONTask().execute(url);
                }
            }
        });

        sPais.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                final String protocolo = "http://";
                String ip = getResources().getString(R.string.ipweb);
                String puerto = getResources().getString(R.string.puertoweb);
                puerto = puerto.equals("") ? "" : ":" + puerto;
                String pais_seleccionado = "0";

                if(!(sPais == null)) {
                    pais_seleccionado = String.valueOf(sPais.getSelectedItemPosition());
                }

                pais_seleccionado = pais_seleccionado.equals("-1") ? "1" : listIdPais.get(sPais.getSelectedItemPosition());

                url = protocolo + ip + puerto +  "/pagador/get_departamentos_by_pais";
                new JSONDepartamentosTask().execute(url, pais_seleccionado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sDepartamento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                final String protocolo = "http://";
                String ip = getResources().getString(R.string.ipweb);
                String puerto = getResources().getString(R.string.puertoweb);
                puerto = puerto.equals("") ? "" : ":" + puerto;
                String departamento_seleccionado = "0";
                String pais_seleccionado = "0";

                if(!(sDepartamento == null)) {
                    departamento_seleccionado = String.valueOf(sDepartamento.getSelectedItemPosition());
                }

                departamento_seleccionado = departamento_seleccionado.equals("-1") ? "1" : listIdDepartamento.get(sDepartamento.getSelectedItemPosition());
                pais_seleccionado = pais_seleccionado.equals("-1") ? "1" : listIdPais.get(sPais.getSelectedItemPosition());

                url = protocolo + ip + puerto +  "/pagador/get_provincia_by_departamento";
                new JSONProvinciaTask().execute(url, departamento_seleccionado, pais_seleccionado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sProvincia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                final String protocolo = "http://";
                String ip = getResources().getString(R.string.ipweb);
                String puerto = getResources().getString(R.string.puertoweb);
                puerto = puerto.equals("") ? "" : ":" + puerto;
                String provincia_seleccionado = "0";
                String departamento_seleccionado = "0";
                String pais_seleccionado = "0";

                if(!(sProvincia == null)) {
                    provincia_seleccionado = String.valueOf(sProvincia.getSelectedItemPosition());
                }

                provincia_seleccionado = provincia_seleccionado.equals("-1") ? "1" : listIdProvincia.get(sProvincia.getSelectedItemPosition());
                departamento_seleccionado = departamento_seleccionado.equals("-1") ? "1" : listIdDepartamento.get(sDepartamento.getSelectedItemPosition());
                pais_seleccionado = pais_seleccionado.equals("-1") ? "1" : listIdPais.get(sPais.getSelectedItemPosition());

                url = protocolo + ip + puerto +  "/pagador/get_distrito_by_provincia";
                new JSONDistritoTask().execute(url, provincia_seleccionado, departamento_seleccionado, pais_seleccionado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final String protocolo = "http://";
        String ip = getResources().getString(R.string.ipweb);
        String puerto = getResources().getString(R.string.puertoweb);
        puerto = puerto.equals("") ? "" : ":" + puerto;

        url = protocolo + ip + puerto +  "/pagador/get_datos_registro";
        new JSONGetDatosTask().execute(url);
    }

    public void cargarPaises(){
        ArrayAdapter<String> ad = new ArrayAdapter<String>(this , android.R.layout.simple_spinner_dropdown_item, listPais );
        sPais.setAdapter(ad);
    }

    public void cargarDepartamentos(){
        ArrayAdapter<String> ad = new ArrayAdapter<String>(this , android.R.layout.simple_spinner_dropdown_item, listDepartamento );
        sDepartamento.setAdapter(ad);
    }

    public void cargarProvincias(){
        ArrayAdapter<String> ad = new ArrayAdapter<String>(this , android.R.layout.simple_spinner_dropdown_item, listProvincia );
        sProvincia.setAdapter(ad);
    }

    public void cargarDistritos(){
        ArrayAdapter<String> ad = new ArrayAdapter<String>(this , android.R.layout.simple_spinner_dropdown_item, listDistrito );
        sDistrito.setAdapter(ad);
    }

    public void cargarTipoDocumento(){
        ArrayAdapter<String> ad = new ArrayAdapter<String>(this , android.R.layout.simple_spinner_dropdown_item, listTipodocumento );
        sTipoDocumento.setAdapter(ad);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("email", etEmail.getText());
                postDataParams.put("password", etPassword.getText());
                postDataParams.put("name", etName.getText());
                postDataParams.put("a_materno", etPLastName.getText());
                postDataParams.put("a_paterno", etSLastName.getText());
                postDataParams.put("direccion", etDireccion.getText());
                postDataParams.put("telefono", etTelefono.getText());
                postDataParams.put("codigo_pais", listIdPais.get(sPais.getSelectedItemPosition()));
                postDataParams.put("departamento", listIdDepartamento.get(sDepartamento.getSelectedItemPosition()));
                postDataParams.put("provincia", listIdProvincia.get(sProvincia.getSelectedItemPosition()));
                postDataParams.put("distrito", listIdDistrito.get(sDistrito.getSelectedItemPosition()));
                postDataParams.put("tipo_documento", listIdTipoDocumento.get(sTipoDocumento.getSelectedItemPosition()));
                postDataParams.put("num_documento", etNumbreDocument.getText());

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
                RPTA_msg = jsonRpta.getString("menssage");
                if(ok) {
                    Toast.makeText(getApplicationContext(), "Registrado correctamente", Toast.LENGTH_SHORT).show();
                    focusView = registrar;
                    etEmail.setText("");
                    etPassword.setText("");
                    etName.setText("");
                    etPLastName.setText("");
                    etSLastName.setText("");
                    etDireccion.setText("");
                    etTelefono.setText("");
                    etNumbreDocument.setText("");

                    final String protocolo = "http://";
                    String ip = getResources().getString(R.string.ipweb);
                    String puerto = getResources().getString(R.string.puertoweb);
                    puerto = puerto.equals("") ? "" : ":" + puerto;

                    url = protocolo + ip + puerto +  "/pagador/get_datos_registro";
                    new JSONGetDatosTask().execute(url);
                }else if(RPTA_msg.equals("Email registrado")){
                    etEmail.setError(RPTA_msg);
                    etEmail.setText("");
                    focusView = etEmail;
                }else if(RPTA_msg.equals("DNI registrado")){
                    etNumbreDocument.setError(RPTA_msg);
                    etNumbreDocument.setText("");
                    focusView = etNumbreDocument;
                }
                focusView.requestFocus();
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
                JSONArray negociosArray = parentObject.getJSONArray("negocios");
                JSONArray tDocumentoArray = parentObject.getJSONArray("tipo_documento");

                listPais.clear();
                listIdPais.clear();
                listTipodocumento.clear();
                listIdTipoDocumento.clear();

                listPais.add("Seleccionar País");
                listIdPais.add("0");
                listTipodocumento.add("Seleccionar Tipo de Documento");
                listIdTipoDocumento.add("0");


                for (int i = 0; i < negociosArray.length(); i++) {
                    JSONObject finalObject = negociosArray.getJSONObject(i);
                    listPais.add(finalObject.getString("Pais_nombre"));
                    listIdPais.add(finalObject.getString("Pais_codigo"));
                }
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
                cargarPaises();
                cargarTipoDocumento();
            } else {
                Toast.makeText(getApplicationContext(), "Sin Conexion a Internet", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class JSONDepartamentosTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            int respuesta = 0;

            try {
                URL url = new URL(params[0]);
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("idpais", params[1]);

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
                    JSONArray parentArray = parentObject.getJSONArray("departamentos");

                    listDepartamento.clear();
                    listIdDepartamento.clear();

                    listDepartamento.add("Seleccionar Departamento");
                    listIdDepartamento.add("0");

                    StringBuffer finalBufferData = new StringBuffer();
                    Gson gson = new Gson();
                    for (int i = 0; i < parentArray.length(); i++) {
                        JSONObject finalObject = parentArray.getJSONObject(i);
                        listDepartamento.add(finalObject.getString("Depa_desc"));
                        listIdDepartamento.add(finalObject.getString("Depa_cod"));
                    }
                    return true;
                }else{
                    return false;
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
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            if (result) {
                cargarDepartamentos();
            } else {
                Toast.makeText(getApplicationContext(), "Sin Coneccion a internet", Toast.LENGTH_SHORT).show();
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

    public class JSONProvinciaTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            int respuesta = 0;

            try {
                URL url = new URL(params[0]);
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("iddepartamento", params[1]);
                postDataParams.put("idpais", params[2]);
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
                    JSONArray parentArray = parentObject.getJSONArray("provincias");

                    listProvincia.clear();
                    listIdProvincia.clear();

                    listProvincia.add("Seleccionar Provincia");
                    listIdProvincia.add("0");

                    StringBuffer finalBufferData = new StringBuffer();
                    Gson gson = new Gson();
                    for (int i = 0; i < parentArray.length(); i++) {
                        JSONObject finalObject = parentArray.getJSONObject(i);
                        listProvincia.add(finalObject.getString("Prov_desc"));
                        listIdProvincia.add(finalObject.getString("Prov_cod"));
                    }
                    return true;
                }else{
                    return false;
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
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            if (result) {
                cargarProvincias();
            } else {
                Toast.makeText(getApplicationContext(), "Sin Coneccion a internet", Toast.LENGTH_SHORT).show();
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

    public class JSONDistritoTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            int respuesta = 0;

            try {
                URL url = new URL(params[0]);
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("idprovincia", params[1]);
                postDataParams.put("iddepartamento", params[2]);
                postDataParams.put("idpais", params[3]);

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
                    JSONArray parentArray = parentObject.getJSONArray("distrito");

                    listDistrito.clear();
                    listIdDistrito.clear();

                    listDistrito.add("Seleccionar Distrito");
                    listIdDistrito.add("0");

                    StringBuffer finalBufferData = new StringBuffer();
                    Gson gson = new Gson();
                    for (int i = 0; i < parentArray.length(); i++) {
                        JSONObject finalObject = parentArray.getJSONObject(i);
                        listDistrito.add(finalObject.getString("Dist_desc"));
                        listIdDistrito.add(finalObject.getString("Dist_cod"));
                    }
                    return true;
                }else{
                    return false;
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
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            if (result) {
                cargarDistritos();
            } else {
                Toast.makeText(getApplicationContext(), "Sin Coneccion a internet", Toast.LENGTH_SHORT).show();
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

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 2;
    }

    public static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }
}
