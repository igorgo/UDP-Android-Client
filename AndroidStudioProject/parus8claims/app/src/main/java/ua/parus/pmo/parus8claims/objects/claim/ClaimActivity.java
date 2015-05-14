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
import android.os.Handler;
import android.os.Message;
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
import ua.parus.pmo.parus8claims.Intents;
import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.gui.ErrorPopup;
import ua.parus.pmo.parus8claims.objects.claim.actions.ClaimActionActivity;
import ua.parus.pmo.parus8claims.objects.claim.hist.ClaimHistoryFragment;
import ua.parus.pmo.parus8claims.rest.RestRequest;


@SuppressWarnings("deprecation")
public class ClaimActivity extends ActionBarActivity
        implements
        ClaimHistoryFragment.ClaimHistoryFragmentInterface {

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
    private ProgressDialog loadDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.rn = getIntent().getLongExtra(Intents.EXTRA_KEY_RN, 0);
        this.backListPos = getIntent().getIntExtra(Intents.EXTRA_KEY_CLAIM_LIST_POS, -1);
        this.hasDocs = getIntent().getBooleanExtra(Intents.EXTRA_KEY_HAS_DOCS, false);
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
        intent.putExtra(Intents.EXTRA_KEY_CLAIM_LIST_POS, needsRefreshParent ? backListPos : -1);
        intent.putExtra(Intents.EXTRA_KEY_CLAIM, claim);
        setResult(Intents.RESULT_CANCEL, intent);
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
        int id = item.getItemId();
        if (id == R.id.action_claim_edit) {
            startActionActivity(Intents.REQUEST_CLAIM_EDIT);
            return true;
        }
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (id == R.id.action_claim_send) {
            startActionActivity(Intents.REQUEST_CLAIM_SEND);
            return true;
        }
        if (id == R.id.action_claim_return) {
            startActionActivity(Intents.REQUEST_CLAIM_RETURN);
            return true;
        }
        if (id == R.id.action_claim_forward) {
            startActionActivity(Intents.REQUEST_CLAIM_FORWARD);
            return true;
        }
        if (id == R.id.action_claim_delete) {
            doSimpleAction("claim/delete/", R.string.deleting, R.string.delete_confirm, Intents.RESULT_CLAIM_DELETED);
            return true;
        }
        if (id == R.id.action_claim_close) {
            doSimpleAction("claim/close/", R.string.closing, R.string.close_confirm, Intents.RESULT_CLAIM_DELETED);
            return true;
        }
        if (id == R.id.action_claim_add_note) {
            startActionActivity(Intents.REQUEST_CLAIM_NOTE);
            return true;
        }
        if (id == R.id.action_claim_add_file) {
            selectFile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startActionActivity(int request) {
        Intent action = new Intent(this, ClaimActionActivity.class);
        action.putExtra(Intents.EXTRA_KEY_CLAIM, this.claim);
        action.putExtra(Intents.EXTRA_KEY_REQUEST, request);
        action.putExtra(Intents.EXTRA_KEY_SESSION, session);
        startActivityForResult(action, request);
    }

    private void doSimpleAction(final String url, int titleId, int confirmTextId, @SuppressWarnings(
            "SameParameterValue") final int resultCode) {
        new AlertDialog.Builder(this)
                .setTitle(titleId)
                .setMessage(getString(confirmTextId, claim.number))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(
                        android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    RestRequest restRequest = new RestRequest(url, "POST");
                                    restRequest.addInParam("session", session);
                                    restRequest.addInParam("rn", String.valueOf(claim.rn));
                                    JSONObject response = restRequest.getJsonContent();
                                    if (response != null) {
                                        String error = response.optString("error");
                                        if (error != null && !error.isEmpty()) {
                                            dialog.dismiss();
                                            new ErrorPopup(ClaimActivity.this, null)
                                                    .showErrorDialog(getString(R.string.error_title), error);
                                            return;
                                        }
                                    }
                                    dialog.dismiss();
                                    Intent intent = new Intent();
                                    setResult(resultCode, intent);
                                    finish();
                                } catch (MalformedURLException | ConnectException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                                  )
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent, Intents.REQUEST_SELECT_FILE);
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
        Log.i(TAG, "DocumDownloadRequest: Rn=" + String.valueOf(attach.getFileRn()) + "; FileName=" +
                   attach.getFileName());
        new GetDocumentAsyncTask().execute(attach);
    }

    private String fileExtention(String url) {
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String extention = url.substring(url.lastIndexOf("."));
            if (url.contains("%")) {
                extention = extention.substring(0, extention.indexOf("%"));
            }
            if (url.contains("/")) {
                extention = extention.substring(0, extention.indexOf("/"));
            }
            return extention.toLowerCase();
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

    private void uploadAttach(final Uri fileUri) throws IOException, JSONException {
        loadDialog = new ProgressDialog(this);
        loadDialog.setIndeterminate(true);
        loadDialog.setMessage(getString(R.string.uploading_file));
        loadDialog.setTitle(getString(R.string.please_wait));
        loadDialog.show();

        final Handler handler = new Handler(new Handler.Callback() {
            @Override public boolean handleMessage(Message msg) {
                loadDialog.dismiss();
                if (msg.what == 0) {
                    Bundle bundle = msg.getData();
                    new ErrorPopup(ClaimActivity.this, null).showErrorDialog(getString(R.string.error_title),
                            bundle.getString(REST_PARAM_ERROR));
                }
                onPostResume();
                return false;
            }
        });


        Runnable upload = new Runnable() {
            @Override public void run() {
                try {
                    String fullFileName = getFullFileName(fileUri);
                    File sourceFile = new File(fullFileName);
                    if (TextUtils.isEmpty(fullFileName)) {
                        return;
                    }
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
                            if (jsonContent.optString(REST_PARAM_ERROR) != null &&
                                !jsonContent.optString(REST_PARAM_ERROR).isEmpty()) {
                                Bundle b = new Bundle(1);
                                b.putString(REST_PARAM_ERROR, jsonContent.optString(REST_PARAM_ERROR));
                                Message msg = handler.obtainMessage();
                                msg.what = 0;
                                msg.setData(b);
                                handler.sendMessage(msg);
                                return;
                            }
                        }
                        claim.hasAttach = true;
                        ClaimActivity.this.hasDocs = true;
                        needsRefresh = true;
                    } else {
                        throw new IOException("Server returned non-OK status: " + status);
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                } finally {
                    handler.sendEmptyMessage(1);
                }
            }
        };
        new Thread(upload).start();
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
        Log.i(TAG, "onActivityResult{\n\trequestCode: " + requestCode + "\n\tresultCode: " + resultCode + "\n\tdata: " +
                   data + "\n}");
        if (resultCode == Intents.RESULT_CANCEL) return;
        switch (requestCode) {
            case Intents.REQUEST_CLAIM_EDIT:
            case Intents.REQUEST_CLAIM_NOTE:
            case Intents.REQUEST_CLAIM_SEND:
            case Intents.REQUEST_CLAIM_RETURN:
            case Intents.REQUEST_CLAIM_FORWARD:
                Log.i(TAG, "claim updated");
                needsRefresh = true;
                needsRefreshParent = true;
                break;
            case Intents.REQUEST_SELECT_FILE:
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "FileSelected");
                    Uri selectedFileUri = data.getData();
                    try {
                        uploadAttach(selectedFileUri);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private class GetDocumentAsyncTask extends AsyncTask<ClaimHistoryFragment.Attach, Void, File> {
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
                String extention = fileExtention(aFile.getName());
                if (extention != null) {
                    mimeType = mimeTypeMap.getMimeTypeFromExtension(extention.substring(1));
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
