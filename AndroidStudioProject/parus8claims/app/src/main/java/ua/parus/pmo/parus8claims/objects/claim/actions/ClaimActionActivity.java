package ua.parus.pmo.parus8claims.objects.claim.actions;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONObject;

import java.net.ConnectException;
import java.net.MalformedURLException;

import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.gui.ErrorPopup;
import ua.parus.pmo.parus8claims.gui.ProgressWindow;
import ua.parus.pmo.parus8claims.objects.claim.Claim;
import ua.parus.pmo.parus8claims.rest.RestRequest;
import ua.parus.pmo.parus8claims.utils.Constants;


@SuppressWarnings("deprecation")
public class ClaimActionActivity extends ActionBarActivity {


    private static final String REST_POST_METHOD = "POST";
    private static final String REST_PARAM_SESSION = "session";
    private static final String REST_PARAM_RN = "rn";
    private static final String REST_PARAM_DESCRIPTION = "description";
    private static final String REST_PARAM_RELEASE_FOUND = "relfound";
    private static final String REST_PARAM_BUILD_FOUND = "bldfound";
    private static final String REST_PARAM_RELEASE_FIX = "relfix";
    private static final String REST_PARAM_BUILD_FIX = "bldfix";
    private static final String REST_PARAM_UNIT_APP = "app";
    private static final String REST_PARAM_UNIT = "unit";
    private static final String REST_PARAM_UNIT_FUNC = "func";
    private static final String REST_PARAM_PRIORITY = "priority";
    private static final String REST_PARAM_ERROR = "error";
    private static final String REST_PARAM_TYPE = "ptype";
    private static final String CLAIM_TYPE_ADDON = "ДОРАБОТКА";
    private static final String CLAIM_TYPE_REBUKE = "ЗАМЕЧАНИЕ";
    private static final String CLAIM_TYPE_ERROR = "ОШИБКА";
    private static final String REST_NOTE_URL = "claim/addnote/";
    private static final String REST_EDIT_URL = "claim/edit/";
    private static final String REST_ADD_URL = "claim/add/";
    private static final String REST_SEND_URL = "send/";
    private static final String REST_RETURN_URL = "return/";
    private static final String REST_PARAM_NOTE = "note";
    private static final String REST_PARAM_PERSON = "person";
    private static final String REST_FORWARD_URL = "nextpoint/";
    private static final String REST_PARAM_STATE = "stat";
    private Claim claim;
    private int request;
    private String session;
    private ClaimActionActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.instance = this;
        this.claim = (Claim) getIntent().getSerializableExtra(Constants.EXTRA_KEY_CLAIM);
        this.request = getIntent().getIntExtra(Constants.EXTRA_KEY_REQUEST, 0);
        this.session = getIntent().getStringExtra(Constants.EXTRA_KEY_SESSION);
        setContentView(R.layout.activity_claim);
        if (savedInstanceState == null) {
            ActionBar supportActionBar = getSupportActionBar();
            if (supportActionBar != null) {
                if (this.request == Constants.REQUEST_CLAIM_EDIT) {
                    supportActionBar.setTitle(R.string.editing);
                    ClaimEditFragment claimEditFragment = ClaimEditFragment.newInstance(this.claim);
                    placeFragment(claimEditFragment);
                }
                if (this.request == Constants.REQUEST_CLAIM_ADD) {
                    supportActionBar.setTitle(R.string.adding);
                    ClaimAddFragment claimAddFragment = ClaimAddFragment.newInstance();
                    placeFragment(claimAddFragment);
                }
                if (this.request == Constants.REQUEST_CLAIM_NOTE) {
                    supportActionBar.setTitle(R.string.claim_note);
                    ClaimNoteFragment claimNoteFragment = ClaimNoteFragment.newInstance();
                    placeFragment(claimNoteFragment);
                }
                if (this.request == Constants.REQUEST_CLAIM_SEND) {
                    supportActionBar.setTitle(R.string.act_send);
                    ClaimSendFragment claimSendFragment = ClaimSendFragment.newInstance(this.claim, this.session);
                    placeFragment(claimSendFragment);
                }
                if (this.request == Constants.REQUEST_CLAIM_RETURN) {
                    supportActionBar.setTitle(R.string.act_return);
                    ClaimReturnFragment claimReturnFragment = ClaimReturnFragment.newInstance(this.claim, this.session);
                    placeFragment(claimReturnFragment);
                }
                if (this.request == Constants.REQUEST_CLAIM_FORWARD) {
                    supportActionBar.setTitle(R.string.pass_on);
                    ClaimForwardFragment claimForwardFragment =
                            ClaimForwardFragment.newInstance(this.claim, this.session);
                    placeFragment(claimForwardFragment);
                }
            }
        }
    }

    private void placeFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction;
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_claim_action, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    private void addClaim() {
        ClaimAddFragment fragment = (ClaimAddFragment) getSupportFragmentManager()
                .findFragmentById(R.id.container);
        boolean unitNotSet = fragment.holder.unit.getText().toString().isEmpty();
        boolean contentNotSet = fragment.holder.content.getText().toString().isEmpty();
        boolean relNotSet = (fragment.holder.release.getValueString() == null)
                            || fragment.holder.release.getValueString().isEmpty();
        boolean bldNotSet = (fragment.holder.build.getValueString() == null)
                            || fragment.holder.build.getValueString().isEmpty();
        if (unitNotSet || contentNotSet || relNotSet || bldNotSet) return;
        new AddClaimTask().execute(fragment.holder);
    }

    private void editClaim() {
        ClaimEditFragment fragment = (ClaimEditFragment) getSupportFragmentManager()
                .findFragmentById(R.id.container);
        boolean unitNotSet = fragment.holder.unit.getText().toString().isEmpty();
        boolean contentNotSet = fragment.holder.content.getText().toString().isEmpty();
        boolean relNotSet = (fragment.holder.release.getValueString() == null)
                            || fragment.holder.release.getValueString().isEmpty();
        boolean bldNotSet = (fragment.holder.build.getValueString() == null)
                            || fragment.holder.build.getValueString().isEmpty();
        if (unitNotSet || contentNotSet || relNotSet || bldNotSet) return;
        new EditClaimTask().execute(fragment.holder);
    }

    private void finishUpdated() {
        Intent intent = new Intent();
        setResult(Constants.RESULT_CLAIM_UPDATED, intent);
        finish();
    }

    private void addNote() {
        ClaimNoteFragment fragment = (ClaimNoteFragment) getSupportFragmentManager()
                .findFragmentById(R.id.container);
        new AddNoteTask().execute(fragment.note.getText().toString());
    }

    private void sendClaim() {
        ClaimSendFragment fragment = (ClaimSendFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        new SendClaimTask().execute(fragment.holder);
    }

    private void forwardClaim() {
        ClaimForwardFragment fragment =
                (ClaimForwardFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        new ForwardClaimTask().execute(fragment.holder);
    }

    private void returnClaim() {
        ClaimReturnFragment fragment =
                (ClaimReturnFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        new ReturnClaimTask().execute(fragment.holder.note.getText().toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cancel:
                Intent intent = new Intent();
                setResult(Constants.RESULT_CANCEL, intent);
                finish();
                break;
            case R.id.action_ok:
                switch (this.request) {
                    case Constants.REQUEST_CLAIM_EDIT:
                        editClaim();
                        break;
                    case Constants.REQUEST_CLAIM_ADD:
                        addClaim();
                        break;
                    case Constants.REQUEST_CLAIM_NOTE:
                        addNote();
                        break;
                    case Constants.REQUEST_CLAIM_SEND:
                        sendClaim();
                        break;
                    case Constants.REQUEST_CLAIM_RETURN:
                        returnClaim();
                        break;
                    case Constants.REQUEST_CLAIM_FORWARD:
                        forwardClaim();
                        break;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class SendClaimTask extends AsyncTask<ClaimSendFragment.Holder, Void, Integer> {
        String error;
        ProgressWindow pw;

        @Override protected Integer doInBackground(ClaimSendFragment.Holder... holders) {
            try {
                RestRequest restRequest = new RestRequest(REST_SEND_URL, REST_POST_METHOD);
                restRequest.addInParam(REST_PARAM_SESSION, session);
                restRequest.addInParam(REST_PARAM_RN, String.valueOf(claim.rn));
                restRequest.addInParam(
                        REST_PARAM_NOTE, holders[0].note.getText().toString());
                restRequest.addInParam(
                        REST_PARAM_PERSON, holders[0].send.getValueString());
                JSONObject response = restRequest.getJsonContent();
                if (response != null) {
                    error = response.optString(REST_PARAM_ERROR);
                    return TextUtils.isEmpty(error) ? 0 : -1;
                } else {
                    return 0;
                }

            } catch (MalformedURLException | ConnectException e) {
                error = e.getLocalizedMessage();
                e.printStackTrace();
                return -1;
            }
        }

        @Override protected void onPreExecute() {
            pw = new ProgressWindow(instance);
            super.onPreExecute();
        }

        @Override protected void onPostExecute(Integer result) {
            pw.dismiss();
            if (result == -1) {
                new ErrorPopup(ClaimActionActivity.this, null)
                        .showErrorDialog(getString(R.string.error_title), error);
            } else {
                finishUpdated();
            }
            super.onPostExecute(result);
        }
    }

    private class ForwardClaimTask extends AsyncTask<ClaimForwardFragment.Holder, Void, Integer> {
        String error;
        ProgressWindow pw;

        @Override protected Integer doInBackground(ClaimForwardFragment.Holder... holders) {
            ClaimForwardFragment.Holder holder = holders[0];
            try {
                RestRequest restRequest = new RestRequest(REST_FORWARD_URL, REST_POST_METHOD);
                restRequest.addInParam(REST_PARAM_SESSION, session);
                restRequest.addInParam(REST_PARAM_RN, String.valueOf(claim.rn));
                restRequest.addInParam(
                        REST_PARAM_NOTE, holder.note.getText().toString());
                restRequest.addInParam(
                        REST_PARAM_STATE, holder.state.getValueDisplay());
                restRequest.addInParam(
                        REST_PARAM_PERSON, holder.send.getValueString());
                restRequest.addInParam(REST_PARAM_PRIORITY, holder.priority.getText().toString());
                restRequest.addInParam(
                        REST_PARAM_RELEASE_FIX, holder.releaseFix.getValueString());
                restRequest.addInParam(
                        REST_PARAM_BUILD_FIX, holder.buildFix.getValueString());
                JSONObject response = restRequest.getJsonContent();
                if (response != null) {
                    error = response.optString(REST_PARAM_ERROR);
                    return TextUtils.isEmpty(error) ? 0 : -1;
                } else {
                    return 0;
                }
            } catch (MalformedURLException | ConnectException e) {
                error = e.getLocalizedMessage();
                e.printStackTrace();
                return -1;
            }
        }

        @Override protected void onPreExecute() {
            pw = new ProgressWindow(instance);
            super.onPreExecute();
        }

        @Override protected void onPostExecute(Integer result) {
            pw.dismiss();
            if (result == -1) {
                new ErrorPopup(ClaimActionActivity.this, null)
                        .showErrorDialog(getString(R.string.error_title), error);
            } else {
                finishUpdated();
            }
            super.onPostExecute(result);
        }
    }

    private class ReturnClaimTask extends AsyncTask<String, Void, Integer> {
        String error;
        ProgressWindow pw;

        @Override protected Integer doInBackground(String... strings) {
            try {
                RestRequest restRequest = new RestRequest(REST_RETURN_URL, REST_POST_METHOD);
                restRequest.addInParam(REST_PARAM_SESSION, session);
                restRequest.addInParam(REST_PARAM_RN, String.valueOf(claim.rn));
                restRequest.addInParam(
                        REST_PARAM_NOTE, strings[0]);
                JSONObject response = restRequest.getJsonContent();
                if (response != null) {
                    error = response.optString(REST_PARAM_ERROR);
                    return TextUtils.isEmpty(error) ? 0 : -1;
                } else {
                    return 0;
                }
            } catch (MalformedURLException | ConnectException e) {
                error = e.getLocalizedMessage();
                e.printStackTrace();
                return -1;
            }
        }

        @Override protected void onPreExecute() {
            pw = new ProgressWindow(instance);
            super.onPreExecute();
        }

        @Override protected void onPostExecute(Integer result) {
            pw.dismiss();
            if (result == -1) {
                new ErrorPopup(ClaimActionActivity.this, null)
                        .showErrorDialog(getString(R.string.error_title), error);
            } else {
                finishUpdated();
            }
            super.onPostExecute(result);
        }
    }

    private class AddClaimTask extends AsyncTask<ClaimAddFragment.Holder, Void, Long> {
        String error;
        ProgressWindow pw;

        @Override protected Long doInBackground(ClaimAddFragment.Holder... holders) {
            ClaimAddFragment.Holder holder = holders[0];
            try {
                RestRequest restRequest = new RestRequest(REST_ADD_URL, REST_POST_METHOD);
                restRequest.addInParam(REST_PARAM_SESSION, session);
                String claimType = "";
                switch (holder.type.getCheckedRadioButtonId()) {
                    case R.id.radioTypeAddon:
                        claimType = CLAIM_TYPE_ADDON;
                        break;
                    case R.id.radioTypeRebuke:
                        claimType = CLAIM_TYPE_REBUKE;
                        break;
                    case R.id.radioTypeError:
                        claimType = CLAIM_TYPE_ERROR;
                        break;
                }
                restRequest.addInParam(
                        REST_PARAM_TYPE, claimType);
                restRequest.addInParam(
                        REST_PARAM_PRIORITY, holder.priority.getText().toString());
                restRequest.addInParam(
                        REST_PARAM_UNIT_APP, holder.unitApp.getValue());
                restRequest.addInParam(
                        REST_PARAM_UNIT_APP, holder.unitApp.getValue());
                restRequest.addInParam(
                        REST_PARAM_UNIT, holder.unit.getText().toString());
                restRequest.addInParam(
                        REST_PARAM_UNIT_FUNC, holder.unitFunc.getValueString());
                restRequest.addInParam(
                        REST_PARAM_DESCRIPTION, holder.content.getText().toString());
                restRequest.addInParam(
                        REST_PARAM_RELEASE_FOUND, holder.release.getValueString());
                restRequest.addInParam(
                        REST_PARAM_BUILD_FOUND, holder.build.getValueString());
                restRequest.addInParam(
                        REST_PARAM_RELEASE_FIX, holder.releaseFix.getValueString());
                JSONObject response = restRequest.getJsonContent();
                if (response != null) {
                    error = response.optString(REST_PARAM_ERROR);
                    return TextUtils.isEmpty(error) ? response.optLong(REST_PARAM_RN) : (long) -1;
                } else {
                    return null;
                }
            } catch (MalformedURLException | ConnectException e) {
                error = e.getLocalizedMessage();
                e.printStackTrace();
                return (long) -1;
            }
        }

        @Override protected void onPreExecute() {
            pw = new ProgressWindow(instance);
            super.onPreExecute();
        }

        @Override protected void onPostExecute(Long result) {
            pw.dismiss();
            if (result == -1) {
                new ErrorPopup(ClaimActionActivity.this, null)
                        .showErrorDialog(getString(R.string.error_title), error);
            } else {
                Intent intent = new Intent();
                intent.putExtra(Constants.EXTRA_KEY_RN, result);
                setResult(Constants.RESULT_CLAIM_ADDED, intent);
                finish();
            }
            super.onPostExecute(result);
        }
    }

    private class EditClaimTask extends AsyncTask<ClaimEditFragment.Holder, Void, Integer> {
        String error;
        ProgressWindow pw;

        @Override protected Integer doInBackground(ClaimEditFragment.Holder... holders) {
            ClaimEditFragment.Holder holder = holders[0];
            try {
                RestRequest restRequest = new RestRequest(REST_EDIT_URL, REST_POST_METHOD);
                restRequest.addInParam(REST_PARAM_SESSION, session);
                restRequest.addInParam(REST_PARAM_RN, String.valueOf(claim.rn));
                restRequest.addInParam(
                        REST_PARAM_DESCRIPTION, holder.content.getText().toString());
                restRequest.addInParam(
                        REST_PARAM_RELEASE_FOUND, holder.release.getValueString());
                restRequest.addInParam(
                        REST_PARAM_BUILD_FOUND, holder.build.getValueString());
                restRequest.addInParam(
                        REST_PARAM_RELEASE_FIX, holder.releaseFix.getValueString());
                restRequest.addInParam(
                        REST_PARAM_BUILD_FIX, holder.buildFix.getValueString());
                restRequest.addInParam(
                        REST_PARAM_UNIT_APP, holder.unitApp.getValue());
                restRequest.addInParam(
                        REST_PARAM_UNIT, holder.unit.getText().toString());
                restRequest.addInParam(
                        REST_PARAM_UNIT_FUNC, holder.unitFunc.getValueString());
                restRequest.addInParam(
                        REST_PARAM_PRIORITY, holder.priority.getText().toString());
                JSONObject response = restRequest.getJsonContent();
                if (response != null) {
                    error = response.optString(REST_PARAM_ERROR);
                    return TextUtils.isEmpty(error) ? 0 : -1;
                } else {
                    return 0;
                }
            } catch (MalformedURLException | ConnectException e) {
                error = e.getLocalizedMessage();
                e.printStackTrace();
                return -1;
            }
        }

        @Override protected void onPreExecute() {
            pw = new ProgressWindow(instance);
            super.onPreExecute();
        }

        @Override protected void onPostExecute(Integer result) {
            pw.dismiss();
            if (result == -1) {
                new ErrorPopup(ClaimActionActivity.this, null)
                        .showErrorDialog(getString(R.string.error_title), error);
            } else {
                finishUpdated();
            }
            super.onPostExecute(result);
        }
    }

    private class AddNoteTask extends AsyncTask<String, Void, Integer> {
        String error;
        ProgressWindow pw;

        @Override protected Integer doInBackground(String... strings) {
            try {
                RestRequest restRequest = new RestRequest(REST_NOTE_URL, REST_POST_METHOD);
                restRequest.addInParam(REST_PARAM_SESSION, session);
                restRequest.addInParam(REST_PARAM_RN, String.valueOf(claim.rn));
                restRequest.addInParam(
                        REST_PARAM_NOTE, strings[0]);
                JSONObject response = restRequest.getJsonContent();
                if (response != null) {
                    error = response.optString(REST_PARAM_ERROR);
                    return TextUtils.isEmpty(error) ? 0 : -1;
                } else {
                    return 0;
                }
            } catch (MalformedURLException | ConnectException e) {
                error = e.getLocalizedMessage();
                e.printStackTrace();
                return -1;
            }
        }

        @Override protected void onPreExecute() {
            pw = new ProgressWindow(instance);
            super.onPreExecute();
        }

        @Override protected void onPostExecute(Integer result) {
            pw.dismiss();
            if (result == -1) {
                new ErrorPopup(ClaimActionActivity.this, null)
                        .showErrorDialog(getString(R.string.error_title), error);
            } else {
                finishUpdated();
            }
            super.onPostExecute(result);
        }
    }


}
