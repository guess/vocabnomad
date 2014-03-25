package ca.taglab.vocabnomad;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;
import ca.taglab.vocabnomad.db.Contract;
import ca.taglab.vocabnomad.db.UserEvents;
import ca.taglab.vocabnomad.types.Tag;
import ca.taglab.vocabnomad.types.Word;
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

public class VocabSearch extends Activity {
    public static final String TAG = "VocabSearch";

    private ProgressBar progressBar;
    private ImageView searchIcon;
    private ViewGroup vocab, tags;

    static HashMap<Integer, Language> languages = new HashMap<Integer, Language>();
    static {
        languages.put(1, Language.ESTONIAN);
        languages.put(5, Language.SPANISH);
        languages.put(6, Language.ENGLISH);
        languages.put(7, Language.HINDI);
        languages.put(8, Language.ARABIC);
        languages.put(10, Language.PORTUGUESE);
        languages.put(11, Language.RUSSIAN);
        languages.put(12, Language.JAPANESE);
        languages.put(14, Language.LATVIAN);
        languages.put(15, Language.GERMAN);
        languages.put(19, Language.VIETNAMESE);
        languages.put(21, Language.FRENCH);
        languages.put(22, Language.KOREAN);
        languages.put(25, Language.TURKISH);
        languages.put(27, Language.ITALIAN);
        languages.put(30, Language.POLISH);
        languages.put(34, Language.UKRAINIAN);
        languages.put(35, Language.MALAY);
        languages.put(46, Language.THAI);
        languages.put(49, Language.ROMANIAN);
        languages.put(50, Language.DUTCH);
        languages.put(82, Language.GREEK);
        languages.put(85, Language.HUNGARIAN);
        languages.put(87, Language.BULGARIAN);
        languages.put(95, Language.CZECH);
        languages.put(102, Language.SWEDISH);
        languages.put(107, Language.HAITIAN_CREOLE);
        languages.put(110, Language.ITALIAN);               // Calabrese
        languages.put(121, Language.ITALIAN);               // Venetian
        languages.put(127, Language.DANISH);
        languages.put(130, Language.HEBREW);
        languages.put(131, Language.FINNISH);
        languages.put(132, Language.SLOVAK);
        languages.put(135, Language.SLOVENIAN);
        languages.put(127, Language.ITALIAN);               // Sicilian
        languages.put(138, Language.NORWEGIAN);
        languages.put(127, Language.DANISH);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.search_bg));
        actionBar.setIcon(R.drawable.launch_icon);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        progressBar = (ProgressBar) findViewById(R.id.progress);
        searchIcon = (ImageView) findViewById(R.id.search);

        vocab = (ViewGroup) findViewById(R.id.vocab);
        tags = (ViewGroup) findViewById(R.id.tags);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add("Search");
        item.setIcon(R.drawable.action_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        final SearchView sv = new SearchView(getApplicationContext());
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                new TranslateText().execute(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)) {
                    progressBar.setVisibility(View.VISIBLE);
                    searchIcon.setVisibility(View.GONE);
                    findViewById(R.id.results).setVisibility(View.VISIBLE);
                    new SearchTags().execute(newText);
                    new SearchVocab().execute(newText);
                } else {
                    progressBar.setVisibility(View.GONE);
                    searchIcon.setVisibility(View.VISIBLE);
                    findViewById(R.id.results).setVisibility(View.GONE);
                }
                return false;
            }
        });

        sv.setIconified(false);
        item.setActionView(sv);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpTo(this, new Intent(this, VocabListActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    class SearchTags extends AsyncTask<String, Void, Cursor> {
        String query;

        @Override
        protected Cursor doInBackground(String... params) {
            query = params[0];

            Log.i(TAG, "Search query: " + query);

            return getContentResolver().query(
                    Contract.Tag.getUri(),
                    null,
                    Contract.Tag.NAME + " LIKE \'%" + query + "%\'",
                    null,
                    Contract.Tag.NAME + " DESC LIMIT 4"
            );
        }

        @Override
        protected void onPostExecute(final Cursor cursor) {
            super.onPostExecute(cursor);
            progressBar.setVisibility(View.GONE);
            tags.removeAllViewsInLayout();

            if (query.length() > 2) {
                Cursor jit = getContentResolver().query(
                        Contract.Tag.getUri(),
                        null,
                        Contract.Tag.NAME + "=?",
                        new String[] { query },
                        null
                );

                if (jit == null || !jit.moveToFirst()) {
                    final ViewGroup newView = (ViewGroup) LayoutInflater.from(getApplicationContext()).inflate(
                            R.layout.search_result,
                            tags,
                            false
                    );

                    ((TextView) newView.findViewById(android.R.id.text1)).setText(query);
                    newView.setBackgroundResource(R.drawable.list_item_bg);

                    newView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            UserEvents.log(
                                    Contract.UserEvents.JIT_VOC_REQUEST,
                                    0, 0, 0, 0,
                                    (String) ((TextView) v.findViewById(android.R.id.text1)).getText()
                            );

                            Intent intent = new Intent();
                            intent.putExtra("tag", ((TextView) v.findViewById(android.R.id.text1)).getText());
                            intent.putExtra("jit", true);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    });

                    tags.addView(newView, 0);
                }

                if (jit != null) {
                    jit.close();
                }
            }

            if (cursor != null && cursor.moveToFirst()) {
                String name;

                while (!cursor.isAfterLast()) {
                    name = cursor.getString(cursor.getColumnIndex(Contract.Tag.NAME));

                    final ViewGroup newView = (ViewGroup) LayoutInflater.from(getApplicationContext()).inflate(
                            R.layout.search_result,
                            tags,
                            false
                    );

                    ((TextView) newView.findViewById(android.R.id.text1)).setText(name);
                    newView.setBackgroundResource(R.drawable.list_item_bg);
                    newView.setTag(cursor.getLong(cursor.getColumnIndex(Contract.Tag._ID)));

                    newView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Tag tag = new Tag();
                            tag.refresh(ContentUris.withAppendedId(Contract.Tag.getUri(), (Long) v.getTag()), getContentResolver());

                            UserEvents.log(
                                    Contract.UserEvents.TAG_SEARCH,
                                    0, 0,
                                    tag.getId(),
                                    tag.getServerId(),
                                    query
                            );

                            tag.cursor.close();

                            Intent intent = new Intent();
                            intent.putExtra("tag", ((TextView) v.findViewById(android.R.id.text1)).getText());
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    });

                    tags.addView(newView, 0);

                    cursor.moveToNext();
                }

                cursor.close();
            }
        }
    }


    class TranslateText extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String text = params[0];
            String translation = null;

            Translate.setClientId("taglab_vocabnomad");
            Translate.setClientSecret("J4+0CAnIByVOBXQIL+Q9KKsn/qzcMMmRJfJLxD6Aj28=");

            UserEvents.log(
                    Contract.UserEvents.L1_SEARCH,
                    0, 0, 0, 0, text
            );

            try {
                translation = Translate.execute(text, Language.ENGLISH);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (translation != null && translation.equals(text)) {
                translation = null;
            }

            return translation;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s != null) {
                final ViewGroup newView = (ViewGroup) LayoutInflater.from(getApplicationContext()).inflate(
                        R.layout.search_result,
                        tags,
                        false
                );

                ((TextView) newView.findViewById(android.R.id.text1)).setText(s.toLowerCase());
                newView.setBackgroundResource(R.drawable.list_item_bg);

                newView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UserEvents.log(
                                Contract.UserEvents.JIT_VOC_REQUEST,
                                0, 0, 0, 0,
                                (String) ((TextView) v.findViewById(android.R.id.text1)).getText()
                        );

                        Intent intent = new Intent();
                        intent.putExtra("tag", ((TextView) v.findViewById(android.R.id.text1)).getText());
                        intent.putExtra("jit", true);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });

                tags.addView(newView, 0);
            }
        }
    }


    class SearchVocab extends AsyncTask<String, Void, Cursor> {
        String query;

        @Override
        protected Cursor doInBackground(String... params) {
            query = params[0];

            Log.i(TAG, "Search query: " + query);

            return getContentResolver().query(
                    Contract.Word.getUri(),
                    null,
                    Contract.Word.ENTRY + " LIKE \'%" + query + "%\'",
                    null,
                    Contract.Word.ENTRY + " DESC LIMIT 6"
            );
        }

        @Override
        protected void onPostExecute(final Cursor cursor) {
            super.onPostExecute(cursor);
            progressBar.setVisibility(View.GONE);
            vocab.removeAllViewsInLayout();

            if (cursor != null && cursor.moveToFirst()) {
                String name;

                while (!cursor.isAfterLast()) {
                    name = cursor.getString(cursor.getColumnIndex(Contract.Word.ENTRY));

                    final ViewGroup newView = (ViewGroup) LayoutInflater.from(getApplicationContext()).inflate(
                            R.layout.search_result,
                            vocab,
                            false
                    );

                    ((TextView) newView.findViewById(android.R.id.text1)).setText(name);
                    newView.setTag(cursor.getLong(cursor.getColumnIndex(Contract.Word._ID)));
                    newView.setBackgroundResource(R.drawable.list_item_bg);

                    String image = cursor.getString(cursor.getColumnIndex(Contract.Word.IMG));
                    ((ImageView) newView.findViewById(android.R.id.icon)).setImageResource(R.drawable.image_placeholder);
                    if (image != null) {
                        File f = new File(Environment.getExternalStorageDirectory() + "/VocabNomad", image);
                        ((ImageView) newView.findViewById(android.R.id.icon)).setImageBitmap(getImage(f, 80));
                    }

                    newView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Word word = new Word();
                            word.refresh(ContentUris.withAppendedId(Contract.Word.getUri(), (Long) v.getTag()), getContentResolver());

                            UserEvents.log(
                                    Contract.UserEvents.L2_SEARCH,
                                    word.getId(),
                                    word.getServerId(),
                                    0, 0,
                                    query
                            );

                            Intent intent = new Intent(getApplicationContext(), ViewWordActivity.class);
                            intent.putExtra("id", word.getId());
                            word.cursor.close();
                            startActivity(intent);
                        }
                    });

                    vocab.addView(newView, 0);

                    cursor.moveToNext();
                }

                cursor.close();
            }
        }
    }


    public static Bitmap getImage(File f, int max_size) {
        Bitmap b = null;

        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();

            int scale = 1;
            if (o.outHeight > max_size || o.outWidth > max_size) {
                scale = (int)Math.pow(2, (int) Math.round(Math.log(max_size /
                        (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b;
    }


}
