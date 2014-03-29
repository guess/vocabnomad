package ca.taglab.vocabnomad.olm;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.widgets.TypefacedTextView;


public class OverviewFragment extends Fragment {
    PieChartFragment pieChartFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.olm_overview, container, false);

        if (layout != null) drawDetails(layout);

        UserStats.getTags(getActivity(), UserStats.READING_ACTIONS);

        return layout;
    }

    /**
     * Dynamically draw the detailed view of the OLM overview
     * @param layout    Activity layout
     */
    private void drawDetails(View layout) {
        ViewGroup details;

        details = (ViewGroup) layout.findViewById(R.id.read_details);
        ((ImageView) details.findViewById(R.id.skill_img)).setImageResource(R.drawable.read);
        ((TypefacedTextView) details.findViewById(R.id.skill_name)).setText(R.string.reading);

        details = (ViewGroup) layout.findViewById(R.id.write_details);
        ((ImageView) details.findViewById(R.id.skill_img)).setImageResource(R.drawable.write);
        ((TypefacedTextView) details.findViewById(R.id.skill_name)).setText(R.string.writing);

        details = (ViewGroup) layout.findViewById(R.id.listen_details);
        ((ImageView) details.findViewById(R.id.skill_img)).setImageResource(R.drawable.listen);
        ((TypefacedTextView) details.findViewById(R.id.skill_name)).setText(R.string.listening);

        details = (ViewGroup) layout.findViewById(R.id.speak_details);
        ((ImageView) details.findViewById(R.id.skill_img)).setImageResource(R.drawable.speak);
        ((TypefacedTextView) details.findViewById(R.id.skill_name)).setText(R.string.speaking);
    }

}
