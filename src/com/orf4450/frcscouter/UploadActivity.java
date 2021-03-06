package com.orf4450.frcscouter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.orf4450.frcscouter.pit.PitDB;
import com.orf4450.frcscouter.stand.StandDB;
import com.orf4450.scouter.R;

import java.util.Set;

/**
 * Activity to handle uploading of data from a scouter to the master device
 *
 * @author Caleb Milligan
 *         Created on 2/12/2016
 */
public class UploadActivity extends Activity {
	private StandDB stand_db;
	private PitDB pit_db;
	private Dialog status_dialog;
	private ListView list_devices;
	private ArrayAdapter<RemoteDeviceWrapper> list_adapter;

	@Override
	public void onCreate(Bundle saved_instance_state) {
		super.onCreate(saved_instance_state);
		setContentView(R.layout.upload);

		// Instantiate dummy databases
		pit_db = new PitDB(this, null);
		stand_db = new StandDB(this, null);

		// Acquire and display connected Bluetooth devices
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> devices = adapter.getBondedDevices();
		list_adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
		for (BluetoothDevice device : devices) {
			list_adapter.add(new RemoteDeviceWrapper(device));
		}
		list_adapter.notifyDataSetChanged();

		list_devices = (ListView) findViewById(R.id.list_devices);
		list_devices.setAdapter(list_adapter);
		// Set the listener
		list_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Disable the list to prevent multiple uploads
				list_devices.setEnabled(false);
				// Show a status dialog
				status_dialog = new Dialog(UploadActivity.this);
				status_dialog.setContentView(R.layout.upload_status);
				status_dialog.show();
				// Get the clicked device
				BluetoothDevice device = list_adapter.getItem(position).getDevice();
				// Create the upload task
				new UploadTask(device, new UploadCallback() {
					@Override
					public void onUploadFinished(final Throwable e) {
						// Dismiss the status dialog on the main UI thread
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								status_dialog.dismiss();
							}
						});
						if (e != null) {
							// Display a new error report panel on the main UI thread
							UploadActivity.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									new AlertDialog.Builder(UploadActivity.this)
											.setTitle("Upload failed")
											.setMessage(e.getClass() + ": " + e.getMessage())
											.setIcon(android.R.drawable.ic_dialog_alert)
											.setOnDismissListener(new DialogInterface.OnDismissListener() {
												@Override
												public void onDismiss(DialogInterface dialog) {
													UploadActivity.this.finish();
												}
											})
											.show();
								}
							});
						}
						else {
							// Close out of the activity
							UploadActivity.this.finish();
						}
					}
				}, pit_db, stand_db);
			}
		});
		// Set the listener
		findViewById(R.id.button_reset_uploaded).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Reset uploaded status of all databases
				stand_db.resetUploaded();
				pit_db.resetUploaded();
			}
		});
	}
}
