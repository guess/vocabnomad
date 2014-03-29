package ca.taglab.vocabnomad.olm;

import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.DatabaseHelper;
import ca.taglab.vocabnomad.db.UserEvents;

public class PieChartFragment extends Fragment {

    public static final String CATEGORY = "category";

    public static final int READ = 0;
    public static final int WRITE = 1;
    public static final int SPEAK = 2;
    public static final int LISTEN = 3;

    /** Colors to be used for the pie slices. */
    private static int[] COLORS = new int[] {
            Color.parseColor("#8DEEEE"),    // blue     read
            Color.parseColor("#49E20E"),    // green    write
            Color.parseColor("#FF4500"),    // red      speak
            Color.parseColor("#EE7AE9")     // purple   listen
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

            // Set legend colours
            layout.findViewById(R.id.read).setBackgroundColor(COLORS[READ]);
            layout.findViewById(R.id.read).setOnClickListener(new SkillSelected());
            layout.findViewById(R.id.write).setBackgroundColor(COLORS[WRITE]);
            layout.findViewById(R.id.write).setOnClickListener(new SkillSelected());
            layout.findViewById(R.id.speak).setBackgroundColor(COLORS[SPEAK]);
            layout.findViewById(R.id.speak).setOnClickListener(new SkillSelected());
            layout.findViewById(R.id.listen).setBackgroundColor(COLORS[LISTEN]);
            layout.findViewById(R.id.listen).setOnClickListener(new SkillSelected());
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

    @Override
    public void onResume() {
        super.onResume();
        if (mChartView != null) {
            mChartView.repaint();
            mChartView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
                    if (seriesSelection == null) {
                        for (int i = 0; i < mSeries.getItemCount(); i++) {
                            mRenderer.getSeriesRendererAt(i).setHighlighted(false);
                        }
                        mChartView.repaint();
                    } else {
                        skillSelected(seriesSelection.getPointIndex());
                    }
                }
            });
        }
        new SetValues().execute();
    }


    class SetValues extends AsyncTask<Void, Void, Void> {
        private long reading = 0, writing = 0, speaking = 0, listening = 0;

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
                mChartView.repaint();
            }
        }
    }


}
