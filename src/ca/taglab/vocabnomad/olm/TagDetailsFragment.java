package ca.taglab.vocabnomad.olm;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.crypto.Mac;

import ca.taglab.vocabnomad.R;


public class TagDetailsFragment extends Fragment {
    private static final String TAG_NAME = "tag_name";
    private String mTagName;


    /**
     * Create a new instance of the TagDetailsFragment.
     * @param tag   Name of the tag to display details
     * @return      A new instance of fragment TagDetailsFragment for 'tag'
     */
    public static TagDetailsFragment newInstance(String tag) {
        TagDetailsFragment fragment = new TagDetailsFragment();
        Bundle args = new Bundle();
        args.putString(TAG_NAME, tag);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.olm_tag_details, container, false);

        if (layout != null && layout.findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return layout;
            }

            CategoryTitleFragment title = CategoryTitleFragment.newInstance(mTagName);
            FavouriteWordsFragment words = FavouriteWordsFragment.newInstance(mTagName);
            RelatedTagsFragment tags = RelatedTagsFragment.newInstance(mTagName);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.tag_title, title)
                    .add(R.id.fragment_container, words)
                    .add(R.id.fragment_container, tags)
                    .commit();
        }

        return layout;
    }


}
