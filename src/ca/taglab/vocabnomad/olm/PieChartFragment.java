package ca.taglab.vocabnomad.olm;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import ca.taglab.vocabnomad.R;

public class PieChartFragment extends Fragment {

    public static final String CATEGORY = "category";

    public static final int READ = 0;
    public static final int WRITE = 1;
    public static final int SPEAK = 2;
    public static final int LISTEN = 3;

    /** Colors to be used for the pie slices. */
    private static int[] COLORS = new int[] {
            Color.parseColor("#C08DEEEE"),    // blue     read    8DEEEE
            Color.parseColor("#C049E20E"),    // green    write   49E20E
            Color.parseColor("#C0FF4500"),    // red      speak   FF4500
            Color.parseColor("#C0EE7AE9")     // purple   listen  EE7AE9
    };

    /** The main series that will include all the data. */
    private CategorySeries mSeries = new CategorySeries("");

    /** The main renderer for the main dataset. */
    private DefaultRenderer mRenderer = new DefaultRenderer();

    /** The chart view that displays the data. */
    private GraphicalView mChartView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.olm_pie_chart, container, false);
        if (layout != null) {
            LinearLayout chart = (LinearLayout) layout.findViewById(R.id.chart);
            mChartView = ChartFactory.getPieChartView(getActivity(), mSeries, mRenderer);
            chart.addView(mChartView, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            mRenderer.setPanEnabled(false);
            mRenderer.setZoomEnabled(false);
            mRenderer.setShowLegend(false);
            mRenderer.setShowLabels(false);
            mRenderer.setLabelsColor(getResources().getColor(R.color.black));
            mRenderer.setLabelsTextSize(30);
            mRenderer.setDisplayValues(true);

            // Set legend colours
            layout.findViewById(R.id.read).setBackgroundColor(COLORS[READ]);
            layout.findViewById(R.id.read).setOnClickListener(new SkillSelected());
            layout.findViewById(R.id.write).setBackgroundColor(COLORS[WRITE]);
            layout.findViewById(R.id.write).setOnClickListener(new SkillSelected());
            layout.findViewById(R.id.speak).setBackgroundColor(COLORS[SPEAK]);
            layout.findViewById(R.id.speak).setOnClickListener(new SkillSelected());
            layout.findViewById(R.id.listen).setBackgroundColor(COLORS[LISTEN]);
            layout.findViewById(R.id.listen).setOnClickListener(new SkillSelected());

            layout.findViewById(R.id.pie_chart_card).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    unselectAll();
                }
            });

            new SetValues().execute();
        }
        return layout;
    }


    class SkillSelected implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.read:
                    skillSelected(READ);
                    break;
                case R.id.write:
                    skillSelected(WRITE);
                    break;
                case R.id.speak:
                    skillSelected(SPEAK);
                    break;
                case R.id.listen:
                    skillSelected(LISTEN);
                    break;
            }
        }
    }

    public void skillSelected(int skill) {
        for (int i = 0; i < mSeries.getItemCount(); i++) {
            mRenderer.getSeriesRendererAt(i).setHighlighted(i == skill);
        }
        mChartView.repaint();
    }

    public void unselectAll() {
        for (int i = 0; i < mSeries.getItemCount(); i++) {
            mRenderer.getSeriesRendererAt(i).setHighlighted(false);
        }
        mChartView.repaint();
    }


    class SetValues extends AsyncTask<Void, Void, Void> {
        private double reading = 0, writing = 0, speaking = 0, listening = 0;

        @Override
        protected Void doInBackground(Void... voids) {
            this.reading = UserStats.getCount(getActivity(),
                    UserStats.getSelection(UserStats.READING_ACTIONS));
            this.writing = UserStats.getCount(getActivity(),
                    UserStats.getSelection(UserStats.WRITING_ACTIONS));
            this.speaking = UserStats.getCount(getActivity(),
                    UserStats.getSelection(UserStats.SPEAKING_ACTIONS));
            this.listening = UserStats.getCount(getActivity(),
                    UserStats.getSelection(UserStats.LISTENING_ACTIONS));

            double total = reading + writing + speaking + listening;
            reading = Math.round((Double) (reading / total) * 100);
            writing = Math.round((Double) (writing / total) * 100);
            listening = Math.round((Double) (listening / total) * 100);
            speaking = Math.round((Double) (speaking / total) * 100);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mSeries.clear();

            for (int i = 0; i < 4; i++) {
                switch (i) {
                    case READ:
                        mSeries.add("Reading", this.reading);
                        break;
                    case WRITE:
                        mSeries.add("Writing", this.writing);
                        break;
                    case SPEAK:
                        mSeries.add("Speaking", this.speaking);
                        break;
                    case LISTEN:
                        mSeries.add("Listening", this.listening);
                        break;
                }

                SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
                renderer.setColor(COLORS[i % COLORS.length]);
                mRenderer.addSeriesRenderer(renderer);
            }

            mChartView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
                    if (seriesSelection == null) {
                        unselectAll();
                    } else {
                        skillSelected(seriesSelection.getPointIndex());
                    }
                }
            });

            mChartView.repaint();
        }
    }


}
