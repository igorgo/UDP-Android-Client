package ua.parus.pmo.parus8claims;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.security.PublicKey;

import ua.parus.pmo.parus8claims.om.claim.Claim;


public class ClaimActionActivity extends ActionBarActivity implements ClaimEditFragment.OnFragmentInteractionListener {

    public static final String EXTRA_KEY_CLAIM = "claim";
    public static final String EXTRA_KEY_REQUEST = "request";

    public static final int REQUEST_EDIT=701;

    private Claim mClaim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClaim = (Claim) getIntent().getSerializableExtra(EXTRA_KEY_CLAIM);
        int lRequest = getIntent().getIntExtra(EXTRA_KEY_REQUEST,0);
        setContentView(R.layout.activity_claim);
        if (savedInstanceState == null) {
            if (lRequest == REQUEST_EDIT) {
                ClaimEditFragment lEditFrag = ClaimEditFragment.newInstance(mClaim);
                FragmentTransaction lFt = getSupportFragmentManager().beginTransaction();
                lFt.add(R.id.container, lEditFrag);
                lFt.commit();
            }

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_claim_action, menu);
        //TODO: Edit menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //TODO: Handle menu selection
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
