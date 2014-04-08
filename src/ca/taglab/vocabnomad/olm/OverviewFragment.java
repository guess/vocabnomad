package ca.taglab.vocabnomad.olm;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.HashMap;

import ca.taglab.vocabnomad.R;


public class OverviewFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.olm_overview, container, false);

        if (layout != null && layout.findViewById(R.id.overview_container) != null) {

            PieChartFragment pie = new PieChartFragment();
            MacroSkillFragment read = MacroSkillFragment.newInstance(UserStats.READING);
            MacroSkillFragment write = MacroSkillFragment.newInstance(UserStats.WRITING);
            MacroSkillFragment listen = MacroSkillFragment.newInstance(UserStats.LISTENING);
            MacroSkillFragment speak = MacroSkillFragment.newInstance(UserStats.SPEAKING);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .add(R.id.overview_container, pie)
                    .add(R.id.overview_container, read)
                    .add(R.id.overview_container, write)
                    .add(R.id.overview_container, listen)
                    .add(R.id.overview_container, speak)
                    .commit();
        }

        return layout;
    }

}
