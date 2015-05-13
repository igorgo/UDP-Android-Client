package ua.parus.pmo.parus8claims.gui;

import android.widget.MultiAutoCompleteTextView;

/**
 * Created by igorgo on 18.04.2015.
 */
public class SemicolonTokenizer implements MultiAutoCompleteTextView.Tokenizer {
    /**
     * Returns the start of the token that ends at offset
     * <code>cursor</code> within <code>text</code>.
     *
     * @param text
     * @param cursor
     */
    @Override
    public int findTokenStart(CharSequence text, int cursor) {
        int i = cursor;
        while (i > 0 && text.charAt(i - 1) != ';')
            i--;
        //while (i < cursor && text.charAt(i) == ' ')
        //    i++;
        return i;
    }

    /**
     * Returns the end of the token (minus trailing punctuation)
     * that begins at offset <code>cursor</code> within <code>text</code>.
     *
     * @param text
     * @param cursor
     */
    @Override
    public int findTokenEnd(CharSequence text, int cursor) {
        int i = cursor;
        int len = text.length();
        while (i < len) {
            if (text.charAt(i) == ';') {
                return i;
            } else {
                i++;
            }
        }
        return len;
    }

    /**
     * Returns <code>text</code>, modified, if necessary, to ensure that
     * it ends with a token terminator (for example a space or comma).
     *
     * @param text
     */
    @Override
    public CharSequence terminateToken(CharSequence text) {
        return text;
    }
}
