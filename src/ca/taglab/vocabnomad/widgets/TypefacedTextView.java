package ca.taglab.vocabnomad.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;
import ca.taglab.vocabnomad.R;

public class TypefacedTextView extends TextView {

    public TypefacedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTextView(context, attrs);
    }

    public TypefacedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        initTextView(context, attrs);
    }

    private void initTextView(Context context, AttributeSet attrs) {
        //Typeface.createFromAsset doesn't work in the layout editor. Skipping...
        if (isInEditMode()) {
            return;
        }

        TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.TypefacedTextView);
        String fontName = styledAttrs.getString(R.styleable.TypefacedTextView_typeface);
        styledAttrs.recycle();

        if (fontName != null) {
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), fontName);
            setTypeface(typeface);
        }
    }
}