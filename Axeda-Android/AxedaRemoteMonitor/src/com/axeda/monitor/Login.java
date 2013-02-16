package com.axeda.monitor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import com.axeda.monitor.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity {

	public EditText etUsername;
	public EditText etPassword;
	public EditText etURL;
	private Button btnCancel;
	public String username;
	public String password;
	public String sURLHost;
	public CheckBox rememberUsername;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login);

		etUsername = (EditText) findViewById(R.id.txtUserName);
		etURL = (EditText) findViewById(R.id.etURL);
		etPassword = (EditText) findViewById(R.id.txtPassword);
		btnCancel = (Button) findViewById(R.id.but_cancel);

		checkUserName();

		Button login = (Button) findViewById(R.id.but_login);

		login.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) throws IllegalArgumentException {

				String sUserName = etUsername.getText().toString();
				String sPassword = etPassword.getText().toString();

				try {
					getURL();
				} catch (IOException e3) {
					e3.printStackTrace();
				}

				CheckBox checkBox = (CheckBox) findViewById(R.id.chkRememberPassword);
				sUserName = etUsername.getText().toString();
				if (checkBox.isChecked()) {
					FileOutputStream fos = null;

					try {
						fos = openFileOutput("usercredential.txt",
								Context.MODE_PRIVATE);
					} catch (FileNotFoundException e2) {
						e2.printStackTrace();
					}
					try {
						fos.write(sUserName.getBytes());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
				AxedaMonitor app = (AxedaMonitor) getApplication();
				app.setURL(sURLHost);
				boolean ok = app.login(sUserName, sPassword);
				if (!ok) {
					Toast.makeText(
							getApplicationContext(),
							"Login failed. Username and/or password doesn't match. Please retry!",
							Toast.LENGTH_LONG).show();

				} else {
					Intent i = new Intent(Login.this, Main.class);
					startActivity(i);
				}
			}

			private String getURL() throws IOException {
				StringBuffer strContent = new StringBuffer("");
				int ch;
				try {
					FileInputStream fos = openFileInput("platformURL.txt");
					try {
						while ((ch = fos.read()) != -1)
							strContent.append((char) ch);

						sURLHost = strContent.toString();
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

				} catch (FileNotFoundException e) {
					FileOutputStream fos = null;
					fos = openFileOutput("platformURL.txt",
							Context.MODE_PRIVATE);
					fos.close();
				}
				return sURLHost;
			}
		});

		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		Button advset = (Button) findViewById(R.id.but_advanced);
		advset.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setContentView(R.layout.advsettings);
				etURL = (EditText) findViewById(R.id.etURL);
				try {
					checkReadURL();
				} catch (IOException e3) {
					e3.printStackTrace();
				}
				Button bas = (Button) findViewById(R.id.but_adv_save);
				bas.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						writeURL();
					}
				});

				Button cancel = (Button) findViewById(R.id.but_cancel);
				cancel.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(Login.this, Login.class));
					}
				});
			}
		});
	}

	private void checkUserName() {

		StringBuffer strContent = new StringBuffer("");
		int ch;
		try {
			FileInputStream fos = openFileInput("usercredential.txt");
			try {

				while ((ch = fos.read()) != -1)
					strContent.append((char) ch);
				etUsername.setText(strContent);
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			etUsername.setText("");
		}

	}

	private void checkReadURL() throws IOException {

		StringBuffer strContent = new StringBuffer("");
		int ch;
		try {
			FileInputStream fos = openFileInput("platformURL.txt");
			try {

				while ((ch = fos.read()) != -1)
					strContent.append((char) ch);

				sURLHost = strContent.toString();
				etURL.setText(sURLHost);
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			etURL.setText("");
		}

	}

	public void writeURL() {
		sURLHost = etURL.getText().toString();

		FileOutputStream fos = null;

		try {
			fos = openFileOutput("platformURL.txt", Context.MODE_PRIVATE);
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		}
		try {
			fos.write(sURLHost.getBytes());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			fos.close();
			startActivity(new Intent(Login.this, Login.class));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (Integer.parseInt(android.os.Build.VERSION.SDK) < 5
				&& keyCode == KeyEvent.KEYCODE_BACK
				&& event.getRepeatCount() == 0) {

			onBackPressed();
		}

		return super.onKeyDown(keyCode, event);
	}

	public void onBackPressed() {

		return;
	}

}