package ca.taglab.vocabnomad.details;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.db.Contract;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VocabDetailsShared.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VocabDetailsShared#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class VocabDetailsShared extends Fragment {
    private static final String WORD_ID = "word_id";
    private long mWordId;

    private OnFragmentInteractionListener mListener;

    public static VocabDetailsShared newInstance(long word_id) {
        VocabDetailsShared fragment = new VocabDetailsShared();
        Bundle args = new Bundle();
        args.putLong(WORD_ID, word_id);
        fragment.setArguments(args);
        return fragment;
    }
    public VocabDetailsShared() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mWordId = getArguments().getLong(WORD_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.details_shared, parent, false);
        if (layout != null) {
            View checkBox = layout.findViewById(R.id.shared);
            new SetSharedCheckbox(checkBox).execute();
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Change the shared status
                    boolean isShared = (Boolean) view.getTag();
                    view.setTag(!isShared);
                    drawCheckBox(view);
                    setShared(!isShared);
                }
            });
        }
        return layout;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    /*
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    } */

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void drawCheckBox(View checkBox) {
        boolean isShared = (Boolean) checkBox.getTag();
        if (isShared) {
            ((ImageView) checkBox).setImageResource(R.drawable.checkbox_checked);
        } else {
            ((ImageView) checkBox).setImageResource(R.drawable.checkbox_unchecked);
        }
    }

    synchronized private void setShared(final boolean isShared) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentValues values = new ContentValues();
                values.put(Contract.Word.SHARED, isShared);
                Uri uri = ContentUris.withAppendedId(Contract.Word.getUri(), mWordId);
                if (uri != null) {
                    getActivity().getContentResolver().update(uri, values, null, null);
                }
            }
        }).start();
    }

    class SetSharedCheckbox extends AsyncTask<Void, Void, Boolean> {
        private View checkBox;

        SetSharedCheckbox(View checkBox) {
            this.checkBox = checkBox;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean isShared = false;

            Uri uri = ContentUris.withAppendedId(Contract.Word.getUri(), mWordId);
            if (uri == null) return false;
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    isShared = cursor.getInt(cursor.getColumnIndex(Contract.Word.SHARED)) > 0;
                }
                cursor.close();
            }
            return isShared;
        }

        @Override
        protected void onPostExecute(Boolean isShared) {
            this.checkBox.setTag(isShared);
            drawCheckBox(checkBox);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
