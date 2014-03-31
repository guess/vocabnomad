package ca.taglab.vocabnomad.olm;



import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.DatabaseHelper;
import ca.taglab.vocabnomad.widgets.ImageOpt;
import ca.taglab.vocabnomad.widgets.TypefacedTextView;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link CategoryTitleFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class CategoryTitleFragment extends Fragment {
    private static final String TITLE = "param1";
    private String mTitle;
    private ViewGroup mLayout;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CategoryTitleFragment.
     */
    public static CategoryTitleFragment newInstance(String title) {
        CategoryTitleFragment fragment = new CategoryTitleFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getString(TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLayout = (ViewGroup) inflater.inflate(R.layout.olm_category_title, container, false);
        if (!TextUtils.isEmpty(mTitle)) {
            ((TypefacedTextView) mLayout.findViewById(R.id.title)).setText(mTitle);
            new GetImages().execute();
        }
        return mLayout;
    }


    class GetImages extends AsyncTask<Void, Void, ArrayList<Bitmap>> {

        @Override
        protected ArrayList<Bitmap> doInBackground(Void... voids) {
            ArrayList<String> paths = getFilePaths(mTitle);

            ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
            for (String path : paths) {
                String prefix = Environment.getExternalStorageDirectory() + "/VocabNomad/";
                bitmaps.add(ImageOpt.decodeSampledBitmapFromFile(prefix + path, 50, 50));
            }

            return bitmaps;
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> bitmaps) {
            if (bitmaps != null) {

                // Remove the images that will not be used
                switch (bitmaps.size()) {
                    case 0:
                        mLayout.findViewById(R.id.image1).setVisibility(View.GONE);
                    case 1:
                        mLayout.findViewById(R.id.image2).setVisibility(View.GONE);
                    case 2:
                        mLayout.findViewById(R.id.image3).setVisibility(View.GONE);
                    case 3:
                        mLayout.findViewById(R.id.image4).setVisibility(View.GONE);
                }

                // Set the images that will be used
                switch (bitmaps.size()) {
                    case 4:
                        ((ImageView) mLayout.findViewById(R.id.image4)).setImageBitmap(bitmaps.get(3));
                    case 3:
                        ((ImageView) mLayout.findViewById(R.id.image3)).setImageBitmap(bitmaps.get(2));
                    case 2:
                        ((ImageView) mLayout.findViewById(R.id.image2)).setImageBitmap(bitmaps.get(1));
                    case 1:
                        ((ImageView) mLayout.findViewById(R.id.image1)).setImageBitmap(bitmaps.get(0));
                    default:
                        break;
                }
            }
        }

        /**
         * Get an array of file paths for vocabulary associated with tag 'name'
         * @param name  Name of the tag to get vocabulary images for
         * @return      Bitmap full of file paths for vocabulary associated with the 'name'
         */
        public ArrayList<String> getFilePaths(String name) {
            ArrayList<String> paths = new ArrayList<String>();
            DatabaseHelper db = DatabaseHelper.getInstance(getActivity());

            Cursor cursor = db.query(
                    Contract.View.TABLE,
                    new String[] { Contract.View.IMG },
                    Contract.View.NAME + "=? COLLATE NOCASE AND " + Contract.View.IMG + " IS NOT NULL LIMIT 4",
                    new String[] { name },
                    null, null, null
            );

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        paths.add(cursor.getString(cursor.getColumnIndex(Contract.View.IMG)));
                        cursor.moveToNext();
                    }
                }
                cursor.close();
            }

            return paths;
        }
    }


}
