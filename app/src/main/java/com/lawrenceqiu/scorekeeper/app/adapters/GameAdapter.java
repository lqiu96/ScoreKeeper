package com.lawrenceqiu.scorekeeper.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.lawrence.scorekeeper.app.R;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Lawrence
 * Date: 5/28/2015
 * Time: 12:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class GameAdapter extends BaseAdapter implements ListAdapter {
    private Context context;
    private ArrayList<String> fileNames;

    /**
     * Initiates the context and names of the files
     *
     * @param context   Conext
     * @param fileNames ArrayList of fileNames
     */
    public GameAdapter(Context context, ArrayList<String> fileNames) {
        this.context = context;
        this.fileNames = fileNames;
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return fileNames.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return fileNames.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.save_game_row, null);
        }

        TextView savedGame = (TextView) convertView.findViewById(R.id.savedGames);
        savedGame.setText(fileNames.get(position));

        return convertView;
    }

    /**
     * Notifies the adapter that things have changed
     */
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
