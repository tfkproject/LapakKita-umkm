package ta.widia.lapakkita.umkm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ta.widia.lapakkita.R;
import ta.widia.lapakkita.umkm.adapter.ProdukAdapter;
import ta.widia.lapakkita.umkm.model.ItemProduk;
import ta.widia.lapakkita.umkm.util.Config;
import ta.widia.lapakkita.umkm.util.Request;

public class UkmDetail extends AppCompatActivity {

    ImageView logo;
    TextView txtNama, txtAlamat;
    Button btnTelp, btnDirek;
    private ProgressDialog pDialog;

    private RecyclerView rc;
    private List<ItemProduk> itemPrdkList;
    private ProdukAdapter prdkAdapter;
    private String url_produk = Config.HOST+"produk_umkm.php";
    private String url_post_view = Config.HOST+"update_view.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.umkm_detail);

        String id_umkm = getIntent().getStringExtra("key_id");
        final String nama = getIntent().getStringExtra("key_nama");
        String alamat = getIntent().getStringExtra("key_alamat");
        String gambar = getIntent().getStringExtra("key_gambar");
        final String hp = getIntent().getStringExtra("key_hp");
        final String lat = getIntent().getStringExtra("key_lat");
        final String lon = getIntent().getStringExtra("key_lon");

        getSupportActionBar().setTitle(nama);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        logo = (ImageView) findViewById(R.id.img_logo);
        txtNama = (TextView) findViewById(R.id.txt_nama);
        txtAlamat = (TextView) findViewById(R.id.txt_alamat);

        btnTelp = (Button) findViewById(R.id.btn_telp);
        btnDirek = (Button) findViewById(R.id.btn_direk);

        rc = (RecyclerView) findViewById(R.id.recycler_view);

        Glide.with(UkmDetail.this).load(gambar).into(logo);
        txtNama.setText(nama);
        txtAlamat.setText(alamat);
        btnTelp.setText(hp);

        btnTelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+hp));
                startActivity(intent);
            }
        });

        btnDirek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent(UkmDetail.this, DirectionMap.class);
                intent.putExtra("key_nama_tujuan", nama);
                intent.putExtra("key_lat_tujuan", lat);
                intent.putExtra("key_long_tujuan", lon);
                startActivity(intent);*/
            }
        });

        new getProduk(id_umkm).execute();

        itemPrdkList = new ArrayList<>();

        prdkAdapter = new ProdukAdapter(UkmDetail.this, itemPrdkList, new ProdukAdapter.AdapterListener() {
            @Override
            public void onSelected(int position, String id_produk) {
                Intent intent = new Intent(UkmDetail.this, ProdukDetail.class);
                intent.putExtra("key_id_pdk", id_produk);
                startActivity(intent);

                new updateView(id_produk).execute();
            }
        });
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(UkmDetail.this, 2);
        rc.setLayoutManager(mLayoutManager);
        rc.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        rc.setItemAnimator(new DefaultItemAnimator());
        rc.setAdapter(prdkAdapter);
    }

    private class getProduk extends AsyncTask<Void,Void,String> {

        //variabel untuk tangkap data
        private int scs = 0;
        private String id_umkm;

        public getProduk(String id_umkm){
            this.id_umkm = id_umkm;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(UkmDetail.this);
            pDialog.setMessage("Memuat data...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(Void... params) {

            try{
                //susun parameter
                HashMap<String,String> detail = new HashMap<>();
                detail.put("id_umkm", id_umkm);

                try {
                    //convert this HashMap to encodedUrl to send to php file
                    String dataToSend = hashMapToUrl(detail);
                    //make a Http request and send data to php file
                    String response = Request.post(url_produk,dataToSend);

                    //dapatkan respon
                    Log.e("Respon", response);

                    JSONObject ob = new JSONObject(response);
                    scs = ob.getInt("success");

                    if (scs == 1) {
                        JSONArray products = ob.getJSONArray("field");

                        for (int i = 0; i < products.length(); i++) {
                            JSONObject c = products.getJSONObject(i);

                            // Storing each json item in variable
                            String id = c.getString("id_produk");
                            String foto = c.getString("gambar");
                            String nama = c.getString("nama_produk");
                            String berat = c.getString("berat");
                            String harga = c.getString("harga");

                            itemPrdkList.add(new ItemProduk(id, foto, nama, berat, harga));

                        }
                    } else {
                        // no data found

                    }

                } catch (JSONException e){
                    e.printStackTrace();
                }

            } catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            prdkAdapter.notifyDataSetChanged();
            pDialog.dismiss();

        }

    }

    private class updateView extends AsyncTask<Void,Void,String> {

        //variabel untuk tangkap data
        private int scs = 0;
        private String id_produk, psn;

        public updateView(String id_produk){
            this.id_produk = id_produk;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*pDialog = new ProgressDialog(UkmDetail.this);
            pDialog.setMessage("Memuat data...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();*/
        }

        protected String doInBackground(Void... params) {

            try{
                //susun parameter
                HashMap<String,String> detail = new HashMap<>();
                detail.put("id_produk", id_produk);

                try {
                    //convert this HashMap to encodedUrl to send to php file
                    String dataToSend = hashMapToUrl(detail);
                    //make a Http request and send data to php file
                    String response = Request.post(url_post_view,dataToSend);

                    //dapatkan respon
                    Log.e("Respon", response);

                    JSONObject ob = new JSONObject(response);
                    scs = ob.getInt("success");

                    if (scs == 1) {
                        psn = ob.getString("message");
                    } else {
                        // fail
                        psn = ob.getString("message");
                    }

                } catch (JSONException e){
                    e.printStackTrace();
                }

            } catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            //..

        }

    }

    private String hashMapToUrl(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        /*else if (id == R.id.action_keluar){
            finish();
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

}
