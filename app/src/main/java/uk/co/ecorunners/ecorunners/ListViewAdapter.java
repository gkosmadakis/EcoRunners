package uk.co.ecorunners.ecorunners;

import static uk.co.ecorunners.ecorunners.Constants.FIRST_COLUMN;
import static uk.co.ecorunners.ecorunners.Constants.FOURTH_COLUMN;
import static uk.co.ecorunners.ecorunners.Constants.SECOND_COLUMN;
import static uk.co.ecorunners.ecorunners.Constants.THIRD_COLUMN;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * Created by cousm on 10/08/2017.
 */

public class ListViewAdapter extends ArrayAdapter<RowItem> {

    public ArrayList<HashMap<String, String>> list;
    TextView txtFirst;
    TextView txtSecond;
    TextView txtThird;
    ImageView txtFourth;
    Context context;

    public ListViewAdapter(Context context, ArrayList<HashMap<String, String>> list, int resourceId, List<RowItem> items){

        super(context, resourceId, items);

        this.list=list;

        this.context=context;
    }

    /*private view holder class*/
    private class ViewHolder {

        ImageView imageView;

    }

    @Override
    public int getCount() {

        return list.size();
    }


    @Override
    public long getItemId(int position) {

        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        RowItem rowItem = null;

        if (position < 1 ) {

            rowItem = getItem(position);
        }

        LayoutInflater mInflater=(LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if(convertView == null){

            convertView = mInflater.inflate(R.layout.colmn_row, null);

            txtFirst=(TextView) convertView.findViewById(R.id.day);

            txtSecond=(TextView) convertView.findViewById(R.id.place);

            txtThird=(TextView) convertView.findViewById(R.id.time);

            txtFourth=(ImageView)convertView.findViewById(R.id.statusIcon);

            holder = new ViewHolder();

            holder.imageView = (ImageView) convertView.findViewById(R.id.statusIcon);

            convertView.setTag(holder);

        }

        else {

            holder = (ViewHolder) convertView.getTag();

            holder.imageView.setImageResource(rowItem.getImageId());
        }

        HashMap<String, String> map=list.get(position);

        txtFirst.setText(map.get(FIRST_COLUMN));

        txtSecond.setText(map.get(SECOND_COLUMN));

        txtThird.setText(map.get(THIRD_COLUMN));

        if (map.get(FOURTH_COLUMN) != null) {

            txtFourth.setImageResource(Integer.parseInt(map.get(FOURTH_COLUMN)));
        }

        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {

        // this is to implement 1.2A till 1.2C user should not be able to click on a schedule ahead from the current week
        boolean clearScheduleIsCalled = ListCalendar.scheduleIsCleared;

        if (clearScheduleIsCalled) {

            return false;
        }

        else {

            return true;
        }
    }
}
