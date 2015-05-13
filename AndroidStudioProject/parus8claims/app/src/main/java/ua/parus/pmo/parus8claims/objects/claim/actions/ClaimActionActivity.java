package ua.parus.pmo.parus8claims.objects.claim.actions;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONObject;

import java.net.ConnectException;
import java.net.MalformedURLException;

import ua.parus.pmo.parus8claims.ClaimApplication;
import ua.parus.pmo.parus8claims.Intents;
import ua.parus.pmo.parus8claims.R;
import ua.parus.pmo.parus8claims.gui.ErrorPopup;
import ua.parus.pmo.parus8claims.objects.claim.Claim;
import ua.parus.pmo.parus8claims.rest.RestRequest;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.claim = (Claim) getIntent().getSerializableExtra(Intents.EXTRA_KEY_CLAIM);
        this.request = getIntent().getIntExtra(Intents.EXTRA_KEY_REQUEST, 0);
        this.session = getIntent().getStringExtra(Intents.EXTRA_KEY_SESSION);
        setContentView(R.layout.activity_claim);
        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction;
            ActionBar supportActionBar = getSupportActionBar();
            if (supportActionBar != null) {
                if (this.request == Intents.REQUEST_CLAIM_EDIT) {
                    supportActionBar.setTitle(R.string.editing);
                    ClaimEditFragment claimEditFragment = ClaimEditFragment.newInstance(this.claim);
                    placeFragment(claimEditFragment);
                }
                if (this.request == Intents.REQUEST_CLAIM_ADD) {
                    supportActionBar.setTitle(R.string.adding);
                    ClaimAddFragment claimAddFragment = ClaimAddFragment.newInstance();
                    placeFragment(claimAddFragment);
                }
                if (this.request == Intents.REQUEST_CLAIM_NOTE) {
                    supportActionBar.setTitle(R.string.claim_note);
                    ClaimNoteFragment claimNoteFragment = ClaimNoteFragment.newInstance();
                    placeFragment(claimNoteFragment);
                }
                if (this.request == Intents.REQUEST_CLAIM_SEND) {
                    supportActionBar.setTitle(R.string.act_send);
                    ClaimSendFragment claimSendFragment = ClaimSendFragment.newInstance(this.claim, this.session);
                    placeFragment(claimSendFragment);
                }
                if (this.request == Intents.REQUEST_CLAIM_RETURN) {
                    supportActionBar.setTitle(R.string.act_return);
                    ClaimReturnFragment claimReturnFragment = ClaimReturnFragment.newInstance(this.claim, this.session);
                    placeFragment(claimReturnFragment);
                }
                if (this.request == Intents.REQUEST_CLAIM_FORWARD) {
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
        //Intent intent = new Intent();
        //setResult(Intents.RESULT_CANCEL, intent);
        //super.onBackPressed();
    }

    private void addClaim() {
        ClaimAddFragment fragment = (ClaimAddFragment) getSupportFragmentManager()
                .findFragmentById(R.id.container);
        Long claimRn = null;
        boolean unitNotSet = fragment.holder.unit.getText().toString().isEmpty();
        boolean contentNotSet = fragment.holder.content.getText().toString().isEmpty();
        boolean relNotSet = (fragment.holder.release.getValueString() == null)
                            || fragment.holder.release.getValueString().isEmpty();
        boolean bldNotSet = (fragment.holder.build.getValueString() == null)
                            || fragment.holder.build.getValueString().isEmpty();
        if (unitNotSet || contentNotSet || relNotSet || bldNotSet) return;
        try {
            RestRequest restRequest = new RestRequest(REST_ADD_URL, REST_POST_METHOD);
            restRequest.addInParam(REST_PARAM_SESSION, session);
            String claimType = "";
            switch (fragment.holder.type.getCheckedRadioButtonId()) {
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
                    REST_PARAM_PRIORITY, fragment.holder.priority.getText().toString());
            restRequest.addInParam(
                    REST_PARAM_UNIT_APP, fragment.holder.unitApp.getValue());
            restRequest.addInParam(
                    REST_PARAM_UNIT_APP, fragment.holder.unitApp.getValue());
            restRequest.addInParam(
                    REST_PARAM_UNIT, fragment.holder.unit.getText().toString());
            restRequest.addInParam(
                    REST_PARAM_UNIT_FUNC, fragment.holder.unitFunc.getValueString());
            restRequest.addInParam(
                    REST_PARAM_DESCRIPTION, fragment.holder.content.getText().toString());
            restRequest.addInParam(
                    REST_PARAM_RELEASE_FOUND, fragment.holder.release.getValueString());
            restRequest.addInParam(
                    REST_PARAM_BUILD_FOUND, fragment.holder.build.getValueString());
            restRequest.addInParam(
                    REST_PARAM_RELEASE_FIX, fragment.holder.releaseFix.getValueString());
            JSONObject response = restRequest.getJsonContent();
            if (response != null) {
                if (response.optString(REST_PARAM_ERROR) != null && !response.optString(REST_PARAM_ERROR).isEmpty()) {
                    new ErrorPopup(this, null)
                            .showErrorDialog(getString(R.string.error_title), response.optString(REST_PARAM_ERROR));
                    return;
                } else {
                    claimRn = response.optLong(REST_PARAM_RN);
                }
            }
        } catch (MalformedURLException | ConnectException e) {
            e.printStackTrace();
        }
        if (claimRn != null) {
            Intent intent = new Intent();
            intent.putExtra(Intents.EXTRA_KEY_RN, claimRn);
            setResult(Intents.RESULT_CLAIM_ADDED, intent);
            finish();
        }

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
        try {
            RestRequest restRequest = new RestRequest(REST_EDIT_URL, REST_POST_METHOD);
            restRequest.addInParam(REST_PARAM_SESSION, session);
            restRequest.addInParam(REST_PARAM_RN, String.valueOf(claim.rn));
            restRequest.addInParam(
                    REST_PARAM_DESCRIPTION, fragment.holder.content.getText().toString());
            restRequest.addInParam(
                    REST_PARAM_RELEASE_FOUND, fragment.holder.release.getValueString());
            restRequest.addInParam(
                    REST_PARAM_BUILD_FOUND, fragment.holder.build.getValueString());
            restRequest.addInParam(
                    REST_PARAM_RELEASE_FIX, fragment.holder.releaseFix.getValueString());
            restRequest.addInParam(
                    REST_PARAM_BUILD_FIX, fragment.holder.buildFix.getValueString());
            restRequest.addInParam(
                    REST_PARAM_UNIT_APP, fragment.holder.unitApp.getValue());
            restRequest.addInParam(
                    REST_PARAM_UNIT, fragment.holder.unit.getText().toString());
            restRequest.addInParam(
                    REST_PARAM_UNIT_FUNC, fragment.holder.unitFunc.getValueString());
            restRequest.addInParam(
                    REST_PARAM_PRIORITY, fragment.holder.priority.getText().toString());
            if (doRestForError(restRequest)) return;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        finishUpdated();

    }

    private void finishUpdated() {
        Intent intent = new Intent();
        setResult(Intents.RESULT_CLAIM_UPDATED, intent);
        finish();
    }

    private boolean doRestForError(RestRequest restRequest) {
        JSONObject response = null;
        try {
            response = restRequest.getJsonContent();
        } catch (ConnectException e) {
            e.printStackTrace();
            response = null;
        }
        if (response != null) {
            if (response.optString(REST_PARAM_ERROR) != null && !response.optString(REST_PARAM_ERROR).isEmpty()) {
                new ErrorPopup(this, null)
                        .showErrorDialog(getString(R.string.error_title), response.optString(REST_PARAM_ERROR));
                return true;
            }
        }
        return false;
    }

    private void addNote() {
        ClaimNoteFragment fragment = (ClaimNoteFragment) getSupportFragmentManager()
                .findFragmentById(R.id.container);
        try {
            RestRequest restRequest = new RestRequest(REST_NOTE_URL, REST_POST_METHOD);
            restRequest.addInParam(REST_PARAM_SESSION, session);
            restRequest.addInParam(REST_PARAM_RN, String.valueOf(claim.rn));
            restRequest.addInParam(
                    REST_PARAM_NOTE, fragment.note.getText().toString());
            if (doRestForError(restRequest)) return;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        finishUpdated();
    }

    private void sendClaim() {
        ClaimSendFragment fragment = (ClaimSendFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        try {
            RestRequest restRequest = new RestRequest(REST_SEND_URL, REST_POST_METHOD);
            restRequest.addInParam(REST_PARAM_SESSION, session);
            restRequest.addInParam(REST_PARAM_RN, String.valueOf(claim.rn));
            restRequest.addInParam(
                    REST_PARAM_NOTE, fragment.holder.note.getText().toString());
            restRequest.addInParam(
                    REST_PARAM_PERSON, fragment.holder.send.getValueString());
            if (doRestForError(restRequest)) return;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        finishUpdated();
    }

    private void forwardClaim() {
        ClaimForwardFragment fragment =
                (ClaimForwardFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        try {
            RestRequest restRequest = new RestRequest(REST_FORWARD_URL, REST_POST_METHOD);
            restRequest.addInParam(REST_PARAM_SESSION, session);
            restRequest.addInParam(REST_PARAM_RN, String.valueOf(claim.rn));
            restRequest.addInParam(
                    REST_PARAM_NOTE, fragment.holder.note.getText().toString());
            restRequest.addInParam(
                    REST_PARAM_STATE, fragment.holder.state.getValueDisplay());
            restRequest.addInParam(
                    REST_PARAM_PERSON, fragment.holder.send.getValueString());
            restRequest.addInParam(REST_PARAM_PRIORITY, fragment.holder.priority.getText().toString());
            restRequest.addInParam(
                    REST_PARAM_RELEASE_FIX, fragment.holder.releaseFix.getValueString());
            restRequest.addInParam(
                    REST_PARAM_BUILD_FIX, fragment.holder.buildFix.getValueString());
            if (doRestForError(restRequest)) return;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        finishUpdated();
    }


    private void returnClaim() {
        ClaimReturnFragment fragment =
                (ClaimReturnFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        try {
            RestRequest restRequest = new RestRequest(REST_RETURN_URL, REST_POST_METHOD);
            restRequest.addInParam(REST_PARAM_SESSION, session);
            restRequest.addInParam(REST_PARAM_RN, String.valueOf(claim.rn));
            restRequest.addInParam(
                    REST_PARAM_NOTE, fragment.holder.note.getText().toString());
            if (doRestForError(restRequest)) return;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        finishUpdated();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_cancel) {
            Intent intent = new Intent();
            setResult(Intents.RESULT_CANCEL, intent);
            finish();
            return true;
        }
        if (id == R.id.action_ok) {
            if (this.request == Intents.REQUEST_CLAIM_EDIT) {
                editClaim();
                return true;
            }
            if (this.request == Intents.REQUEST_CLAIM_ADD) {
                addClaim();
                return true;
            }
            if (this.request == Intents.REQUEST_CLAIM_NOTE) {
                addNote();
                return true;
            }
            if (this.request == Intents.REQUEST_CLAIM_SEND) {
                sendClaim();
                return true;
            }
            if (this.request == Intents.REQUEST_CLAIM_RETURN) {
                returnClaim();
                return true;
            }
            if (this.request == Intents.REQUEST_CLAIM_FORWARD) {
                forwardClaim();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }


}
