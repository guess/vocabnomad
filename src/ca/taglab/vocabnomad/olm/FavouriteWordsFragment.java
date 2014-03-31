package ca.taglab.vocabnomad.olm;

import android.database.Cursor;
import android.os.Bundle;

import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.widgets.CardListFragment;

public class FavouriteWordsFragment extends CardListFragment {
    public static final String TAG_NAME = "tag_name";
    private String mTagName;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTagName = getArguments().getString(TAG_NAME);
        }
    }


    @Override
    public Cursor getCursor() {
        return UserStats.getFavWords(getActivity(), this.mTagName);
    }


    @Override
    public String getTextColumnName() {
        return Contract.View.ENTRY;
    }


    /**
     * Create a new instance of the favourite word fragment
     * @return A new instance of fragment FavouriteWordsFragment.
     */
    public static FavouriteWordsFragment newInstance(String tag) {
        FavouriteWordsFragment fragment = new FavouriteWordsFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, "Favourite words");
        args.putString(TAG_NAME, tag);
        fragment.setArguments(args);
        return fragment;
    }
}