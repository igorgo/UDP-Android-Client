package ua.parus.pmo.parus8claims;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.net.MalformedURLException;

import ua.parus.pmo.parus8claims.om.claim.Claim;
import ua.parus.pmo.parus8claims.rest.RestRequest;


public class ClaimActivity extends ActionBarActivity
        implements
        ClaimHistoryFragment.ClaimHistoryFragmentInterface {

    private static final String TAG = ClaimActivity.class.getSimpleName();
    public static final String EXTRA_RN_KEY = "e-rn";
    public static final String EXTRA_HAS_DOCS_KEY = "e-has-docs";
    private String mSession;
    private Menu mOptionsMenu;
    private Claim mClaim;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long lRn = getIntent().getLongExtra(EXTRA_RN_KEY, 0);
        boolean mHasDocs = getIntent().getBooleanExtra(EXTRA_HAS_DOCS_KEY, false);
        setContentView(R.layout.activity_claim);
        mSession = ((ClaimApplication) this.getApplication()).getSessionId();
        mClaim = null;
        ClaimHistoryFragment lHistFrag = ClaimHistoryFragment.newInstance(lRn, mSession, mHasDocs);
        getSupportActionBar().setTitle("");
        if (savedInstanceState == null) {
            FragmentTransaction lFt = getSupportFragmentManager().beginTransaction();
            lFt.add(R.id.container, lHistFrag);
            lFt.commit();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(MainActivity.RESULT_CANCEL, intent);
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_claim, menu);
        mOptionsMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //TODO: Исправление рекламации
        //TODO: Удаление рекламации
        //TODO: Аннулирование рекламации
        //TODO: Переход рекламации
        //TODO: Возврат рекламации
        //TODO: Пересыл рекламации
        //TODO: Добавление примечания
        //TODO: Добавление аттача


        if (id == R.id.action_claim_edit) {
            Intent iAction = new Intent(this, ClaimActionActivity.class);
            iAction.putExtra(ClaimActionActivity.EXTRA_KEY_CLAIM, mClaim);
            iAction.putExtra(ClaimActionActivity.EXTRA_KEY_REQUEST, ClaimActionActivity.REQUEST_EDIT);
            startActivityForResult(iAction, ClaimActionActivity.REQUEST_EDIT);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onHistoryLoaded(ClaimHistoryFragment fragment, Claim claim) {
        mClaim = claim;
        //TODO: протестить на других версиях (с и без getSupportActionBar())
        getSupportActionBar().invalidateOptionsMenu();
        invalidateOptionsMenu();
    }

    @Override
    public void onDocumDownloadRequest(ClaimHistoryFragment.Attach attach) {
        if (attach == null) {
            return;
        }
        Log.i(TAG, "DocumDownloadRequest: Rn=" + String.valueOf(attach.getFileRn()) + "; FileName=" + attach.getFileName());
        new GetDocumentAsyncTask().execute(attach);
    }

    private String fileExt(String url) {
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf("."));
            if (url.contains("%")) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (url.contains("/")) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mClaim == null) {
            menu.findItem(R.id.action_claim_edit).setVisible(false);
            menu.findItem(R.id.action_claim_add_file).setVisible(false);
            menu.findItem(R.id.action_claim_add_note).setVisible(false);
            menu.findItem(R.id.action_claim_close).setVisible(false);
            menu.findItem(R.id.action_claim_delete).setVisible(false);
            menu.findItem(R.id.action_claim_forward).setVisible(false);
            menu.findItem(R.id.action_claim_return).setVisible(false);
            menu.findItem(R.id.action_claim_send).setVisible(false);
        } else {
            mOptionsMenu.findItem(R.id.action_claim_edit).setVisible(mClaim.canUpdate);
            mOptionsMenu.findItem(R.id.action_claim_add_file).setVisible(mClaim.canAttach);
            mOptionsMenu.findItem(R.id.action_claim_add_note).setVisible(mClaim.canAddNote);
            mOptionsMenu.findItem(R.id.action_claim_close).setVisible(mClaim.canClose);
            mOptionsMenu.findItem(R.id.action_claim_delete).setVisible(mClaim.canDelete);
            mOptionsMenu.findItem(R.id.action_claim_forward).setVisible(mClaim.canForward);
            mOptionsMenu.findItem(R.id.action_claim_return).setVisible(mClaim.canReturn);
            mOptionsMenu.findItem(R.id.action_claim_send).setVisible(mClaim.canSend);
        }
        return true;
    }

    private class GetDocumentAsyncTask extends AsyncTask<ClaimHistoryFragment.Attach, Void, File> {
        private ProgressDialog dialog;


        @Override
        protected File doInBackground(ClaimHistoryFragment.Attach... attaches) {
            RestRequest lRequest;
            try {
                lRequest = new RestRequest("docum/");
                lRequest.addHeaderParam("PSESSION", mSession);
                lRequest.addHeaderParam("PDOCRN", String.valueOf(attaches[0].getFileRn()));
                return lRequest.getFile(attaches[0].getFileName());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(ClaimActivity.this);
            dialog.setMessage("Downloading the file, please wait.");
            dialog.show();
        }

        @Override
        protected void onPostExecute(File aFile) {
            try {
                MediaScannerConnection.scanFile(ClaimActivity.this,
                        new String[]{aFile.toString()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("ExternalStorage", "Scanned " + path + ":");
                                Log.i("ExternalStorage", "-> uri=" + uri);
                            }
                        });
                Uri lPath = Uri.fromFile(aFile);
                MimeTypeMap lMimeTypeMap = MimeTypeMap.getSingleton();
                String lMimeType = lMimeTypeMap.getMimeTypeFromExtension(fileExt(aFile.getName()).substring(1));
                Intent lViewIntent = new Intent(Intent.ACTION_VIEW);
                if (lMimeType == null) {
                    lMimeType = "text/plain";
                }

                lViewIntent.setDataAndType(lPath, lMimeType);
                lViewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                startActivity(lViewIntent);
            } catch (ActivityNotFoundException | SecurityException e) {
                e.printStackTrace();
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }
    }

}
