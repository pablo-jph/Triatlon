package com.cardmovil.cardmovil;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class add_tarjeta extends AppCompatActivity {

    EditText etNumbreTarjeta, etFechaExpiracionTarjeta, etNombre, etApellidos, etNombreTarjeta;
    public Spinner sPaisTarjeta, sBancoTarjeta;
    String url, usuario, pais, distrito;
    public String RPTA_msg = "";
    Button btnGuardarTarjeta;
    ArrayList<String> listPais = new ArrayList<String>();
    ArrayList<String> listIdPais = new ArrayList<String>();
    ArrayList<String> listBanco = new ArrayList<String>();
    ArrayList<String> listIdBanco = new ArrayList<String>();
    Bundle bolsa;
    private RadioGroup grupo;
    String latitud, longitud;

    //FORMATO PARA FECHA VENCIMIENTO
    SimpleDateFormat sdf = new SimpleDateFormat("MM/yy");
    SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd");

    int textlength = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tarjeta);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etNumbreTarjeta = (EditText) findViewById(R.id.etNumbreTarjeta);
        etFechaExpiracionTarjeta = (EditText) findViewById(R.id.etFechaExpiracionTarjeta);
        etNombre = (EditText) findViewById(R.id.etNombre);
        etApellidos = (EditText) findViewById(R.id.etApellidos);
        etNombreTarjeta = (EditText) findViewById(R.id.etNombreTarjeta);
        sPaisTarjeta = (Spinner) findViewById(R.id.sPaisTarjeta);
        sBancoTarjeta = (Spinner) findViewById(R.id.sBancoTarjeta);
        grupo = (RadioGroup) findViewById(R.id.opciones_tarjeta);
        btnGuardarTarjeta = (Button) findViewById(R.id.btnGuardarTarjeta);

        etNombre.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        etApellidos.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        etNombreTarjeta.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        etNumbreTarjeta.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_DEL) {
                    //this is for backspace
                }else {
                    String text = etNumbreTarjeta.getText().toString();
                    textlength = etNumbreTarjeta.getText().length();

                    if (textlength == 4 || textlength == 9 || textlength == 14 ) {
                        etNumbreTarjeta.setText(new StringBuilder(text).insert(text.length(), "-").toString());
                        etNumbreTarjeta.setSelection(etNumbreTarjeta.getText().length());
                    }
                }
                return false;
            }
        });

        etFechaExpiracionTarjeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MonthYearPickerDialog pickerDialog = new MonthYearPickerDialog();
                pickerDialog.setListener(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int i2) {
                        String monthYearStr = year + "-" + (month + 1) + "-" + i2;
                        etFechaExpiracionTarjeta.setText(formatMonthYear(monthYearStr));
                    }
                });
                pickerDialog.show(getSupportFragmentManager(), "Fecha Vencimiento");
            }
        });

        bolsa = getIntent().getExtras();
        if (bolsa != null) {
            usuario = bolsa.getString("usuario");
            pais = bolsa.getString("pais");
            distrito = bolsa.getString("distrito");
            latitud = bolsa.getString("latitud");
            longitud = bolsa.getString("longitud");
        }

        btnGuardarTarjeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset errors.
                etNumbreTarjeta.setError(null);
                etFechaExpiracionTarjeta.setError(null);
                etNombre.setError(null);
                etApellidos.setError(null);
                etNombreTarjeta.setError(null);

                String num_tarjeta = etNumbreTarjeta.getText().toString();
                String fecha_exp = etFechaExpiracionTarjeta.getText().toString();
                String nombre = etNombre.getText().toString();
                String apellidos = etApellidos.getText().toString();
                String nombre_tarjeta = etNombreTarjeta.getText().toString();

                boolean cancel = false;
                View focusView = null;

                if (TextUtils.isEmpty(num_tarjeta)) {
                    etNumbreTarjeta.setError("Numero de Tarjeta Obligatorio");
                    focusView = etNumbreTarjeta;
                    cancel = true;
                } else if (num_tarjeta.length() < 19) {
                    etNumbreTarjeta.setError("Digitos Incompletos");
                    focusView = etNumbreTarjeta;
                    cancel = true;
                } else if (TextUtils.isEmpty(fecha_exp)) {
                    etFechaExpiracionTarjeta.setError("Fecha Expiración Obligatorio");
                    focusView = etFechaExpiracionTarjeta;
                    cancel = true;
                } else if (TextUtils.isEmpty(nombre)) {
                    etNombre.setError("Nombre Obligatorio");
                    focusView = etNombre;
                    cancel = true;
                } else if (TextUtils.isEmpty(apellidos)) {
                    etApellidos.setError("Apellidos Obligatorio");
                    focusView = etApellidos;
                    cancel = true;
                } else if (TextUtils.isEmpty(nombre_tarjeta)) {
                    etNombreTarjeta.setError("Nombre Tarjeta Obligatorio");
                    focusView = etNombreTarjeta;
                    cancel = true;
                } else if (listIdPais.get(sPaisTarjeta.getSelectedItemPosition()).equals("0")) {
                    Toast.makeText(getApplicationContext(), "Seleccionar Pais", Toast.LENGTH_SHORT).show();
                    focusView = sPaisTarjeta;
                    cancel = true;
                } else if (listIdBanco.get(sBancoTarjeta.getSelectedItemPosition()).equals("0")) {
                    Toast.makeText(getApplicationContext(), "Seleccionar Departamento", Toast.LENGTH_SHORT).show();
                    focusView = sBancoTarjeta;
                    cancel = true;
                }

                if (cancel) {
                    focusView.requestFocus();
                } else {
                    final String protocolo = "http://";
                    String ip = getResources().getString(R.string.ipweb);
                    String puerto = getResources().getString(R.string.puertoweb);
                    puerto = puerto.equals("") ? "" : ":" + puerto;

                    url = protocolo + ip + puerto + "/tarjetas/registro_tarjeta";
                    new JSONTask().execute(url);
                }
            }
        });

        final String protocolo = "http://";
        String ip = getResources().getString(R.string.ipweb);
        String puerto = getResources().getString(R.string.puertoweb);
        puerto = puerto.equals("") ? "" : ":" + puerto;

        url = protocolo + ip + puerto + "/tarjetas/get_datos_registro_tarjeta";
        new JSONGetDatosTask().execute(url);
    }

    String formatMonthYear(String str) {
        Date date = null;
        try {
            date = input.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return sdf.format(date);
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
                JSONArray paisesArray = parentObject.getJSONArray("paises");
                JSONArray bancosArray = parentObject.getJSONArray("bancos");

                listPais.clear();
                listIdPais.clear();
                listBanco.clear();
                listIdBanco.clear();

                listPais.add("Seleccionar País");
                listIdPais.add("0");
                listBanco.add("Seleccionar Banco");
                listIdBanco.add("0");


                for (int i = 0; i < paisesArray.length(); i++) {
                    JSONObject finalObject = paisesArray.getJSONObject(i);
                    listPais.add(finalObject.getString("Pais_nombre"));
                    listIdPais.add(finalObject.getString("Pais_codigo"));
                }
                for (int i = 0; i < bancosArray.length(); i++) {
                    JSONObject finalObject = bancosArray.getJSONObject(i);
                    listBanco.add(finalObject.getString("Ban_desc"));
                    listIdBanco.add(finalObject.getString("Ban_codigo"));
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
                cargarBancos();
            } else {
                Toast.makeText(getApplicationContext(), "Sin Conexion a Internet", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void cargarPaises() {
        ArrayAdapter<String> ad = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, listPais);
        sPaisTarjeta.setAdapter(ad);
    }

    public void cargarBancos() {
        ArrayAdapter<String> ad = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, listBanco);
        sBancoTarjeta.setAdapter(ad);
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

                String string = String.valueOf(etFechaExpiracionTarjeta.getText());
                String[] parts = string.split("/");

                postDataParams.put("usuario", usuario);
                postDataParams.put("numero_tarjeta", etNumbreTarjeta.getText());
                postDataParams.put("mes_expiracion", parts[0]);
                postDataParams.put("anio_expiracion", parts[1]);
                postDataParams.put("nombre", etNombre.getText());
                postDataParams.put("apellidos", etApellidos.getText());
                postDataParams.put("codigo_pais", listIdPais.get(sPaisTarjeta.getSelectedItemPosition()));
                postDataParams.put("nombre_tarjeta", etNombreTarjeta.getText());
                postDataParams.put("codigo_banco", listIdBanco.get(sBancoTarjeta.getSelectedItemPosition()));
                if (grupo.getCheckedRadioButtonId() == R.id.radio_cerdito) {
                    postDataParams.put("tipo", "1");
                } else if (grupo.getCheckedRadioButtonId() == R.id.radio_debito) {
                    postDataParams.put("tipo", "2");
                }

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
                if (ok) {
                    Toast.makeText(getApplicationContext(), "Registrado correctamente", Toast.LENGTH_SHORT).show();
                    etNumbreTarjeta.setText("");
                    etFechaExpiracionTarjeta.setText("");
                    etNombre.setText("");
                    etApellidos.setText("");
                    etNombreTarjeta.setText("");
                    Intent intent = new Intent(getApplicationContext(), categorias.class);
                    intent.putExtra("usuario", usuario);
                    intent.putExtra("pais", pais);
                    intent.putExtra("distrito", distrito);
                    startActivity(intent);
                }else{
                    etNumbreTarjeta.setError(RPTA_msg);
                    etNumbreTarjeta.setText("");
                    focusView = etNumbreTarjeta;
                    focusView.requestFocus();
                }
            } catch (JSONException e) {
                e.printStackTrace();
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
}
