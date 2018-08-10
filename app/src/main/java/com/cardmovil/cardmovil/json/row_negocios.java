package com.cardmovil.cardmovil.json;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cardmovil.cardmovil.mapa;
import com.cardmovil.cardmovil.perfilNegocio;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.cardmovil.cardmovil.models.list_negocios;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.sa.tonisa.tonisa.R;
import com.cardmovil.cardmovil.negocios;

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

public class row_negocios extends AsyncTask<String, String, List<list_negocios>> {
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        AppCompatActivity aca = (AppCompatActivity) negocios.getContext();
        negocios cont = (negocios) aca;
//        cont.getDialog().show();
    }

    @Override
    protected List<list_negocios> doInBackground(String... params) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        int respuesta = 0;

        try {
            URL url = new URL(params[0]);
            JSONObject postDataParams = new JSONObject();
            postDataParams.put("categoriaid", params[1]);
            postDataParams.put("distritoid", params[2]);
            postDataParams.put("latitud", params[3]);
            postDataParams.put("longitud", params[4]);
            postDataParams.put("busqueda", params[5]);

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
                JSONArray parentArray = parentObject.getJSONArray("negocios");

                List<list_negocios> list_aModelList = new ArrayList<>();

                StringBuffer finalBufferData = new StringBuffer();
                Gson gson = new Gson();
                for (int i = 0; i < parentArray.length(); i++) {
                    JSONObject finalObject = parentArray.getJSONObject(i);
                    list_negocios l_negociosModel = gson.fromJson(finalObject.toString(), list_negocios.class);
                    list_aModelList.add(l_negociosModel);
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
    protected void onPostExecute(final List<list_negocios> result) {
        AppCompatActivity aca = (AppCompatActivity) negocios.getContext();
        final negocios cont = (negocios) aca;
        super.onPostExecute(result);
//        cont.getDialog().dismiss();
        if (result != null) {
            NegociosAdapter adapter = new NegociosAdapter(cont, R.layout.row_negocios, result);
            cont.lvNegocios.setAdapter(adapter);
            cont.lvNegocios.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    list_negocios l_negocioModel = result.get(position);
                    Intent intent = new Intent(cont, perfilNegocio.class);
                    intent.putExtra("l_perfilModel", new Gson().toJson(l_negocioModel));
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

    public class NegociosAdapter extends ArrayAdapter {
        AppCompatActivity aca = (AppCompatActivity) negocios.getContext();
        negocios cont = (negocios) aca;

        public List<list_negocios> list_aModelList;
        public int resource;
        private LayoutInflater inflater;

        public NegociosAdapter(Context context, int resource, List<list_negocios> objects) {
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
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(negocios.getContext())
                    .defaultDisplayImageOptions(defaultOptions)
                    .build();
            ImageLoader.getInstance().init(config); // Do it on Application start

            ViewHolder holder = null;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(resource, null);
                holder.ivLogoNegocio = (ImageView) convertView.findViewById(R.id.ivLogoNegocio);
                holder.tvNombreNegocio = (TextView) convertView.findViewById(R.id.tvNombreNegocio);
                holder.progressBarNegocio = (ProgressBar) convertView.findViewById(R.id.progressBarNegocio);
                holder.tvDescripcionNegocio = (TextView) convertView.findViewById(R.id.tvDescripcionNegocio);
                holder.tvDireccionNegocio = (TextView) convertView.findViewById(R.id.tvDireccionNegocio);
                holder.tvEstadoNegocio = (TextView) convertView.findViewById(R.id.tvEstadoNegocio);
                holder.tvDistancia = (TextView) convertView.findViewById(R.id.tvDistancia);
                holder.btnLlamarNegocio = (Button) convertView.findViewById(R.id.btnLlamarNegocio);
                holder.btnPromocion = (Button) convertView.findViewById(R.id.btnPromocion);
                holder.btnMapaNegocio = (Button) convertView.findViewById(R.id.btnMapaNegocio);
                holder.tvEstadoNegocio = (TextView) convertView.findViewById(R.id.tvEstadoNegocio);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvNombreNegocio.setText(list_aModelList.get(position).getCli_razonsocial());
            holder.tvDescripcionNegocio.setText(list_aModelList.get(position).getSuc_descripcion());
            holder.tvDireccionNegocio.setText(list_aModelList.get(position).getSuc_direccion());
            if(Integer.parseInt(list_aModelList.get(position).getDistancia()) < 1000)
                holder.tvDistancia.setText(list_aModelList.get(position).getDistancia() + "mts");
            else
                holder.tvDistancia.setText(Integer.parseInt(list_aModelList.get(position).getDistancia()) / 1000 + "Km");
            holder.btnLlamarNegocio.setTag(list_aModelList.get(position).getSuc_telefijo());
            holder.btnPromocion.setTag(position);
            holder.btnMapaNegocio.setTag(list_aModelList.get(position).getSuc_X()+"&"+list_aModelList.get(position).getSuc_Y()+"&"+list_aModelList.get(position).getCli_razonsocial()+"&"+list_aModelList.get(position).getSuc_direccion()+"&"+list_aModelList.get(position).getSuc_telefijo()+"&"+list_aModelList.get(position).getCli_logo());

            holder.btnPromocion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    list_negocios l_negocioModel = list_aModelList.get(position);
                    Intent intent = new Intent(cont, perfilNegocio.class);
                    intent.putExtra("l_perfilModel", new Gson().toJson(l_negocioModel));
                    cont.startActivity(intent);
                }
            });

            holder.btnMapaNegocio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] parts = v.getTag().toString().split("&");
                    Intent intent = new Intent(cont, mapa.class);
                    Bundle bolsa = new Bundle();
                    bolsa.putString("usuario", "0");
                    bolsa.putString("desde", "negocio");
                    bolsa.putString("id_categoria", "0");
                    bolsa.putString("latitud", parts[0]);
                    bolsa.putString("longitud", parts[1]);
                    bolsa.putString("razon_social", parts[2]);
                    bolsa.putString("direccion", parts[3]);
                    bolsa.putString("telefono", parts[4]);
                    bolsa.putString("logo", parts[4]);
                    intent.putExtras(bolsa);
                    cont.startActivity(intent);
                }
            });

            final String protocolo = "http://";
            String ip = cont.getResources().getString(R.string.ipweb);
            String puerto = cont.getResources().getString(R.string.puertoweb);
            puerto = puerto.equals("") ? "" : ":" + puerto;
            String url = protocolo + ip + puerto + "/";

            final ViewHolder finalHolder = holder;
            ImageLoader.getInstance().displayImage(url + list_aModelList.get(position).getCli_logo(), holder.ivLogoNegocio, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    finalHolder.progressBarNegocio.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    finalHolder.progressBarNegocio.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    finalHolder.progressBarNegocio.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    finalHolder.progressBarNegocio.setVisibility(View.GONE);
                }
            });

            return convertView;
        }

        class ViewHolder {
            private ImageView ivLogoNegocio;
            private TextView tvNombreNegocio;
            private ProgressBar progressBarNegocio;
            private TextView tvDescripcionNegocio;
            private TextView tvDireccionNegocio;
            private TextView tvEstadoNegocio;
            private TextView tvDistancia;
            private Button btnLlamarNegocio;
            private Button btnPromocion;
            private Button btnMapaNegocio;
        }
    }

}
