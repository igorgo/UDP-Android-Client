package ua.parus.pmo.parus8claims.objects.claim;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import ua.parus.pmo.parus8claims.ClaimApplication;
import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.gui.ErrorPopup;
import ua.parus.pmo.parus8claims.objects.claim.actions.ClaimActionActivity;
import ua.parus.pmo.parus8claims.objects.claim.hist.ClaimHistoryFragment;
import ua.parus.pmo.parus8claims.rest.RestRequest;
import ua.parus.pmo.parus8claims.utils.Constants;

@SuppressWarnings("deprecation")
public class ClaimActivity extends ActionBarActivity
        implements
        ClaimHistoryFragment.ClaimHistoryFragmentInterface {

    @SuppressWarnings("unused")
    private static final String TAG = ClaimActivity.class.getSimpleName();
    private static final String REST_PARAM_ERROR = "error";
    private String session;
    private Menu optionsMenu;
    private Claim claim;
    private long rn;
    private int backListPos;
    private boolean hasDocs;
    private boolean needsRefresh = false;
    private boolean needsRefreshParent = false;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.rn = getIntent().getLongExtra(Constants.EXTRA_KEY_RN, 0);
        this.backListPos = getIntent().getIntExtra(Constants.EXTRA_KEY_CLAIM_LIST_POS, -1);
        this.hasDocs = getIntent().getBooleanExtra(Constants.EXTRA_KEY_HAS_DOCS, false);
        setContentView(R.layout.activity_claim);
        this.session = ((ClaimApplication) this.getApplication()).getSessionId();
        this.claim = null;
        this.actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        ClaimHistoryFragment claimHistoryFragment = ClaimHistoryFragment.newInstance(rn, this.session, hasDocs);
        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.container, claimHistoryFragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(Constants.EXTRA_KEY_CLAIM_LIST_POS, needsRefreshParent ? backListPos : -1);
        intent.putExtra(Constants.EXTRA_KEY_CLAIM, claim);
        setResult(Constants.RESULT_CANCEL, intent);
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_claim, menu);
        this.optionsMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_claim_edit:
                startActionActivity(Constants.REQUEST_CLAIM_EDIT);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_claim_send:
                startActionActivity(Constants.REQUEST_CLAIM_SEND);
                break;
            case R.id.action_claim_return:
                startActionActivity(Constants.REQUEST_CLAIM_RETURN);
                break;
            case R.id.action_claim_forward:
                startActionActivity(Constants.REQUEST_CLAIM_FORWARD);
                break;
            case R.id.action_claim_delete:
                doSimpleAction("claim/delete/", R.string.deleting, R.string.delete_confirm,
                        Constants.RESULT_CLAIM_DELETED);
                break;
            case R.id.action_claim_close:
                doSimpleAction("claim/close/", R.string.closing, R.string.close_confirm, Constants.RESULT_CLAIM_DELETED);
                break;
            case R.id.action_claim_add_note:
                startActionActivity(Constants.REQUEST_CLAIM_NOTE);
                break;
            case R.id.action_claim_add_file:
                selectFile();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startActionActivity(int request) {
        Intent action = new Intent(this, ClaimActionActivity.class);
        action.putExtra(Constants.EXTRA_KEY_CLAIM, this.claim);
        action.putExtra(Constants.EXTRA_KEY_REQUEST, request);
        action.putExtra(Constants.EXTRA_KEY_SESSION, session);
        startActivityForResult(action, request);
    }

    private void doSimpleAction(final String url, int titleId, int confirmTextId, final int resultCode) {
        new AlertDialog.Builder(this)
                .setTitle(titleId)
                .setMessage(getString(confirmTextId, claim.number))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(
                        android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                new SampleActionTask().execute(new SimpleAction(url, resultCode));
                            }
                        }
                                  )
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent, Constants.REQUEST_SELECT_FILE);
    }

    @Override
    public void onHistoryLoaded(ClaimHistoryFragment fragment, Claim claim) {
        this.claim = claim;
        this.actionBar.invalidateOptionsMenu();
        invalidateOptionsMenu();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (needsRefresh) {
            ClaimHistoryFragment claimHistoryFragment =
                    ClaimHistoryFragment.newInstance(this.rn, this.session, this.hasDocs);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.container, claimHistoryFragment);
            fragmentTransaction.commit();
        }
        needsRefresh = false;
    }

    @Override
    public void onDocumDownloadRequest(ClaimHistoryFragment.Attach attach) {
        if (attach == null) {
            return;
        }
        new DownloadAttachTask().execute(attach);
    }

    private String fileExtension(String url) {
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String extension = url.substring(url.lastIndexOf("."));
            if (url.contains("%")) {
                extension = extension.substring(0, extension.indexOf("%"));
            }
            if (url.contains("/")) {
                extension = extension.substring(0, extension.indexOf("/"));
            }
            return extension.toLowerCase();
        }
    }

    public String getFullFileName(Uri fileUri) {
        String fileName = null;
        String scheme = fileUri.getScheme();
        if (scheme.equals("file")) {
            fileName = fileUri.getPath();
        } else if (scheme.equals("content")) {
            Cursor cursor = null;
            try {
                String[] projection = {MediaStore.Images.Media.DATA};
                cursor = getContentResolver().query(fileUri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                fileName = cursor.getString(column_index);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return fileName;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (this.claim == null || this.optionsMenu == null) {
            menu.findItem(R.id.action_claim_edit).setVisible(false);
            menu.findItem(R.id.action_claim_add_file).setVisible(false);
            menu.findItem(R.id.action_claim_add_note).setVisible(false);
            menu.findItem(R.id.action_claim_close).setVisible(false);
            menu.findItem(R.id.action_claim_delete).setVisible(false);
            menu.findItem(R.id.action_claim_forward).setVisible(false);
            menu.findItem(R.id.action_claim_return).setVisible(false);
            menu.findItem(R.id.action_claim_send).setVisible(false);
        } else {
            this.optionsMenu.findItem(R.id.action_claim_edit).setVisible(this.claim.canUpdate);
            this.optionsMenu.findItem(R.id.action_claim_add_file).setVisible(this.claim.canAttach);
            this.optionsMenu.findItem(R.id.action_claim_add_note).setVisible(this.claim.canAddNote);
            this.optionsMenu.findItem(R.id.action_claim_close).setVisible(this.claim.canClose);
            this.optionsMenu.findItem(R.id.action_claim_delete).setVisible(this.claim.canDelete);
            this.optionsMenu.findItem(R.id.action_claim_forward).setVisible(this.claim.canForward);
            this.optionsMenu.findItem(R.id.action_claim_return).setVisible(this.claim.canReturn);
            this.optionsMenu.findItem(R.id.action_claim_send).setVisible(this.claim.canSend);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Constants.RESULT_CANCEL) return;
        switch (requestCode) {
            case Constants.REQUEST_CLAIM_EDIT:
            case Constants.REQUEST_CLAIM_NOTE:
            case Constants.REQUEST_CLAIM_SEND:
            case Constants.REQUEST_CLAIM_RETURN:
            case Constants.REQUEST_CLAIM_FORWARD:
                needsRefresh = true;
                needsRefreshParent = true;
                break;
            case Constants.REQUEST_SELECT_FILE:
                if (resultCode == RESULT_OK) {
                    Uri selectedFileUri = data.getData();
                    new UploadAttachTask().execute(selectedFileUri);
                }
                break;
        }
    }

    private class UploadAttachTask extends AsyncTask<Uri,Void,Integer> {
        private ProgressDialog progressDialog;
        private String error;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(ClaimActivity.this);
            progressDialog.setMessage(getString(R.string.uploading_file));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Uri... params) {
            try {
                String fullFileName = getFullFileName(params[0]);
                if (TextUtils.isEmpty(fullFileName)) {
                    return -2;
                }
                File sourceFile = new File(fullFileName);
                String fileName = sourceFile.getName();

                URL url = new URL(RestRequest.BASE_URL + "docum/");

                HttpURLConnection httpConn;
                httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setUseCaches(false);
                httpConn.setDoOutput(true); // indicates POST method
                httpConn.setDoInput(true);


                httpConn.setRequestProperty("Parus-session", session);
                httpConn.setRequestProperty("Parus-rn", String.valueOf(claim.rn));
                httpConn.setRequestProperty("Parus-filename", fileName);
                httpConn.setRequestProperty("Content-Type", URLConnection.guessContentTypeFromName(fileName));

                OutputStream outputStream = httpConn.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);

                FileInputStream inputStream = new FileInputStream(sourceFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
                inputStream.close();
                writer.flush();
                writer.close();

                int status = httpConn.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK) {
                    StringBuilder content = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line);
                    }
                    reader.close();
                    httpConn.disconnect();

                    if (!TextUtils.isEmpty(content.toString())) {
                        JSONObject jsonContent = new JSONObject(content.toString());
                        error = jsonContent.optString(REST_PARAM_ERROR);
                        return TextUtils.isEmpty(error) ? 0 : -1;
                    } else
                    return 0;

                } else {
                    error = "Server returned non-OK status: " + status;
                    return -1;
                }
            } catch (IOException | JSONException e) {
                error = e.getLocalizedMessage();
                e.printStackTrace();
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (result == -1) {
                new ErrorPopup(ClaimActivity.this, null)
                        .showErrorDialog(getString(R.string.error_title), error);
            }
            if (result == 0) {
                claim.hasAttach = true;
                ClaimActivity.this.hasDocs = true;
                needsRefresh = true;
                onPostResume();
            }
        }
    }

    private class SimpleAction {
        String url;
        int resultCode;

        public SimpleAction(String url, int resultCode) {
            this.url = url;
            this.resultCode = resultCode;
        }
    }

    private class SampleActionTask extends AsyncTask<SimpleAction, Void, Integer> {
        private ProgressDialog progressDialog;
        private String error;

        @Override protected void onPreExecute() {
            progressDialog = new ProgressDialog(ClaimActivity.this);
            progressDialog.setMessage(getString(R.string.please_wait));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override protected Integer doInBackground(SimpleAction... actions) {
            try {
                RestRequest restRequest = new RestRequest(actions[0].url, "POST");
                restRequest.addInParam("session", session);
                restRequest.addInParam("rn", String.valueOf(claim.rn));
                JSONObject response = restRequest.getJsonContent();
                if (response != null) {
                    error = response.optString("error");
                    return TextUtils.isEmpty(error) ? actions[0].resultCode : -1;
                } else {
                    return actions[0].resultCode;
                }
            } catch (MalformedURLException | ConnectException e) {
                error = e.getLocalizedMessage();
                e.printStackTrace();
                return -1;
            }
        }

        @Override protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (result == -1) {
                new ErrorPopup(ClaimActivity.this, null)
                        .showErrorDialog(getString(R.string.error_title), error);
            } else {
                Intent intent = new Intent();
                setResult(result, intent);
                finish();
            }
        }
    }

    private class DownloadAttachTask extends AsyncTask<ClaimHistoryFragment.Attach, Void, File> {
        public static final String URL_GET_DOCUM = "docum/";
        public static final String PARAM_SESSION = "session";
        public static final String PARAM_DOCRN = "docrn";
        private ProgressDialog dialog;


        @Override
        protected File doInBackground(ClaimHistoryFragment.Attach... attaches) {
            RestRequest restRequest;
            try {
                restRequest = new RestRequest(URL_GET_DOCUM);
                restRequest.addInParam(PARAM_SESSION, ClaimActivity.this.session);
                restRequest.addInParam(PARAM_DOCRN, String.valueOf(attaches[0].getFileRn()));
                return restRequest.getFile(attaches[0].getFileName());
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
                Uri path = Uri.fromFile(aFile);
                MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                String mimeType = null;
                String extension = fileExtension(aFile.getName());
                if (extension != null) {
                    mimeType = mimeTypeMap.getMimeTypeFromExtension(extension.substring(1));
                }
                Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                if (mimeType == null) {
                    mimeType = "text/plain";
                }
                viewIntent.setDataAndType(path, mimeType);
                viewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                startActivity(viewIntent);
            } catch (ActivityNotFoundException | SecurityException e) {
                e.printStackTrace();
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }
    }
}
