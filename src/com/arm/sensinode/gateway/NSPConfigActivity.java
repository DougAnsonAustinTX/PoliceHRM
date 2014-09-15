package com.arm.sensinode.gateway;

import com.sensinode.coap.CoapConstants;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import no.nordicsemi.android.nrftoolbox.R;

/**
 * MainActivity class
 * Responsible for displaying the GUI and for initializing the connection information.
 * Uses the stored preferences from a previous connection or saves new preferences.
 * It is also the application entry-point, responsible for starting the Sensinode service
 * or stopping it, as demanded.
 *
 * @author Dragos Donici <Dragos.Donici@arm.com>
 * @see com.arm.sensinode.gateway.SensinodeService
 */ 
public class NSPConfigActivity extends Activity {
    private Switch status_switch;

    // Connection properties
    private String server_address = null;
    private int server_port = -1;
    private String endpoint_id = null;
    private String server_domain = null;
    
    private boolean update_switch_only = false;

    // EditText boxes on the GUI for the connection properties
    private EditText server_address_box;
    private EditText server_port_box;
    private EditText server_domain_box;
    private EditText endpoint_id_box;

    private SharedPreferences preferences;

    void initializeServiceStatus() {
      boolean is_running = preferences.getBoolean("service_enabled",false);
      if (!is_running) {
          preferences.edit().putBoolean("service_enabled", false);
          preferences.edit().commit();
      }
    }
    
    void updateServiceStatus(boolean enabled) {
      preferences.edit().putBoolean("service_enabled",enabled);
      preferences.edit().commit();
    }
    
