package com.cardmovil.cardmovil;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cardmovil.cardmovil.json.row_promociones;
import com.cardmovil.cardmovil.models.list_negocios;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
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
import java.util.Iterator;

public class perfilNegocio extends AppCompatActivity {

    private static Context mContext;
    public LayoutInflater inflater;
    ImageView ivPerfill;
    TextView tvEstadoPerfil, etDescripcionPerfil, etTelefonoPerfil, etDireccionPerfil, tvHorarioPerfil;
    Bundle bolsa;
    private ProgressBar progressBar;
    String idperfil;
    public ListView lvPromociones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_negocio);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ivPerfill = (ImageView)findViewById(R.id.ivPerfill);
        tvEstadoPerfil = (TextView)findViewById(R.id.tvEstadoPerfil);
        etDescripcionPerfil = (TextView)findViewById(R.id.etDescripcionPerfil);
        etTelefonoPerfil = (TextView)findViewById(R.id.etTelefonoPerfil);
        etDireccionPerfil = (TextView)findViewById(R.id.etDireccionPerfil);
        tvHorarioPerfil = (TextView)findViewById(R.id.tvHorarioPerfil);
        progressBar = (ProgressBar) findViewById(R.id.progressBarPerfil);

        this.mContext = this;
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        lvPromociones = (ListView) findViewById(R.id.lvPromociones);

        bolsa = getIntent().getExtras();
        if (bolsa != null) {
            String json = bolsa.getString("l_perfilModel");
            list_negocios list_negociosModel = new Gson().fromJson(json, list_negocios.class);
            etDescripcionPerfil.setText(list_negociosModel.getSuc_descripcion());
            etTelefonoPerfil.setText(list_negociosModel.getSuc_telefijo());
            etDireccionPerfil.setText(list_negociosModel.getSuc_direccion());

            idperfil = list_negociosModel.getSuc_codigo();

            final String protocolo = "http://";
            String ip = getResources().getString(R.string.ipweb);
            String puerto = getResources().getString(R.string.puertoweb);
            puerto = puerto.equals("") ? "" : ":" + puerto;
            String url = protocolo + ip + puerto + "/";

            ImageLoader.getInstance().displayImage(url + list_negociosModel.getCli_logo(), ivPerfill, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    progressBar.setVisibility(View.GONE);
                }
            });

            row_promociones rwrow_promociones = new row_promociones();
            rwrow_promociones.execute(url + "/negocios/get_promociones", idperfil);

            new JSONTaskHorario().execute(url + "/negocios/get_horario", idperfil);

        }
    }

    public class JSONTaskHorario extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            int respuesta = 0;
            String respuesta_horario = "";

            try {
                URL url = new URL(params[0]);
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("sucursal", params[1]);

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
                    JSONArray parentArray = parentObject.getJSONArray("horarios");

                    StringBuffer finalBufferData = new StringBuffer();
                    Gson gson = new Gson();
                    for (int i = 0; i < parentArray.length(); i++) {
                        JSONObject finalObject = parentArray.getJSONObject(i);
                        respuesta_horario = finalObject.toString();
                        tvHorarioPerfil.setText(finalObject.getString("Hor_Dia1S") + " - " + finalObject.getString("Hor_Dia2S") + "\n" + finalObject.getString("Hor_Ini") + " - " + finalObject.getString("Hor_Fin"));
                    }
                    return respuesta_horario;
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
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                Log.d("resultado", result);
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

    public static Context getContext() {
        return mContext;
    }

    public void mostrarImagen(View v) {
        Intent intent = new Intent(getApplicationContext(), zoomImagen.class);
        intent.putExtra("urlImagen", v.getTag().toString());
        startActivity(intent);
    }
}
