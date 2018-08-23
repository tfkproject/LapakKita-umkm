package ta.widia.lapakkita.umkm.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import ta.widia.lapakkita.R;
import ta.widia.lapakkita.umkm.model.ItemProdukDetailImage;

/**
 * Created by taufik on 21/05/18.
 */

public class ProdukDetailImageAdapter extends PagerAdapter{
    private Context context;
    private LayoutInflater layoutInflater;
    private List<ItemProdukDetailImage> itemList;

    public ProdukDetailImageAdapter(Context context, List<ItemProdukDetailImage> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.produk_detail_image_layout, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        Glide.with(context).load(itemList.get(position).getUrl_gambar()).into(imageView);
        TextView caption = (TextView) view.findViewById(R.id.txtCaption);
        caption.setText("Gambar "+itemList.get(position).getGambar_ke());

        ViewPager vp = (ViewPager) container;
        vp.addView(view, 0);
        return view;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        ViewPager vp = (ViewPager) container;
        View view = (View) object;
        vp.removeView(view);

    }
}
