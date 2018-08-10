package com.cardmovil.cardmovil.json;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.cardmovil.cardmovil.categorias;
import com.cardmovil.cardmovil.datos_tarjeta;
import com.cardmovil.cardmovil.models.list_tarjetas;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
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

public class row_tarjetas extends AsyncTask<String, String, List<list_tarjetas>> {
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        AppCompatActivity aca = (AppCompatActivity) categorias.getContext();
        categorias cont = (categorias) aca;
        cont.getDialog().show();
    }

    @Override
    protected List<list_tarjetas> doInBackground(String... params) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        int respuesta = 0;
        StringBuilder result = null;

        try {
            URL url = new URL(params[0]);
            JSONObject postDataParams = new JSONObject();
            postDataParams.put("userid", params[1]);

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
                JSONArray parentArray = parentObject.getJSONArray("tarjetas");

                List<list_tarjetas> list_aModelList = new ArrayList<>();

                StringBuffer finalBufferData = new StringBuffer();
                Gson gson = new Gson();
                for (int i = 0; i < parentArray.length(); i++) {
                    JSONObject finalObject = parentArray.getJSONObject(i);
                    list_tarjetas l_tarjetasModel = gson.fromJson(finalObject.toString(), list_tarjetas.class);
                    list_aModelList.add(l_tarjetasModel);
                }
                return list_aModelList;
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
    protected void onPostExecute(final List<list_tarjetas> result) {
        AppCompatActivity aca = (AppCompatActivity) categorias.getContext();
        final categorias cont = (categorias) aca;
        super.onPostExecute(result);
        cont.getDialog().dismiss();
        if (result != null) {
            CategoriaAdapter adapter = new CategoriaAdapter(cont, R.layout.row_tarjetas, result);
            cont.lvTarjetas.setAdapter(adapter);
            cont.lvTarjetas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    list_tarjetas l_tarjetasModel = result.get(position);
                    Intent intent = new Intent(cont, datos_tarjeta.class);
                    intent.putExtra("l_tarjetasModel", new Gson().toJson(l_tarjetasModel));
                    intent.putExtra("usuario", cont.usuario_id);
                    intent.putExtra("pais", cont.pais_id);
                    intent.putExtra("distrito", cont.distrito_id);
                    cont.startActivity(intent);
                }
            });
        } else {
            Toast.makeText(cont, "Sin conección a internet", Toast.LENGTH_SHORT).show();
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

    public class CategoriaAdapter extends ArrayAdapter {
        AppCompatActivity aca = (AppCompatActivity) categorias.getContext();
        categorias cont = (categorias) aca;

        public List<list_tarjetas> list_aModelList;
        public int resource;
        private LayoutInflater inflater;

        public CategoriaAdapter(Context context, int resource, List<list_tarjetas> objects) {
            super(context, resource, objects);
            list_aModelList = objects;
            this.resource = resource;
            inflater = cont.inflater;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .build();
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(categorias.getContext())
                    .defaultDisplayImageOptions(defaultOptions)
                    .build();
            ImageLoader.getInstance().init(config); // Do it on Application start

            ViewHolder holder = null;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(resource, null);
                holder.tvNombreTarjeta = (TextView) convertView.findViewById(R.id.tvNombreTarjeta);
                holder.tvNumTarjeta = (TextView) convertView.findViewById(R.id.tvNumTarjeta);
//                holder.tvSaldoTarjeta = (TextView) convertView.findViewById(R.id.tvSaldoTarjeta);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvNombreTarjeta.setText(list_aModelList.get(position).getTar_Desc());
            holder.tvNumTarjeta.setText("XXXX - XXXX - XXXX - " + list_aModelList.get(position).getTar_NumTarjeta().substring(list_aModelList.get(position).getTar_NumTarjeta().length() - 4, list_aModelList.get(position).getTar_NumTarjeta().length()));
//            holder.tvSaldoTarjeta.setText("S/. " + list_aModelList.get(position).getTar_saldo());

            return convertView;
        }

        class ViewHolder {
            private TextView tvNombreTarjeta;
            private TextView tvNumTarjeta;
//            private TextView tvSaldoTarjeta;
        }
    }

}
