package ca.taglab.vocabnomad.widgets;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ca.taglab.vocabnomad.R;

public abstract class CardListFragment extends Fragment {

    /**
     * The title of the list.
     */
    public static final String TITLE = "title";
    protected String mTitle;

    /**
     * The image resource representing the list.
     */
    public static final String IMAGE = "image";
    private int mImageId;

    /**
     * The limit to the number of items in the list.
     */
    public static final String LIMIT = "limit";
    private int mLimit;

    private ViewGroup mList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getString(TITLE);
            mImageId = getArguments().getInt(IMAGE, 0);
            mLimit = getArguments().getInt(LIMIT, 4);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.widget_card_list, container, false);

        if (layout != null) {
            ((TextView) layout.findViewById(R.id.title)).setText(mTitle);

            if (mImageId > 0) { ((ImageView) layout.findViewById(R.id.image)).setImageResource(mImageId); }
            else { layout.findViewById(R.id.image).setVisibility(View.GONE); }

            mList = (ViewGroup) layout.findViewById(R.id.list);

            new FillList().execute();
        }

        return layout;
    }

    /**
     * Cursor that will hold the values of the list
     * @return  Cursor
     */
    public abstract Cursor getCursor();

    /**
     * The column name in the cursor that will be displayed in the text field.
     * @return  Column name
     */
    public abstract String getTextColumnName();


    public abstract View.OnClickListener getItemClickListener();


    /**
     * Fill the list values on a separate thread.
     */
    class FillList extends AsyncTask<Void, Void, Cursor> {
        @Override
        protected Cursor doInBackground(Void... voids) {
            return getCursor();
        }

        @Override
        protected void onPostExecute(Cursor cursor) {

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    for (int i = 0; i < mLimit && !cursor.isAfterLast(); i++) {
                        final ViewGroup newView = (ViewGroup) LayoutInflater.from(getActivity())
                                .inflate(R.layout.widget_card_list_item, mList, false);
                        if (newView != null && getTextColumnName() != null) {
                            String text = cursor.getString(cursor.getColumnIndex(getTextColumnName()));
                            if (!TextUtils.isEmpty(text)) {
                                ((TypefacedTextView) newView.findViewById(R.id.text)).setText(text);
                                newView.setTag(text);
                                newView.setOnClickListener(getItemClickListener());
                            }
                            mList.addView(newView, i);
                        }
                        cursor.moveToNext();
                    }
                }
                cursor.close();
            }
        }
    }

}
