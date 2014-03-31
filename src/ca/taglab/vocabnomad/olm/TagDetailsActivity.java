package ca.taglab.vocabnomad.olm;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.crypto.Mac;

import ca.taglab.vocabnomad.R;


public class TagDetailsActivity extends FragmentActivity {
    public static final String TAG_NAME = "tag_name";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String tag = getIntent().getStringExtra(TAG_NAME);

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            CategoryTitleFragment title = CategoryTitleFragment.newInstance(tag);
            FavouriteWordsFragment words = FavouriteWordsFragment.newInstance(tag);
            RelatedTagsFragment tags = RelatedTagsFragment.newInstance(tag);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.tag_title, title)
                    .add(R.id.fragment_container, words)
                    .add(R.id.fragment_container, tags)
                    .commit();
        }
    }


}
