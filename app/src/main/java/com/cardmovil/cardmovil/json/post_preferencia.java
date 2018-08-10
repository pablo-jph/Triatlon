package com.cardmovil.cardmovil.json;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

import com.cardmovil.cardmovil.categorias;
import com.cardmovil.cardmovil.negocios;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class post_preferencia extends AsyncTask<String, String, String> {

    String RPTA_id;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            URL url = new URL(params[0]);

            JSONObject postDataParams = new JSONObject();
            postDataParams.put("categoria_id", params[1]);
            postDataParams.put("usuario_id", params[2]);
            postDataParams.put("item_selecateg", params[3]);
            postDataParams.put("estado", params[4]);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();

            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {

                BufferedReader in=new BufferedReader(new
                        InputStreamReader(
                        conn.getInputStream()));

                StringBuffer sb = new StringBuffer("");
                String line="";

                while((line = in.readLine()) != null) {
                    sb.append(line);
                    break;
                }

                in.close();

                AppCompatActivity aca = (AppCompatActivity) categorias.getContext();
                categorias cont = (categorias) aca;

                JSONObject jsonRpta = new JSONObject(sb.toString());
                Boolean ok = jsonRpta.getBoolean("status");
                RPTA_id = jsonRpta.getString("id_insertado");

                cont.setItem_selecateg(RPTA_id);

                return RPTA_id;

            }
            else {
                return new String("false : "+responseCode);
            }

        } catch(Exception e){
            return new String("Exception: " + e.getMessage());
        }
    }

    @Override
    protected void onPostExecute(String result) {
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
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