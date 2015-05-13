package ua.parus.pmo.parus8claims.objects.claim.hist;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import ua.parus.pmo.parus8claims.R;

/**
 * Created by igorgo on 26.04.2015.
 */
@SuppressWarnings("DefaultFileTemplate")
class ClaimHist {

    private static final int ACT_NOTE = 1;
    private static final int ACT_FWD = 2;
    private static final int ACT_NULL = 3;
    private static final int ACT_RET = 4;
    private static final int ACT_SEND = 5;
    private static final int ACT_UPD = 6;
    private static final int ACT_INS = 7;


    public static final String FLAG_COMMENT_OTHER = "O";
    // --Commented out by Inspection (13.05.2015 16:03):public static final String FLAG_NOCOMMENT = "N";
    public static final String FLAG_COMMENT_AUTHOR = "A";
    // --Commented out by Inspection (13.05.2015 16:03):public static final String FLAG_IGNORE = "I";

    private static final String TOKEN_WHO = "@@";
    private static final String TOKEN_WHOM = "$$";
    private static final String TOKEN_STATE = "##";
    private static final String SPACE = " ";

    public String flag;
    public String dateHist;
    public String who;
    public String newState;
    public String whom;
    public String textContent;
    public int action;

    private static CharSequence setSpanBetweenTokens(CharSequence text,
                                                     String token, CharacterStyle... cs) {
        // Start and end refer to the points where the span will apply
        int tokenLen = token.length();
        int start = text.toString().indexOf(token) + tokenLen;
        int end = text.toString().indexOf(token, start);

        if (start > -1 && end > -1) {
            // Copy the spannable string to a mutable spannable string
            SpannableStringBuilder ssb = new SpannableStringBuilder(text);
            for (CharacterStyle c : cs)
                ssb.setSpan(c, start, end, 0);

            // Delete the tokens before and after the span
            ssb.delete(end, end + tokenLen);
            ssb.delete(start - tokenLen, start);

            text = ssb;
        }

        return text;
    }

    CharSequence actionAsText(Context context) {
        CharSequence lText;
        lText = dateHist;
        lText = lText + SPACE + TOKEN_WHO + who + TOKEN_WHO;

        switch (action) {
            case ACT_NOTE:
                lText = lText + SPACE + context.getString(R.string.act_snote);
                break;
            case ACT_FWD:
                lText = lText + SPACE + context.getString(R.string.act_sforward);
                lText = lText + SPACE + TOKEN_STATE + "«" + newState + "»" + TOKEN_STATE;
                lText = lText + "." + SPACE + context.getString(R.string.claim_executor) + ": " + TOKEN_WHOM + whom + TOKEN_WHOM;
                break;
            case ACT_NULL:
                lText = lText + SPACE + context.getString(R.string.act_snull);
                break;
            case ACT_RET:
                lText = lText + SPACE + context.getString(R.string.act_sreturn);
                lText = lText + SPACE + TOKEN_STATE + "«" + newState + "»" + TOKEN_STATE;
                lText = lText + "." + SPACE + context.getString(R.string.claim_executor) + ": " + TOKEN_WHOM + whom + TOKEN_WHOM;
                break;
            case ACT_SEND:
                lText = lText + SPACE + context.getString(R.string.act_ssend);
                lText = lText + "." + SPACE + context.getString(R.string.claim_executor) + ": " + TOKEN_WHOM + whom + TOKEN_WHOM;
                break;
            case ACT_UPD:
                lText = lText + SPACE + context.getString(R.string.act_supdate);
                break;
            case ACT_INS:
                lText = lText + SPACE + context.getString(R.string.act_sinsert);
                break;
        }
        lText = setSpanBetweenTokens(lText, TOKEN_WHO, new StyleSpan(Typeface.BOLD));
        lText = setSpanBetweenTokens(lText, TOKEN_STATE, new StyleSpan(Typeface.BOLD_ITALIC), new ForegroundColorSpan(0xFF000088));
        lText = setSpanBetweenTokens(lText, TOKEN_WHOM, new StyleSpan(Typeface.BOLD));
        return lText;
    }


}
