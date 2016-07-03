package jp.co.future.androidbase.adapter;

/**
 * Created by itaru on 2016/07/02.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vipul.hp_hp.timelineview.TimelineView;

import java.util.ArrayList;
import java.util.List;

import jp.co.future.androidbase.Orientation;
import jp.co.future.androidbase.R;
import jp.co.future.androidbase.model.InitialModel;
import jp.co.future.androidbase.model.TimeLineModel;

/**
 * Created by HP-HP on 05-12-2015.
 */
public class InitialAdapter extends BaseAdapter {

    Context context;
    LayoutInflater layoutInflater = null;
    ArrayList<InitialModel> initialList;

    public InitialAdapter(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setInitialList(ArrayList<InitialModel> initialList) {
        this.initialList = initialList;
    }

    @Override
    public int getCount() {
        return initialList.size();
    }

    @Override
    public Object getItem(int position) {
        return initialList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return initialList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.listitem_initial,parent,false);

        ((TextView)convertView.findViewById(R.id.list_name)).setText(initialList.get(position).getName());
        ((ImageView)convertView.findViewById(R.id.list_img)).setImageResource(initialList.get(position).getImg());

        return convertView;
    }
}
