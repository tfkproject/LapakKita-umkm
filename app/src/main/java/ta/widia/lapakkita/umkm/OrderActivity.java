package ta.widia.lapakkita.umkm;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

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

public class OrderActivity extends AppCompatActivity {

    Button btnOrder;
    ImageView gambarProduk, tambah, kurang;
    TextView namaPrdk, hargaPrdk, namaUkm, nilai, txtTotalHarga;
    EditText edtCatatan;
    private ProgressDialog pDialog;
    private static final String TAG = OrderActivity.class.getSimpleName();
    public String SERVER_POST = Config.HOST+"tambah_ke_keranjang.php";
    int counter = 1;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        session = new SessionManager(getApplicationContext());

        getSupportActionBar().setTitle("Order Produk");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        gambarProduk = (ImageView) findViewById(R.id.gmbr_produk);
        namaPrdk = (TextView) findViewById(R.id.nama_produk);
        hargaPrdk = (TextView) findViewById(R.id.harga_barang);
        namaUkm = (TextView) findViewById(R.id.nama_ukm);
        edtCatatan = (EditText) findViewById(R.id.catatan);
        txtTotalHarga = (TextView) findViewById(R.id.txt_total_harga);

        tambah = (ImageView) findViewById(R.id.btnTambah);
        kurang = (ImageView) findViewById(R.id.btnKurang);
        nilai  = (TextView)findViewById(R.id.txtNilai);

        HashMap<String, String> user = session.getUserDetails();
        final String id_pelanggan = user.get(SessionManager.KEY_ID_UMKM);

        final String id_produk = getIntent().getStringExtra("key_id_produk");
        String link_foto = getIntent().getStringExtra("key_foto");
        String nama = getIntent().getStringExtra("key_nama");
        final String harga = getIntent().getStringExtra("key_harga");
        String ukm = getIntent().getStringExtra("key_ukm");

        Glide.with(OrderActivity.this).load(link_foto).into(gambarProduk);
        namaPrdk.setText(nama);
        hargaPrdk.setText("Rp. "+harga);
        namaUkm.setText(ukm);

        nilai.setText("" + counter);

        tambah.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
                counter++;
                nilai.setText("" + counter);
                int h_total = Integer.valueOf(harga) * counter;
                txtTotalHarga.setText("Rp. "+h_total);
            }
        });

        kurang.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
                counter--;
                if (counter < 0){
                    counter = 1;
                    nilai.setText("" + counter);
                    int h_total = Integer.valueOf(harga) * counter;
                    txtTotalHarga.setText("Rp. "+h_total);
                }
                else{
                    nilai.setText("" + counter);
                    int h_total = Integer.valueOf(harga) * counter;
                    txtTotalHarga.setText("Rp. "+h_total);
                }
            }
        });

        final int h_total = Integer.valueOf(harga) * counter;
        txtTotalHarga.setText("Rp. "+h_total);

        btnOrder = (Button) findViewById(R.id.btn_order);
        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //tambahkan ke keranjang
                String jumlah = String.valueOf(counter);
                String total = String.valueOf(h_total);
                String catatan = edtCatatan.getText().toString();
                new postData(id_produk, id_pelanggan, jumlah, total, catatan).execute();
                //Toast.makeText(OrderActivity.this, "Anda akan order", Toast.LENGTH_SHORT).show();
            }
        });

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

    private class postData extends AsyncTask<Void,Void,String> {
        private String id_produk;
        private String id_pelanggan;
        private String jumlah;
        private String harga_total;
        private String catatan;

        public postData(String id_produk, String id_pelanggan, String jumlah, String harga_total, String catatan){
            this.id_produk = id_produk;
            this.id_pelanggan = id_pelanggan;
            this.jumlah = jumlah;
            this.harga_total = harga_total;
            this.catatan = catatan;
        }

        private String scs = "";
        private String psn = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(OrderActivity.this);
            pDialog.setMessage("Loading..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                //menganbil data-data yang akan dikirim

                //generate hashMap to store encodedImage and the name
                HashMap<String,String> detail = new HashMap<>();
                detail.put("id_produk", id_produk);
                detail.put("id_pelanggan", id_pelanggan);
                detail.put("jumlah", jumlah);
                detail.put("harga", harga_total);
                detail.put("catatan", catatan);

                try{
                    //convert this HashMap to encodedUrl to send to php file
                    String dataToSend = hashMapToUrl(detail);
                    //make a Http request and send data to php file
                    String response = Request.post(SERVER_POST,dataToSend);

                    //dapatkan respon
                    Log.e("Respon", response);

                    JSONObject ob = new JSONObject(response);
                    scs = ob.getString("success");
                    psn = ob.getString("message");

                }catch (JSONException e){
                    e.printStackTrace();
                    Log.e(TAG, "ERROR  " + e);
                    Toast.makeText(getApplicationContext(),"Maaf, terjadi error!",Toast.LENGTH_SHORT).show();
                    //return null;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }



        @Override
        protected void onPostExecute(String s) {
            pDialog.dismiss();

            if(scs.contains("1")){
                dialogBox(psn);
            }
            if(scs.contains("0")){
                Toast.makeText(getApplicationContext(), psn,Toast.LENGTH_SHORT).show();
            }

        }

    }

    private void dialogBox(String pesan){
        //ini munculkan dialog box keranjang belanja
        AlertDialog.Builder builder = new AlertDialog.Builder(OrderActivity.this);
        builder.setTitle("Lihat keranjang anda?");
        builder.setMessage(pesan);
        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //masuk ke keranjang belanja
                /*Intent intent = new Intent(OrderActivity.this, KeranjangActivity.class);
                startActivity(intent);*/
            }
        });

        builder.setNegativeButton("Nanti", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.setCanceledOnTouchOutside(false);
        alert.show();
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

}
