package ca.taglab.vocabnomad.olm;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.widgets.CardListFragment;

public class MacroSkillFragment extends CardListFragment {

    public static final String SKILL = "skill";
    private int mSkill;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.mSkill = getArguments().getInt(SKILL);
        }
    }

    @Override
    public Cursor getCursor() {
        Cursor cursor = null;
        switch (mSkill) {
            case UserStats.READING:
                cursor = UserStats.getTags(getActivity(), UserStats.READING_ACTIONS);
                break;
            case UserStats.WRITING:
                cursor = UserStats.getTags(getActivity(), UserStats.WRITING_ACTIONS);
                break;
            case UserStats.LISTENING:
                cursor = UserStats.getTags(getActivity(), UserStats.LISTENING_ACTIONS);
                break;
            case UserStats.SPEAKING:
                cursor = UserStats.getTags(getActivity(), UserStats.SPEAKING_ACTIONS);
                break;
        }
        return cursor;
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


    /**
     * Create a new instance of the favourite word fragment
     * @return A new instance of fragment FavouriteWordsFragment.
     */
    public static MacroSkillFragment newInstance(int skill) {
        MacroSkillFragment fragment = new MacroSkillFragment();
        Bundle args = new Bundle();
        args.putInt(SKILL, skill);

        switch (skill) {
            case UserStats.READING:
                args.putString(TITLE, "Reading");
                args.putInt(IMAGE, R.drawable.read);
                break;
            case UserStats.WRITING:
                args.putString(TITLE, "Writing");
                args.putInt(IMAGE, R.drawable.write);
                break;
            case UserStats.LISTENING:
                args.putString(TITLE, "Listening");
                args.putInt(IMAGE, R.drawable.listen);
                break;
            case UserStats.SPEAKING:
                args.putString(TITLE, "Speaking");
                args.putInt(IMAGE, R.drawable.speak);
                break;
            default:
                throw new IllegalArgumentException("Unknown skill: Are you divergent?");
        }

        fragment.setArguments(args);
        return fragment;
    }
}
