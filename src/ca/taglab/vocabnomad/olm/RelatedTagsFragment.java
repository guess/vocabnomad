package ca.taglab.vocabnomad.olm;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.widgets.CardListFragment;


public class RelatedTagsFragment extends CardListFragment {
    private static final String TAG_NAME = "tag_name";
    private String mTagName;


    /**
     * Create a new instance of the RelatedTagsFragment.
     * @param tag   Tag to show related tags of
     * @return      A new instance of fragment RelatedTagsFragment for 'tag'
     */
    public static RelatedTagsFragment newInstance(String tag) {
        RelatedTagsFragment fragment = new RelatedTagsFragment();
        Bundle args = new Bundle();
        args.putString(TAG_NAME, tag);
        args.putString(TITLE, "Related tags");
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTagName = getArguments().getString(TAG_NAME);
        }
    }

    @Override
    public Cursor getCursor() {
        return UserStats.getRelatedTags(getActivity(), mTagName);
    }

    @Override
    public String getTextColumnName() {
        return Contract.View.NAME;
    }

    @Override
    public View.OnClickListener getItemClickListener() {
        return new ItemClick();
    }

    class ItemClick implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            String tag = (String) view.getTag();
            Intent intent = new Intent(getActivity(), TagDetailsActivity.class);
            intent.putExtra(TagDetailsActivity.TAG_NAME, tag);
            startActivity(intent);
        }
    }
}
