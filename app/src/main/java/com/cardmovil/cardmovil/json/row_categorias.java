package com.cardmovil.cardmovil.json;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import com.cardmovil.cardmovil.negocios;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.cardmovil.cardmovil.categorias;
import com.cardmovil.cardmovil.models.list_categorias;
import com.sa.tonisa.tonisa.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
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

public class row_categorias extends AsyncTask<String, String, List<list_categorias>> {
    public String pais, distrito;
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        AppCompatActivity aca = (AppCompatActivity) categorias.getContext();
        categorias cont = (categorias) aca;
//        cont.getDialog().show();
    }

    @Override
    protected List<list_categorias> doInBackground(String... params) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        int respuesta = 0;
        StringBuilder result = null;

        pais = params[2];
        distrito = params[3];

        try {
            URL url = new URL(params[0]);
            JSONObject postDataParams = new JSONObject();
            postDataParams.put("userid", params[1]);
            postDataParams.put("ubigeo", params[2]);
            postDataParams.put("latitud", params[4]);
            postDataParams.put("longitud", params[5]);
            postDataParams.put("busqueda", params[6]);

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
                JSONArray parentArray = parentObject.getJSONArray("categorias");

                List<list_categorias> list_aModelList = new ArrayList<>();

                StringBuffer finalBufferData = new StringBuffer();
                Gson gson = new Gson();
                for (int i = 0; i < parentArray.length(); i++) {
                    JSONObject finalObject = parentArray.getJSONObject(i);
                    list_categorias l_categoriasModel = gson.fromJson(finalObject.toString(), list_categorias.class);
                    list_aModelList.add(l_categoriasModel);
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
    protected void onPostExecute(final List<list_categorias> result) {
        AppCompatActivity aca = (AppCompatActivity) categorias.getContext();
        final categorias cont = (categorias) aca;
        super.onPostExecute(result);
//        cont.getDialog().dismiss();
        if (result != null) {
            CategoriaAdapter adapter = new CategoriaAdapter(cont, R.layout.row_categorias, result);
            cont.lvCategorias.setAdapter(adapter);
            cont.lvCategorias.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    list_categorias l_categoriasModel = result.get(position);
                    Intent intent = new Intent(cont, negocios.class);
                    intent.putExtra("l_categoriaModel", new Gson().toJson(l_categoriasModel));
                    intent.putExtra("pais", pais);
                    intent.putExtra("distrito", distrito);
                    intent.putExtra("latitud", cont.latitud);
                    intent.putExtra("longitid", cont.longitud);
                    intent.putExtra("usuario", cont.usuario_id);
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

        public List<list_categorias> list_aModelList;
        public int resource;
        private LayoutInflater inflater;
        public String item_select, estado;

        public CategoriaAdapter(Context context, int resource, List<list_categorias> objects) {
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
                holder.ivCategoria = (ImageView) convertView.findViewById(R.id.ivCategoria);
                holder.tvCategoria = (TextView) convertView.findViewById(R.id.tvCategoria);
                holder.progressBarCategoria = (ProgressBar) convertView.findViewById(R.id.progressBarCategoria);
                holder.btnPreferencia = (Button) convertView.findViewById(R.id.btnPreferencia);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            item_select = list_aModelList.get(position).getItem_Selecateg();
            item_select = (item_select == null) ? "0" : item_select;
            estado = list_aModelList.get(position).getSC_Estado();
            estado = (estado == null) ? "0" : estado;

            holder.tvCategoria.setText(list_aModelList.get(position).getCat_Desc());
            holder.btnPreferencia.setTag(list_aModelList.get(position).getCat_Codigo()+","+item_select+","+estado);

            if(list_aModelList.get(position).getSC_Estado() != null) {
                if (list_aModelList.get(position).getSC_Estado().equals("1")) {
                    cont.num_preferencias++;
                    holder.btnPreferencia.setBackgroundResource(R.drawable.ic_star_seleccionado);
                }else{
                    holder.btnPreferencia.setBackgroundResource(R.drawable.ic_star);
                }
            }


            final String protocolo = "http://";
            String ip = cont.getResources().getString(R.string.ipweb);
            String puerto = cont.getResources().getString(R.string.puertoweb);
            puerto = puerto.equals("") ? "" : ":" + puerto;
            String url = protocolo + ip + puerto + "/";

            final ViewHolder finalHolder = holder;
            ImageLoader.getInstance().displayImage(url + list_aModelList.get(position).getCat_Imagen(), holder.ivCategoria, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    finalHolder.progressBarCategoria.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    finalHolder.progressBarCategoria.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    finalHolder.progressBarCategoria.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    finalHolder.progressBarCategoria.setVisibility(View.GONE);
                }
            });

            return convertView;
        }

        class ViewHolder {
            private ImageView ivCategoria;
            private TextView tvCategoria;
            private ProgressBar progressBarCategoria;
            private Button btnPreferencia;
        }
    }

}
