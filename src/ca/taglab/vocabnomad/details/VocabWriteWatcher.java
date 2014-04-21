package ca.taglab.vocabnomad.details;

import android.text.Editable;
import android.text.TextWatcher;


public class VocabWriteWatcher implements TextWatcher {
    VocabDetailsListener mListener;

    VocabWriteWatcher(VocabDetailsListener listener) {
        this.mListener = listener;
    }

    /*
     *  This method is called to notify you that, within 's', the 'count' characters
     *  beginning at 'start' are about to be replaced by new text with length 'after'.
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    /*
     *  This method is called to notify you that, within 's', the 'count' characters
     *  beginning at 'start' have just replaced old text that had length 'before'.
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // TODO: Check if the changed text creates a new word that is spelled correctly
        // TODO: Then give them the points for the number of characters in the word
        mListener.onProgressIncrement(VocabDetailsProgress.WRITE);
    }

    /*
     *  This method is called to notify you that, somewhere within 's',
     *  the text has been changed.
     */
    @Override
    public void afterTextChanged(Editable s) {

    }
}
