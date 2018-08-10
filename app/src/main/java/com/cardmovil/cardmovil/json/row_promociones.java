package com.cardmovil.cardmovil.json;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
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

import com.cardmovil.cardmovil.models.list_negocios;
import com.cardmovil.cardmovil.models.list_promociones;
import com.cardmovil.cardmovil.negocios;
import com.cardmovil.cardmovil.perfilNegocio;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class row_promociones extends AsyncTask<String, String, List<list_promociones>> {
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        AppCompatActivity aca = (AppCompatActivity) perfilNegocio.getContext();
        perfilNegocio cont = (perfilNegocio) aca;
//        cont.getDialog().show();
    }

    @Override
    protected List<list_promociones> doInBackground(String... params) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        int respuesta = 0;

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
                JSONArray parentArray = parentObject.getJSONArray("promociones");

                List<list_promociones> list_pModelList = new ArrayList<>();

                StringBuffer finalBufferData = new StringBuffer();
                Gson gson = new Gson();
                for (int i = 0; i < parentArray.length(); i++) {
                    JSONObject finalObject = parentArray.getJSONObject(i);
                    list_promociones l_promocionesModel = gson.fromJson(finalObject.toString(), list_promociones.class);
                    list_pModelList.add(l_promocionesModel);
                }
                return list_pModelList;
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
    protected void onPostExecute(final List<list_promociones> result) {
        AppCompatActivity aca = (AppCompatActivity) perfilNegocio.getContext();
        final perfilNegocio cont = (perfilNegocio) aca;
        super.onPostExecute(result);
//        cont.getDialog().dismiss();
        if (result != null) {
            PromocionesAdapter adapter = new PromocionesAdapter(cont, R.layout.row_promocion, result);
            cont.lvPromociones.setAdapter(adapter);
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

    public class PromocionesAdapter extends ArrayAdapter {
        AppCompatActivity aca = (AppCompatActivity) perfilNegocio.getContext();
        perfilNegocio cont = (perfilNegocio) aca;

        public List<list_promociones> list_pModelList;
        public int resource;
        private LayoutInflater inflater;

        public PromocionesAdapter(Context context, int resource, List<list_promociones> objects) {
            super(context, resource, objects);
            list_pModelList = objects;
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
                holder.ivPromocion = (ImageView) convertView.findViewById(R.id.ivPromocion);
                holder.progressBarPromocion = (ProgressBar) convertView.findViewById(R.id.progressBarPromocion);
                holder.tvTituloPromocion = (TextView) convertView.findViewById(R.id.tvTituloPromocion);
                holder.tvDescripcionPromocion = (TextView) convertView.findViewById(R.id.tvDescripcionPromocion);
                holder.tvPrecioPromocion = (TextView) convertView.findViewById(R.id.tvPrecioPromocion);
                holder.tvFechaFinPromocion = (TextView) convertView.findViewById(R.id.tvFechaFinPromocion);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvTituloPromocion.setText("Promoción: " + list_pModelList.get(position).getProm_Codigo());
            holder.tvDescripcionPromocion.setText(list_pModelList.get(position).getProm_desc());
            holder.tvPrecioPromocion.setText("S/. " + list_pModelList.get(position).getProm_Precio());
            holder.tvFechaFinPromocion.setText("Fecha Finalización: " + list_pModelList.get(position).getProm_FFin());

            final String protocolo = "http://";
            String ip = cont.getResources().getString(R.string.ipweb);
            String puerto = cont.getResources().getString(R.string.puertoweb);
            puerto = puerto.equals("") ? "" : ":" + puerto;
            String url = protocolo + ip + puerto + "/";

            holder.ivPromocion.setTag(url + list_pModelList.get(position).getProm_foto());

            final ViewHolder finalHolder = holder;
            ImageLoader.getInstance().displayImage(url + list_pModelList.get(position).getProm_foto(), holder.ivPromocion, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    finalHolder.progressBarPromocion.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    finalHolder.progressBarPromocion.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    finalHolder.progressBarPromocion.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    finalHolder.progressBarPromocion.setVisibility(View.GONE);
                }
            });

            return convertView;
        }

        class ViewHolder {
            private ImageView ivPromocion;
            private TextView tvTituloPromocion;
            private TextView tvDescripcionPromocion;
            private TextView tvPrecioPromocion;
            private TextView tvFechaFinPromocion;
            private ProgressBar progressBarPromocion;
        }
    }

}
