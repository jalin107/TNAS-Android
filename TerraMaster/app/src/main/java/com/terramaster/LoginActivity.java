package com.terramaster;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff.Mode;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;

import com.terramaster.discover.DeviceData;
import com.terramaster.discover.ICCSearching;
import com.terramaster.discover.Receiver;
import com.terramaster.ftp.FTPHelper;
import com.terramaster.task.WorkerThread;
import com.terramaster.utils.AlertUtils;
import com.terramaster.utils.LogM;
import com.terramaster.utils.WiFiUtils;
import com.terramaster.widget.MyButton;
import com.terramaster.widget.MyEditText;
import com.terramaster.widget.MyTextView;

import java.util.ArrayList;

public class LoginActivity extends BaseActivity implements Receiver {
    private EditText edtUsername, edtPassword;
    private boolean isPasswordShow = false;
    private ProgressBar progressBar;
    private MyTextView tvDiscoverStatus;
    private ImageView ivDiscover, ivDiscoverMain;
    private TableLayout tlTerraDevices;
    private MyButton btnLogin,btnRememberMe;
    private Spinner spinTerraDevice;
    private MyTextView tvIP, tvMAC;

    private ArrayList<DeviceData> devicesList = new ArrayList<DeviceData>();
    private ICCSearching iccSearching;
    private OnItemSelectedListener onTerraDeviceSelected = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // TODO Auto-generated method stub
            showDetailsForSelectedDevice();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // TODO Auto-generated method stub

        }
    };

    private void showProgressBar(boolean show, boolean isData) {
        btnLogin.setEnabled(!show && isData);
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        tvDiscoverStatus.setVisibility((show || !isData) ? View.VISIBLE : View.GONE);
        ivDiscoverMain.setVisibility((!show && !isData) ? View.VISIBLE : View.GONE);
        if (show) {
            tvDiscoverStatus.setText(getString(R.string.searching));
        } else if (!isData) {
            tvDiscoverStatus.setText(getString(R.string.no_devices_found));
            /*
            if (WiFiUtils.isWiFiEnbled(getApplicationContext())) {
                AlertUtils.showConfirmAlert(LoginActivity.this, "", getString(R.string.confirm_tcloud_remote_login), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Intent intent = new Intent(LoginActivity.this, CloudWebActivity.class);
                        startActivity(intent);
                    }
                });
            }
            */
        }
        tlTerraDevices.setVisibility((show || !isData) ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initObjects();

        if(prefUtils.isRemember()){
            btnRememberMe.setSelected(true);
            edtUsername.setText(prefUtils.getUsername());
            edtUsername.setSelection(edtUsername.getText().length());
            edtPassword.setText(prefUtils.getPassword());
        }
        startDiscoverDevices(true);
    }

    @Override
    protected void initObjects() {
        // TODO Auto-generated method stub
        super.initObjects();
        setTitle(R.string.login_tcloud);

        btnLogin = (MyButton) findViewById(R.id.btnLogin);
        tlTerraDevices = (TableLayout) findViewById(R.id.tlTerraDevices);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.blue), Mode.SRC_IN);
        tvDiscoverStatus = (MyTextView) findViewById(R.id.tvDiscoverStatus);
        ivDiscover = (ImageView) findViewById(R.id.ivDiscover);
        ivDiscover.setOnClickListener(this);
        ivDiscoverMain = (ImageView) findViewById(R.id.ivDiscoverMain);
        ivDiscoverMain.setOnClickListener(this);
