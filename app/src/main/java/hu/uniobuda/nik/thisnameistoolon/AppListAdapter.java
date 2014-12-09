package hu.uniobuda.nik.thisnameistoolon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class AppListAdapter extends ArrayAdapter<String> {

    private ArrayList<String> appList;
    private ArrayList<String> packList;
    private ArrayList<CheckBox> checkList;
    private ArrayList<String> selectedAppList;

    public AppListAdapter(Context context, int textViewResourceId,
                          ArrayList<String> appName, ArrayList<CheckBox> checkName, ArrayList<String> packName) {
        super(context, textViewResourceId, appName);
        this.appList = new ArrayList<String>();
        this.appList.addAll(appName);
        this.checkList = new ArrayList<CheckBox>();
        this.checkList.addAll(checkName);
        selectedAppList = new ArrayList<String>();
        this.packList = new ArrayList<String>();
        this.packList.addAll(packName);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.app_list, null);
            holder = new ViewHolder();
            holder.label = (TextView) convertView.findViewById(R.id.AppLabel);
            holder.name = (CheckBox) convertView.findViewById(R.id.CheckBox);
            convertView.setTag(holder);
            holder.name.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    if (cb.isChecked()) {
                        Toast.makeText(getContext().getApplicationContext(),
                                cb.getText() +
                                        " is " + cb.isChecked(),
                                Toast.LENGTH_LONG).show();
                        cb.setSelected(cb.isChecked());
                        selectedAppList.add(packList.get(position));
                    } else {
                        Toast.makeText(getContext().getApplicationContext(),
                                cb.getText() +
                                        " is " + cb.isChecked(),
                                Toast.LENGTH_LONG).show();
                        cb.setSelected(cb.isChecked());
                        selectedAppList.remove(packList.get(position));
                    }
                }
            });

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String app = appList.get(position);
        CheckBox cb = checkList.get(position);
        holder.name.setText(app);
        holder.name.setVisibility(View.VISIBLE);
        holder.name.setChecked(cb.isChecked());
        return convertView;
    }

    ArrayList<String> getSelectedApps() {
        return selectedAppList;
    }

    private class ViewHolder {
        TextView label;
        CheckBox name;
    }
}