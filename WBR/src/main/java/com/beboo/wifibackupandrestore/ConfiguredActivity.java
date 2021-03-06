package com.beboo.wifibackupandrestore;

import java.io.IOException;
import java.util.ArrayList;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.SimpleAdapter;

import com.beboo.wifibackupandrestore.backupmanagement.Network;
import com.beboo.wifibackupandrestore.backupmanagement.WIFIConfigurationManager;


public class ConfiguredActivity extends NetworkListActivity {

	private WIFIConfigurationManager confManager;

	public SimpleAdapter contentAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("WBR","initialisation of  configured networks activity");
		setContentView(R.layout.networklist);
		confManager = WIFIConfigurationManager.getInstance();
		contentAdapter = initList(confManager.getConfiguredNetworks());
		//contentAdapter = initList(new ArrayList<Network>());	
		ListView lv = getListView();

		// Listener : on envoie un intent avec l'id du contact
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {				
				//TODO : display network details
			}
		});

		confManager.setConfiguredNetworkChangedListener(this);
		lv.refreshDrawableState();

	}

	/*
	//Fonction appelée au clic d'une des checkbox
	public void MyHandler(View v) {
		CheckBox cb = (CheckBox) v;
		//on récupère la position à l'aide du tag défini dans la classe MyListAdapter
		int position = Integer.parseInt(cb.getTag().toString());

		// On récupère l'élément sur lequel on va changer la couleur
		View o = getListView().getChildAt(position).findViewById(
				R.id.selected);


	}
	 */



	/*********************************************
	 * 
	 *             menu contextuel
	 * 
	 **********************************************/

	// Menu Contextuel
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		// On crée notre menu
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.contextmenu_configured, menu);

		// On récupère le nom pour l'entête du menu
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		menu.setHeaderTitle(" ");

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		

		Log.d("WBR","contextual menu ["+getString(R.string.backup)+"] clicked");
		TextView ssid = (TextView)info.targetView.findViewById(R.id.alias);
		Log.d("WBR","contextual menu ["+getString(R.string.backup)+"] clicked :: was on "+ssid.getText()+" / @"+info.position);
		
		//final Network net = confManager.getBackupedNetworks().get(position);
		final Network net = confManager.getConfiguredNetworkBySsid(ssid.getText().toString());
		if (net != null) {
			switch (item.getItemId()) {
			case R.id.save_network: {

				Log.d("WBR","contextual menu ["+getString(R.string.backup)+"] clicked :: was on "+ssid+" / "+net);
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setTitle(R.string.backup);
				alert.setMessage("donnez un alias au r\u00e9seau "+ssid.getText().toString() +" :");

				// Set an EditText view to get user input 
				final EditText input = new EditText(this);
				alert.setView(input);

				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						// TODO Auto-generated method stub
						String alias = input.getText().toString();
						Log.d("WBR","setting alias ["+alias+"] to network ["+net.getSsid()+"]");
						backupNetwork(net, alias);
					}
				};

				alert.setPositiveButton(getString(R.string.ok), listener);
				alert.setNegativeButton(getString(R.string.cancel), listener);

				alert.show();

				break;
			}
			case R.id.view_conf_network : {
				viewNetwork(net);
				break;
			}


			default:
				return super.onContextItemSelected(item);
			}
		}
		return true;
	}

	
	
	private void backupNetwork(Network network, String alias) {

		Network backupedNetwork = network.clone();
		backupedNetwork.setAlias(alias);

		//ConfiguredActivity.this.contentAdapter.notifyDataSetChanged();
		try {
			WIFIConfigurationManager.getInstance().backupNetwork(backupedNetwork);
		} catch (IOException e) {
			Log.e("WBR","error backuping network : "+e.getMessage());
			e.printStackTrace();
			messageDialog(getString(R.string.backup_failure)+" : "+e.getLocalizedMessage());
		}	
		// TODO half worked
		getListView().invalidate(); 
	}

	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d("WBR","contextual menu ["+getString(R.string.backup)+"] clicked");
		TextView ssid = (TextView)findViewById(R.id.alias);
		Log.d("WBR","contextual menu ["+getString(R.string.backup)+"] clicked :: was on "+ssid);
		
		//final Network net = confManager.getBackupedNetworks().get(position);
		final Network net = confManager.getConfiguredNetworkBySsid(ssid.getText().toString());
		viewNetwork(net);
	}

	
	/*********************************************
	 * 
	 * 
	 *             mise à jour de la liste
	 * 
	 **********************************************/


	public void onNetworkDataChanged() {

		Log.d("WBR","ConfiguredActivity : configured networks updated");		
		initList(confManager.getConfiguredNetworks());
	}
}