    boolean serviceIsRunning() { return preferences.getBoolean("service_enabled",false); }
    
    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensinode_config);

        // Get preferences
        preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        
        // initialize the service status
        initializeServiceStatus();

        // Text for the switch buttons
        status_switch = (Switch) findViewById(R.id.switch1);
        if (status_switch != null) {
            status_switch.setTextOff(getString(R.string.switch_off));
            status_switch.setTextOn(getString(R.string.switch_on));
        }

        if (status_switch != null) {
            // Implement a listener for the state of the switch
            status_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (update_switch_only == false) {
                      if(isChecked && !serviceIsRunning()) {
                          // Start the service when the switch is ON
                          Toast.makeText(getApplicationContext(), "Enabled the MDS service.", Toast.LENGTH_SHORT).show();
                          Intent service = new Intent(getApplicationContext(), SensinodeService.class);
                          service.putExtra("enabled", true);
                          getApplicationContext().startService(service);
                          updateServiceStatus(true);
                      } else if (isChecked && serviceIsRunning()) {
                        Toast.makeText(getApplicationContext(), "MDS Service Already Running.", Toast.LENGTH_SHORT).show();
                        buttonView.setChecked(true);
                      }
                      else if (!isChecked && !serviceIsRunning()) {
                        Toast.makeText(getApplicationContext(), "MDS Service Already Shutdown.", Toast.LENGTH_SHORT).show();
                        buttonView.setChecked(false);
                      }
                      else {
                          // Stop the service when the switch is OFF
                          Toast.makeText(getApplicationContext(), "Disabled the MDS service.", Toast.LENGTH_SHORT).show();
                          Intent service = new Intent(getApplicationContext(), SensinodeService.class);
                          service.putExtra("enabled", false);
                          getApplicationContext().startService(service);
                          updateServiceStatus(false);
                      }
                    }
                    update_switch_only = false;
                }
            });
        }

        // Get each of the boxes for the connection parameters and add listeners
        // Validate the information and update the state of the switch accordingly
        server_address_box = (EditText) findViewById(R.id.server_address_box);
        server_address_box.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                String input;
                boolean valid = false;
                if(actionId == EditorInfo.IME_ACTION_DONE)
                {
                    input= v.getText().toString();
                    preferences.edit().putString("server_address", input).commit();
                    readPreferences(preferences);
                    valid = validatePreferences();
                    if (valid) status_switch.setEnabled(true);
                    else status_switch.setEnabled(false);
                }
                return valid;
            }
        });
        server_address_box.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                String input;
                EditText editText;

                if(!hasFocus)
                {
                    editText= (EditText) v;
                    input = editText.getText().toString();
                    preferences.edit().putString("server_address", input).commit();
                    readPreferences(preferences);
                    boolean valid = validatePreferences();
                    if (valid) status_switch.setEnabled(true);
                    else status_switch.setEnabled(false);
                }
            }
        });

        server_port_box = (EditText) findViewById(R.id.server_port_box);
        server_port_box.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                String input;
                boolean valid = false;
                if(actionId == EditorInfo.IME_ACTION_DONE)
                {
                    input= v.getText().toString();
                    preferences.edit().putInt("server_port", Integer.parseInt(input)).commit();
                    readPreferences(preferences);
                    valid = validatePreferences();
                    if (valid) status_switch.setEnabled(true);
                    else status_switch.setEnabled(false);
                }
                return valid;
            }
        });
        server_port_box.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                String input;
                EditText editText;

                if(!hasFocus)
                {
                	editText= (EditText) v;
                    input = editText.getText().toString();
                    int port = CoapConstants.DEFAULT_PORT;
                	try {
                		port = Integer.parseInt(input);
                	}
                	catch(Exception ex) { ; }
                    preferences.edit().putInt("server_port", port).commit();
                    readPreferences(preferences);
                    boolean valid = validatePreferences();
                    if (valid) status_switch.setEnabled(true);
                    else status_switch.setEnabled(false);
                }
            }
        });

        server_domain_box = (EditText) findViewById(R.id.server_domain_box);
        server_domain_box.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                String input;
                boolean valid = false;
                if(actionId == EditorInfo.IME_ACTION_DONE)
                {
                    input = v.getText().toString();
                    preferences.edit().putString("server_domain", input).commit();
                    readPreferences(preferences);
                    valid = validatePreferences();
                    if (valid) status_switch.setEnabled(true);
                    else status_switch.setEnabled(false);
                }
                return valid;
            }
        });
        server_domain_box.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                String input;
                EditText editText;

                if(!hasFocus)
                {
                    editText = (EditText) v;
                    input = editText.getText().toString();
                    preferences.edit().putString("server_domain", input).commit();
                    readPreferences(preferences);
                    boolean valid = validatePreferences();
                    if (valid) status_switch.setEnabled(true);
                    else status_switch.setEnabled(false);
                }
            }
        });
        
        endpoint_id_box = (EditText) findViewById(R.id.endpoint_id_box);
        endpoint_id_box.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                String input;
                boolean valid = false;
                if(actionId == EditorInfo.IME_ACTION_DONE)
                {
                    input = v.getText().toString();
                    preferences.edit().putString("endpoint_id", input).commit();
                    readPreferences(preferences);
                    valid = validatePreferences();
                    if (valid) status_switch.setEnabled(true);
                    else status_switch.setEnabled(false);
                }
                return valid;
            }
        });
        endpoint_id_box.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                String input;
                EditText editText;

                if(!hasFocus)
                {
                    editText = (EditText) v;
                    input = editText.getText().toString();
                    preferences.edit().putString("endpoint_id", input).commit();
                    readPreferences(preferences);
                    boolean valid = validatePreferences();
                    if (valid) status_switch.setEnabled(true);
                    else status_switch.setEnabled(false);
                }
            }
        });

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        
        // keep the screen saver off
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * This method does a very basic validation of the provided connection information.
     *
     * @return boolean
     *          Contains the result of the validation i.e. are the preferences valid or not.
     */
    private boolean validatePreferences() {
        if (server_address == null || server_address.isEmpty()) {
            server_address_box.setText(SensinodeService.DEFAULT_MDS_IPADDRESS);
            return false;
        }
        if (server_port <= 0) {
            server_port_box.setText(SensinodeService.DEFAULT_MDS_COAP_PORT);
            return false;
        }

        if (server_domain == null || server_domain.isEmpty()) {
            server_domain_box.setText(SensinodeService.DEFAULT_MDS_DOMAIN);
            return false;
        }

        if (endpoint_id == null || endpoint_id.isEmpty()) {
          endpoint_id_box.setText(SensinodeService.DEFAULT_ENDPOINT_NAME);
          return false;
        }
        
        return true;
    }

    /**
     * Read the preferences and populate the values on the EditText boxes
     *
     * @param prefs
     *          Which preferences to use.
     */
    private void readPreferences(SharedPreferences prefs) {
        server_address = prefs.getString("server_address", SensinodeService.DEFAULT_MDS_IPADDRESS);
        server_port = prefs.getInt("server_port", SensinodeService.DEFAULT_MDS_COAP_PORT);
        server_domain = prefs.getString("server_domain", SensinodeService.DEFAULT_MDS_DOMAIN);
        endpoint_id = prefs.getString("endpoint_id",SensinodeService.DEFAULT_ENDPOINT_NAME);

        server_address_box = (EditText) findViewById(R.id.server_address_box);
        server_address_box.setText(server_address);

        server_port_box = (EditText) findViewById(R.id.server_port_box);
        server_port_box.setText(String.valueOf(server_port));

        server_domain_box = (EditText) findViewById(R.id.server_domain_box);
        server_domain_box.setText(server_domain);

        endpoint_id_box = (EditText) findViewById(R.id.endpoint_id_box);
        endpoint_id_box.setText(endpoint_id);
    }
    
    // save the preferences
    private void savePreferences(SharedPreferences prefs) {
      Editor editor = prefs.edit();
      if (server_address != null && server_address.isEmpty() == false) editor.putString("server_address", server_address);
      if (server_port > 0) editor.putInt("server_port",server_port);
      if (server_domain != null && server_domain.isEmpty() == false) editor.putString("server_domain", server_domain);
      if (endpoint_id != null && endpoint_id.isEmpty() == false) editor.putString("endpoint_id", endpoint_id);
      editor.commit();
    }

    /**
     * Gets called when the activity has resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();

        readPreferences(preferences);
        savePreferences(preferences);
        boolean valid = validatePreferences();
        
        if (valid) {
            status_switch.setEnabled(true);
            update_switch_only = true;
            if (preferences.getBoolean("service_enabled",true)) {
                status_switch.setChecked(true);
                // start the monitoring service
               }
            else {
            	status_switch.setChecked(false);
            	// stop the monitoring service
             }
        }
        else {
            status_switch.setEnabled(false);
        }
        update_switch_only = false;
    }

    /**
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
}
