package ta.widia.lapakkita.umkm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
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
import ta.widia.lapakkita.umkm.adapter.ProdukAdapter;
import ta.widia.lapakkita.umkm.model.ItemProduk;
import ta.widia.lapakkita.umkm.util.Config;
import ta.widia.lapakkita.umkm.util.Request;
import ta.widia.lapakkita.umkm.util.SessionManager;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rc;

    private String url = Config.HOST+"produk_umkm.php";

    private ProgressDialog pDialog;
    private ProdukAdapter adapter;
    private List<ItemProduk> items;
    private SessionManager session;
    SubMenu subMenu;
    private boolean log_in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("List Produk Anda");

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        //kalau belum login
        if(!session.isLoggedIn()){
            log_in = false;
            finish();
        }
        //kalau sudah login
        else{
            log_in = true;
            //ambil data user
            HashMap<String, String> user = session.getUserDetails();
            String id_umkm = user.get(SessionManager.KEY_ID_UMKM);
            String nm_pmlk = user.get(SessionManager.KEY_NM_PMLK);
            String nm_umkm = user.get(SessionManager.KEY_NM_UMKM);
            String noktp = user.get(SessionManager.KEY_NOKTP);
            String alamat = user.get(SessionManager.KEY_ALAMAT);
            String nohp = user.get(SessionManager.KEY_NOHP);
            String email = user.get(SessionManager.KEY_EMAIL);

            rc = (RecyclerView) findViewById(R.id.recycler_view);

            new getProduk(id_umkm).execute();

            items = new ArrayList<>();

            adapter = new ProdukAdapter(MainActivity.this, items, new ProdukAdapter.AdapterListener() {
                @Override
                public void onSelected(int position, String id_produk) {
                    Intent intent = new Intent(MainActivity.this, ProdukDetail.class);
                    intent.putExtra("key_id_pdk", id_produk);
                    startActivity(intent);
                }
            });

            LinearLayoutManager mLayoutManager = new LinearLayoutManager(MainActivity.this);
            rc.setLayoutManager(mLayoutManager);
            rc.setAdapter(adapter);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InputProduk.class);
                startActivity(intent);
            }
        });
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
            pDialog = new ProgressDialog(MainActivity.this);
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
                    String response = Request.post(url,dataToSend);

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

                            items.add(new ItemProduk(id, foto, nama, berat, harga));

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
            adapter.notifyDataSetChanged();
            pDialog.dismiss();

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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.act_logout) {
            session.logoutUser();
            /*Intent intent = new Intent(MainActivity.this, LoginUmkm.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);*/

            //terus tutup activity ini
            finish();
            return true;
        }
        if (id == R.id.act_biodata){
            Toast.makeText(this, "Masuk ke biodata UMKM", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
