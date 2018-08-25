package ta.widia.lapakkita.umkm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import ta.widia.lapakkita.R;
import ta.widia.lapakkita.umkm.util.Config;
import ta.widia.lapakkita.umkm.util.Request;
import ta.widia.lapakkita.umkm.util.SessionManager;

public class ProdukDetail extends AppCompatActivity {

    ImageView img_logo;
    TextView txtKategori, txtNama, txtHarga, txtStock, txtBerat, txtDesk;
    Button btnHapus, btnEdit;
    ImageView imgProduk;
    private ProgressDialog pDialog;
    private String url = Config.HOST+"produk_detail.php";

    SessionManager session;

    String kat_prod, nama, harga, stok, gambar1, gambar2, gambar3, gambar4, berat, desk, umkm, id_umkm, logo, hp, lat, lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.produk_detail);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        final String id_produk = getIntent().getStringExtra("key_id_pdk");

        getSupportActionBar().setTitle("Detail Produk");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        new getData(id_produk).execute();
        imgProduk = (ImageView) findViewById(R.id.img_produk);

        txtKategori = (TextView) findViewById(R.id.txt_kategori);
        txtNama = (TextView) findViewById(R.id.txt_nama);
        txtHarga = (TextView) findViewById(R.id.txt_harga);
        txtStock = (TextView) findViewById(R.id.txt_stock);
        txtBerat = (TextView) findViewById(R.id.txt_berat);
        //txtView = (TextView) findViewById(R.id.txt_view);
        txtDesk = (TextView) findViewById(R.id.txt_desk);

        btnHapus = (Button) findViewById(R.id.btn_hapus);
        btnEdit = (Button) findViewById(R.id.btn_edit);

        btnHapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ProdukDetail.this, "Hapus nantinya", Toast.LENGTH_SHORT).show();
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //kalau belum login
                Intent intent = new Intent(ProdukDetail.this, EditProduk.class);
                intent.putExtra("key_id_produk", id_produk);
                intent.putExtra("key_nama", nama);
                intent.putExtra("key_harga", harga);
                intent.putExtra("key_stok", stok);
                intent.putExtra("key_berat", berat);
                intent.putExtra("key_desk", desk);
                intent.putExtra("key_gambar", gambar1);
                startActivity(intent);
            }
        });
    }

    private class getData extends AsyncTask<Void,Void,String> {

        //variabel untuk tangkap data
        private int scs = 0;
        String id;

        public getData(String id){
            this.id = id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ProdukDetail.this);
            pDialog.setMessage("Memuat data...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(Void... params) {

            try{
                //susun parameter
                HashMap<String,String> detail = new HashMap<>();

                try {
                    //convert this HashMap to encodedUrl to send to php file
                    String dataToSend = hashMapToUrl(detail);
                    //make a Http request and send data to php file
                    String response = Request.post(url+"?id_produk="+id,dataToSend);

                    //dapatkan respon
                    Log.e("Respon", response);

                    JSONObject ob = new JSONObject(response);
                    scs = ob.getInt("success");

                    if (scs == 1) {
                        JSONArray products = ob.getJSONArray("field");

                        for (int i = 0; i < products.length(); i++) {
                            JSONObject c = products.getJSONObject(i);

                            // Storing each json item in variable
                            kat_prod = c.getString("kategori_produk");
                            nama = c.getString("nama_produk");
                            harga = c.getString("harga_produk");
                            stok = c.getString("stok_produk");
                            gambar1 = c.getString("gambar_produk1");
                            gambar2 = c.getString("gambar_produk2");
                            gambar3 = c.getString("gambar_produk3");
                            gambar4 = c.getString("gambar_produk4");

                            umkm = c.getString("nama_umkm");
                            berat = c.getString("berat_produk");
                            //view = c.getString("kunjungan_produk");
                            desk = c.getString("deskripsi");

                            id_umkm = c.getString("id_umkm");
                            logo = c.getString("logo");
                            hp = c.getString("hp");
                            lat = c.getString("lat");
                            lon = c.getString("lon");

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

            pDialog.dismiss();

            Glide.with(ProdukDetail.this).load(gambar1).into(imgProduk);
            txtKategori.setText("Kategori: "+kat_prod);
            txtNama.setText(nama);
            txtHarga.setText("Rp. "+harga);
            txtStock.setText("Stock: "+stok);
            txtBerat.setText("Berat: "+berat+" gram");
            txtDesk.setText(Html.fromHtml(desk));

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
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

        if(id == R.id.action_refresh){
            finish();
            startActivity(getIntent());
        }

        return super.onOptionsItemSelected(item);
    }


}
