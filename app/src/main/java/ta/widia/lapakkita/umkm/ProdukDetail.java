package ta.widia.lapakkita.umkm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import ta.widia.lapakkita.umkm.adapter.ProdukDetailImageAdapter;
import ta.widia.lapakkita.umkm.model.ItemProdukDetailImage;
import ta.widia.lapakkita.umkm.util.Config;
import ta.widia.lapakkita.umkm.util.Request;
import ta.widia.lapakkita.umkm.util.SessionManager;

public class ProdukDetail extends AppCompatActivity {

    ImageView img_logo;
    TextView txtNama, txtHarga, txtStock, txtBerat, txtView, txtDesk;
    Button btnBeli, btnDiskusi;
    private ProgressDialog pDialog;
    private String url = Config.HOST+"produk_detail.php";
    private List<ItemProdukDetailImage> imageItem;
    ViewPager viewPagerImage;
    ProdukDetailImageAdapter imageAdapter;
    SessionManager session;
    String foto_thumb;
    String nama, harga, stok, gambar1, gambar2, gambar3, gambar4, berat, desk, umkm, id_umkm, logo, hp, lat, lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.produk_detail);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        final String id_produk = getIntent().getStringExtra("key_id_pdk");

        getSupportActionBar().setTitle("Detail Produk");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPagerImage = (ViewPager) findViewById(R.id.viewPagerImage);

        imageItem = new ArrayList<ItemProdukDetailImage>();
        imageItem.add(new ItemProdukDetailImage(gambar1, "1"));
        imageItem.add(new ItemProdukDetailImage(gambar2, "2"));
        imageItem.add(new ItemProdukDetailImage(gambar3, "3"));
        imageItem.add(new ItemProdukDetailImage(gambar4, "4"));

        new getData(id_produk).execute();


        imageAdapter = new ProdukDetailImageAdapter(ProdukDetail.this, imageItem);
        viewPagerImage.setAdapter(imageAdapter);




        txtNama = (TextView) findViewById(R.id.txt_nama);
        txtHarga = (TextView) findViewById(R.id.txt_harga);
        txtStock = (TextView) findViewById(R.id.txt_stock);
        txtBerat = (TextView) findViewById(R.id.txt_berat);
        //txtView = (TextView) findViewById(R.id.txt_view);
        txtDesk = (TextView) findViewById(R.id.txt_desk);

        btnBeli = (Button) findViewById(R.id.btn_beli);
        btnDiskusi = (Button) findViewById(R.id.btn_diskusi);

        btnBeli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //kalau belum login
                if(!session.isLoggedIn()){
                    Toast.makeText(ProdukDetail.this, "Anda harus login untuk dapat melakukan transaksi!", Toast.LENGTH_SHORT).show();
                }
                //kalau sudah login
                else{
                    Toast.makeText(ProdukDetail.this, "Hapus nantinya", Toast.LENGTH_SHORT).show();
                    /*HashMap<String, String> user = session.getUserDetails();

                    String id_pelanggan = user.get(SessionManager.KEY_ID_UMKM);
                    //lakukan pembelian
                    //Toast.makeText(ProdukDetail.this, "Anda akan beli produk ini", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProdukDetail.this, OrderActivity.class);
                    intent.putExtra("key_id_produk", id_produk);
                    intent.putExtra("key_foto", gambar1);
                    intent.putExtra("key_nama", nama);
                    intent.putExtra("key_harga", harga);
                    intent.putExtra("key_ukm", ukm);
                    startActivity(intent);*/
                }
            }
        });

        btnDiskusi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //kalau belum login
                if(!session.isLoggedIn()){
                    Toast.makeText(ProdukDetail.this, "Anda harus login untuk dapat melakukan diskusi!", Toast.LENGTH_SHORT).show();
                }
                //kalau sudah login
                else{

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
                        imageItem.clear();
                        for (int i = 0; i < products.length(); i++) {
                            JSONObject c = products.getJSONObject(i);

                            // Storing each json item in variable
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

                            imageItem.add(new ItemProdukDetailImage(gambar1, "1"));
                            imageItem.add(new ItemProdukDetailImage(gambar2, "2"));
                            imageItem.add(new ItemProdukDetailImage(gambar3, "3"));
                            imageItem.add(new ItemProdukDetailImage(gambar4, "4"));

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

            imageAdapter.notifyDataSetChanged();
            pDialog.dismiss();

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

        return super.onOptionsItemSelected(item);
    }


}