//	 showProgressBar(false, false);

        tvIP = (MyTextView) findViewById(R.id.tvIP);
        tvMAC = (MyTextView) findViewById(R.id.tvMAC);
        spinTerraDevice = (Spinner) findViewById(R.id.spinTerraDevice);
        spinTerraDevice.setOnItemSelectedListener(onTerraDeviceSelected);

        edtUsername = (MyEditText) findViewById(R.id.edtUsername);
        edtPassword = (MyEditText) findViewById(R.id.edtPassword);

        btnRememberMe=(MyButton)findViewById(R.id.btnRememberMe);
        btnRememberMe.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btnDisplayPassword:
                isPasswordShow = !isPasswordShow;
                if (isPasswordShow) {
                    ((MyButton) v).setText(getString(R.string.hide_password));
                    edtPassword.setTransformationMethod(null);
                } else {
                    ((MyButton) v).setText(getString(R.string.display_password));
                    edtPassword.setTransformationMethod(new PasswordTransformationMethod());
                }
                edtPassword.setSelection(edtPassword.getText().length());
                break;
            case R.id.btnRememberMe:
                btnRememberMe.setSelected(!btnRememberMe.isSelected());
                break;

            case R.id.ivDiscover:
            case R.id.ivDiscoverMain:
                startDiscoverDevices(true);
                break;

            case R.id.btnLogin:
                chkAndStartLogin();
                break;
            default:
                super.onClick(v);
                break;
        }

    }

    private void startDiscoverDevices(boolean showDialogIfNeeded) {
        if (!WiFiUtils.isWiFiEnbled(getApplicationContext())) {
            if (showDialogIfNeeded) {
                WiFiUtils.openWiFiSetting(LoginActivity.this);
            }
            showProgressBar(false, false);
            return;
        }
        showProgressBar(true, false);
//        new Discoverer((WifiManager) getSystemService(Context.WIFI_SERVICE), this).start();

        iccSearching = new ICCSearching((WifiManager) getSystemService(Context.WIFI_SERVICE), this);
        iccSearching.start();
    }

    @Override
    public void addAnnouncedServers(final ArrayList<DeviceData> devicesList) {
        // TODO Auto-generated method stub
        this.devicesList.clear();
        this.devicesList.addAll(devicesList);
        LogM.e("addAnnouncedServers " + devicesList.size());

        runOnUiThread(new Runnable() {
            public void run() {
                showProgressBar(false, devicesList.size() > 0);

                showDevices();
            }
        });
//
        if (iccSearching != null) {
            iccSearching.Destory();
        }
    }

    private void showDevices() {
        if (devicesList != null && devicesList.size() > 0) {
            ArrayAdapter<DeviceData> spinnerArrayAdapter = new ArrayAdapter<DeviceData>(this, R.layout.item_spinner, devicesList);
            spinnerArrayAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
            spinTerraDevice.setAdapter(spinnerArrayAdapter);

            showDetailsForSelectedDevice();
        } else {
            tvIP.setText("-");
            tvMAC.setText("-");
        }
    }

    private void showDetailsForSelectedDevice() {
        DeviceData dd = devicesList.get(spinTerraDevice.getSelectedItemPosition());
        tvIP.setText(dd.IPV4);
        tvMAC.setText(dd.MAC);
    }

    private void chkAndStartLogin() {
        // TODO Auto-generated method stub
        if (devicesList == null && devicesList.size() <= 0) {
            return;
        }
        final String username = edtUsername.getText().toString();
        if (username == null || username.length() <= 0) {
            AlertUtils.showSimpleAlert(LoginActivity.this, getString(R.string.e_blank_username));
            edtUsername.requestFocus();
            return;
        }

        final String password = edtPassword.getText().toString();
        if (password == null || password.length() <= 0) {
            AlertUtils.showSimpleAlert(LoginActivity.this, getString(R.string.e_blank_password));
            edtPassword.requestFocus();
            return;
        }

        DeviceData dd = devicesList.get(spinTerraDevice.getSelectedItemPosition());
        doLogin(dd.IPV4, dd.getDeviceService("ftp").port, username, password);

    }

    private void doLogin(final String ftpIP, final int port, final String username, final String password) {
        // TODO Auto-generated method stub
        new WorkerThread(LoginActivity.this, true) {

            @Override
            public String onWorkInBackground() {
                // TODO Auto-generated method stub
                FTPHelper ftpHelper = FTPHelper.getInstance();
                try {
                    ftpHelper.logIn(ftpIP, port, username, password);
                    if (ftpHelper.isLogin()) {
                        boolean isRemember = btnRememberMe.isSelected();
                        prefUtils.setRemember(isRemember);
                        DeviceData deviceData = null;
                        if (devicesList != null && devicesList.size() > 0) {
                            deviceData = devicesList.get(spinTerraDevice.getSelectedItemPosition());
                        }
                        if (deviceData == null) {
                            deviceData = new DeviceData();
                            deviceData.IPV4 = ftpIP;
                            deviceData.addDeviceService("ftp", "", port);
                        }
                        prefUtils.setDeviceData(deviceData, username, password);

                        return "1";
                    } else {
                        return "530";
                    }
                } catch (Exception e) {

                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected synchronized void onWorkFinished(String result) {
                // TODO Auto-generated method stub
                super.onWorkFinished(result);

                if (result != null) {
                    int code = Integer.parseInt(result);
                    if (code == 1) {
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        AlertUtils.showSimpleAlert(LoginActivity.this, getString(R.string.e_login_failed));
                    }
                } else {
                    AlertUtils.showSimpleAlert(LoginActivity.this, getString(R.string.e_unknown));
                }
            }
        }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == WiFiUtils.REQUEST_WIFI_SETTING) {
            startDiscoverDevices(false);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
