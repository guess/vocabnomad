package ca.taglab.vocabnomad.olm;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.taglab.vocabnomad.R;


public class OverviewFragment extends Fragment {
    PieChartFragment pieChartFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.olm_overview, container, false);
        return layout;
    }

}
