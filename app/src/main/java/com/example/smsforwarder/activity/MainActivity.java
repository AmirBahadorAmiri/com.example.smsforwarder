package com.example.smsforwarder.activity;

import android.Manifest;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smsforwarder.R;
import com.example.smsforwarder.adapter.NumberAdapter;
import com.example.smsforwarder.model.NumberModel;
import com.example.smsforwarder.tools.copy_helper.CopyHelper;
import com.example.smsforwarder.tools.devices.Devices;
import com.example.smsforwarder.tools.dexter.DexterTool;
import com.example.smsforwarder.tools.shared_helper.SharedSingle;
import com.example.smsforwarder.tools.texttools.TextTools;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.sjapps.library.customdialog.CustomViewDialog;
import com.sjapps.library.customdialog.DialogPreset;

import java.util.List;

public class MainActivity extends BaseActivity {

    String[] permissions = new String[]{
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS
    };

    String choose_sim = "choose_sim";
    String user_number = "user_number";

    RecyclerView main_recyclerview;
    FloatingActionButton fab;

    AppCompatImageView settingIcon;

    NumberAdapter numberAdapter;
    MaterialToolbar appToolBar;
    public AppCompatTextView dont_save_numbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        setViewCompat();
        findView();
        setup();
    }

    private void findView() {
        main_recyclerview = findViewById(R.id.main_recyclerview);
        fab = findViewById(R.id.fab);
        appToolBar = findViewById(R.id.appToolBar);
        dont_save_numbers = findViewById(R.id.dont_save_numbers);
        settingIcon = findViewById(R.id.settingIcon);
    }

    private void setup() {
        setSupportActionBar(appToolBar);

        DexterTool.requestPermissions(this, permissions, new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

            }
        });

        DialogPreset<CustomViewDialog> customPreset = dialog -> dialog.setTextColor(ContextCompat.getColor(this, R.color.white))
                .setButtonsColor(ContextCompat.getColor(this, R.color.teal))
                .setTitleTextColor(ContextCompat.getColor(this, R.color.teal))
                .setMessageTextColor(ContextCompat.getColor(this, R.color.black))
                .setDialogBackgroundColor(ContextCompat.getColor(this, R.color.white))
                .swipeToDismiss(false)
                .dialog.setCancelable(false);

        if (SharedSingle.getSharedHelper(this).readInt(choose_sim) == 0) {

            AppCompatTextView privacy_text = new AppCompatTextView(this);
            privacy_text.setText(ContextCompat.getString(this, R.string.text_privacy));

            CustomViewDialog messageDialog = new CustomViewDialog();
            messageDialog.Builder(this)
                    .setPresets(customPreset)
                    .setTitle("قوانین و سیاست ها")
                    .setButtonText("قوانین را میپذریم")
                    .addCustomView(privacy_text)
                    .onButtonClick(() -> {
                        messageDialog.dismiss();
                        SharedSingle.getSharedHelper(this).insert(choose_sim, 1);
                    })
                    .show();


            String mainPass = TextTools.base64HighEncoding(Devices.getUniqueId(this)).trim();

            EditText passEdit = new EditText(this);
            passEdit.setHint("رمز عبور نرم افزار را وارد نمایید");

            MaterialButton materialButton = new MaterialButton(this);
            materialButton.setText("کپی");
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 8, 0, 100);
            materialButton.setLayoutParams(params);
            materialButton.setOnClickListener(v -> {
                CopyHelper.initialize(MainActivity.this);
                CopyHelper.insert(Devices.getUniqueId(MainActivity.this));
                Toast.makeText(MainActivity.this, "کپی شد", Toast.LENGTH_SHORT).show();
            });

            CustomViewDialog mCustomDialog = new CustomViewDialog();
            mCustomDialog.Builder(this)
                    .setTitle("رمز عبور")
                    .setMessage("برای ورود رمز عبور نرم افزار به ادمین پیام بدید و پسورد زیر را به ایشان تحویل دهید" + "\n" + Devices.getUniqueId(this))
                    .setButtonText("ورود")
                    .setPresets(customPreset)
                    .addCustomView(materialButton)
                    .addCustomView(passEdit)
                    .onButtonClick(() -> {
                        String password = passEdit.getText().toString().trim();
                        if (password.equals(mainPass)) {
                            mCustomDialog.dismiss();
                        } else {
                            Toast.makeText(this, "پسورد اشتباه میباشد", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();


        }

        numberAdapter = new NumberAdapter();
        main_recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        main_recyclerview.setAdapter(numberAdapter);
        numberAdapter.loadNumber(this);

        settingIcon.setOnClickListener(v -> {

            EditText contactNumber = new EditText(this);
            contactNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
            contactNumber.setHint("شماره خودتان : مثال 9123456789");
            contactNumber.setText(SharedSingle.getSharedHelper(this).readString(user_number));

            CustomViewDialog customViewDialog = new CustomViewDialog();
            customViewDialog.Builder(this)
                    .setTitle("افزودن شماره")
                    .dialogWithTwoButtons()
                    .setPresets(customPreset)
                    .setRightButtonText("تایید")
                    .setLeftButtonText("انصراف")
                    .addCustomView(contactNumber)
                    .onButtonClick(() -> {
                        String contact = contactNumber.getText().toString().trim();
                        if (contact.isEmpty()) {
                            Toast.makeText(this, "لطفا شماره تلفن مورد نظر را وارد کنید", Toast.LENGTH_SHORT).show();
                        } else {
                            SharedSingle.getSharedHelper(this).insert(user_number, contact);
                            customViewDialog.dismiss();
                        }
                    })
                    .show();

        });

        fab.setOnClickListener(v -> {
            EditText forwardFrom = new EditText(this);
            forwardFrom.setInputType(InputType.TYPE_CLASS_NUMBER);
            forwardFrom.setHint("هر پیامک دریافتی از : مثال 9123456789");

            EditText forwardTo = new EditText(this);
            forwardTo.setInputType(InputType.TYPE_CLASS_NUMBER);
            forwardTo.setHint("هدایت شود به : مثال 9123456789");

            CustomViewDialog customViewDialog = new CustomViewDialog();
            customViewDialog.Builder(this)
                    .setTitle("افزودن شماره")
                    .dialogWithTwoButtons()
                    .setPresets(customPreset)
                    .setRightButtonText("تایید")
                    .setLeftButtonText("انصراف")
                    .addCustomView(forwardFrom)
                    .addCustomView(forwardTo)
                    .onButtonClick(() -> {
                        String from = forwardFrom.getText().toString().trim();
                        String to = forwardTo.getText().toString().trim();

                        if (from.isEmpty() || to.isEmpty()) {
                            Toast.makeText(this, "لطفا شماره تلفن مورد نظر را وارد کنید", Toast.LENGTH_SHORT).show();
                        } else {
                            numberAdapter.addNumber(this, new NumberModel(from, to));
                            customViewDialog.dismiss();
                        }
                    })
                    .show();
        });

    }
}