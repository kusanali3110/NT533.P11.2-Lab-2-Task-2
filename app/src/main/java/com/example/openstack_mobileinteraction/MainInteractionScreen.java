package com.example.openstack_mobileinteraction;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.openstack_mobileinteraction.API.InstanceManager;
import com.example.openstack_mobileinteraction.API.NetworkManager;

import org.json.JSONException;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainInteractionScreen extends AppCompatActivity {
    String token;
    AlertDialog loadingDialog;
    ExecutorService executorService;
    Map<String, String> imageMap = new HashMap<String, String>();
    Map<String, String> flavorMap = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_interaction_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        token = getIntent().getStringExtra("token");

        executorService = Executors.newFixedThreadPool(2);

        getDatas();

        Button createNetworkButton = findViewById(R.id.create_network_button);
        createNetworkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView networkName_TextView = findViewById(R.id.network_name_textField);
                TextView subnetName_TextView = findViewById(R.id.subnet_name_textField);
                TextView networkAddress_TextView = findViewById(R.id.network_address_textField);
                TextView staticIP_TextView = findViewById(R.id.static_IP_textField);

                if (TextUtils.isEmpty(networkName_TextView.getText()) || TextUtils.isEmpty(subnetName_TextView.getText()) || TextUtils.isEmpty(networkAddress_TextView.getText()) || TextUtils.isEmpty(staticIP_TextView.getText())) {
                    showRequireNetworkFieldsDialog();
                }
                else {
                    String networkName = networkName_TextView.getText().toString();
                    String subnetName = subnetName_TextView.getText().toString();
                    String networkAddress = networkAddress_TextView.getText().toString();
                    String staticIP = staticIP_TextView.getText().toString();

                    createNetwork(token, networkName, subnetName, networkAddress, staticIP);
                }
            }
        });

        Button createInstaceButton = findViewById(R.id.create_instance_button);
        createInstaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner imagesSelectionView = findViewById(R.id.images_selectionView);
                Spinner flavorsSelectionView = findViewById(R.id.flavors_selectionView);
                Spinner securityGroupsSelectionView = findViewById(R.id.security_groups_selectionView);
                TextView instaceNameTextView = findViewById(R.id.instance_name_textField);
                Spinner portIDTextView = findViewById(R.id.port_ID_selectionView);
                TextView customScriptTextView = findViewById(R.id.custom_script_multiLineTextFied);

                if (TextUtils.isEmpty(imagesSelectionView.getSelectedItem().toString()) || TextUtils.isEmpty(flavorsSelectionView.getSelectedItem().toString()) || TextUtils.isEmpty(securityGroupsSelectionView.getSelectedItem().toString()) || TextUtils.isEmpty(instaceNameTextView.getText().toString()) || TextUtils.isEmpty(portIDTextView.getSelectedItem().toString())) {
                    showRequireInstanceFieldsDialog();
                }
                else {
                    String imageName = imagesSelectionView.getSelectedItem().toString();
                    String flavorName = flavorsSelectionView.getSelectedItem().toString();
                    String securityGroup = securityGroupsSelectionView.getSelectedItem().toString();
                    String instaceName = instaceNameTextView.getText().toString();
                    String portID = portIDTextView.getSelectedItem().toString();
                    String customScript = customScriptTextView.getText().toString();

                    createInstance(token, imageName, flavorName, securityGroup, instaceName, portID, customScript);
                }
            }
        });
    }

    private void getDatas() {
        showLoadingDialog(); // Show loading dialog once at the start
        getImagesData()
                .thenCompose(v -> getFlavorData())
                .thenCompose(v -> getSecurityGroupsData())
                .thenCompose(v -> getPortsData())
                .thenRun(this::hideLoadingDialog) // Hide loading dialog once all tasks are done
                .exceptionally(ex -> {
                    runOnUiThread(this::hideLoadingDialog);
                    showFailureDialog();
                    return null;
                });
    }

    private CompletableFuture<Void> getImagesData() {
        return CompletableFuture.supplyAsync(() -> {
            InstanceManager instanceManager = new InstanceManager();
            Pair<List<String>, List<String>> imagesData = null;
            try {
                imagesData = instanceManager.getImagesList(token);
            } catch (MalformedURLException | JSONException e) {
                e.printStackTrace();
            }
            return imagesData;
        }, executorService).thenAccept(imagesData -> {
            runOnUiThread(() -> {
                if (imagesData != null) {
                    Spinner imagesSpinner = findViewById(R.id.images_selectionView);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, imagesData.second);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    imagesSpinner.setAdapter(adapter);

                    for (int i = 0; i < imagesData.first.size(); i++) {
                        imageMap.put(imagesData.second.get(i), imagesData.first.get(i));
                    }
                } else {
                    showFailureDialog();
                }
            });
        });
    }

    private CompletableFuture<Void> getFlavorData() {
        return CompletableFuture.supplyAsync(() -> {
            InstanceManager instanceManager = new InstanceManager();
            Pair<List<String>, List<String>> flavorsData = null;
            try {
                flavorsData = instanceManager.getFlavorsList(token);
            } catch (MalformedURLException | JSONException e) {
                e.printStackTrace();
            }
            return flavorsData;
        }, executorService).thenAccept(flavorsData -> {
            runOnUiThread(() -> {
                if (flavorsData != null) {
                    Spinner flavorsSpinner = findViewById(R.id.flavors_selectionView);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, flavorsData.second);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    flavorsSpinner.setAdapter(adapter);

                    for (int i = 0; i < flavorsData.first.size(); i++) {
                        flavorMap.put(flavorsData.second.get(i), flavorsData.first.get(i));
                    }
                } else {
                    showFailureDialog();
                }
            });
        });
    }

    private CompletableFuture<Void> getSecurityGroupsData() {
        return CompletableFuture.supplyAsync(() -> {
            InstanceManager instanceManager = new InstanceManager();
            List<String> securityGroupsData = null;
            try {
                securityGroupsData = instanceManager.getSecurityGroupsList(token);
            } catch (MalformedURLException | JSONException e) {
                e.printStackTrace();
            }
            return securityGroupsData;
        }, executorService).thenAccept(securityGroupsData -> {
            runOnUiThread(() -> {
                if (securityGroupsData != null) {
                    Spinner imagesSpinner = findViewById(R.id.security_groups_selectionView);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, securityGroupsData);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    imagesSpinner.setAdapter(adapter);
                } else {
                    showFailureDialog();
                }
            });
        });
    }

    private CompletableFuture<Void> getPortsData() {
        return CompletableFuture.supplyAsync(() -> {
            InstanceManager instanceManager = new InstanceManager();
            List<String> portsData = null;
            try {
                portsData = instanceManager.getPortsList(token);
            } catch (MalformedURLException | JSONException e) {
                e.printStackTrace();
            }
            return portsData;
        }, executorService).thenAccept(portsData -> {
            runOnUiThread(() -> {
                if (portsData != null) {
                    Spinner imagesSpinner = findViewById(R.id.port_ID_selectionView);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, portsData);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    imagesSpinner.setAdapter(adapter);
                } else {
                    showFailureDialog();
                }
            });
        });
    }

    private void createNetwork(String token, String networkName, String subnetName, String networkAddress, String staticIP) {
        Button createNetworkButton = findViewById(R.id.create_network_button);
        createNetworkButton.setClickable(false);

        CompletableFuture.supplyAsync(() -> {
            String networkID = null;
            String subnetID = null;
            String portID = null;
            NetworkManager networkManager = new NetworkManager();
            try {
                networkID = networkManager.createNetwork(token, networkName);
                subnetID = networkManager.createSubnet(token, networkID, subnetName,networkAddress);
                portID = networkManager.createPort(token, networkID, subnetID, staticIP);
            } catch (MalformedURLException | JSONException e) {
                throw new RuntimeException(e);
            }
            List<String> ids = new ArrayList<String>();
            ids.add(networkID);
            ids.add(subnetID);
            ids.add(portID);
            return ids;
        }, executorService).thenAccept(ids -> {
            runOnUiThread(() -> {
                EditText logView = findViewById(R.id.log_textField);
                if (ids.get(0) != null) {
                    logView.append("Created a network successfully with ID: " + ids.get(0) + "\n");
                } else {
                    logView.append("Failed to create a network\n");
                }
                if (ids.get(1) != null) {
                    logView.append("Created a subnet successfully with ID: " + ids.get(1) + "\n");
                } else {
                    logView.append("Failed to create a subnet\n");
                }
                if (ids.get(2) != null) {
                    logView.append("Created a port successfully with ID: " + ids.get(2) + "\n");
                } else {
                    logView.append("Failed to create a port\n");
                }
                getDatas();
                createNetworkButton.setClickable(true);
            });
        });
    }

    private void createInstance(String token, String imageName, String flavorName, String securityGroup, String instanceName, String portID, String customScript) {
        Button createInstaceButton = findViewById(R.id.create_instance_button);
        createInstaceButton.setClickable(false);

        CompletableFuture.supplyAsync(() -> {
            String instaceID = null;
            InstanceManager instanceManager = new InstanceManager();
            try {
                instaceID = instanceManager.createInstance(token, instanceName, imageMap.get(imageName), flavorMap.get(flavorName), securityGroup, portID, customScript);
            } catch (MalformedURLException | JSONException e) {
                throw new RuntimeException(e);
            }
            return instaceID;
        }, executorService).thenAccept(instaceID -> {
            runOnUiThread(() -> {
                EditText logView = findViewById(R.id.log_textField);
                if (instaceID != null) {
                    logView.append("Created a instnce successfully with ID: " + instaceID + "\n");
                } else {
                    logView.append("Failed to create a network\n");
                }
                createInstaceButton.setClickable(true);
            });
        });
    }


    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        builder.setView(progressBar);
        builder.setCancelable(false);
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void showFailureDialog() {
        new android.app.AlertDialog.Builder(MainInteractionScreen.this)
                .setTitle("Error")
                .setMessage("An unexpected error occurred. Please try again later")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showRequireNetworkFieldsDialog() {
        new android.app.AlertDialog.Builder(MainInteractionScreen.this)
                .setTitle("Error")
                .setMessage("Please provide network name, subnet name, network address and static IP.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showRequireInstanceFieldsDialog() {
        new android.app.AlertDialog.Builder(MainInteractionScreen.this)
                .setTitle("Error")
                .setMessage("Please provide image, flavor, security group, instance name and portID.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}