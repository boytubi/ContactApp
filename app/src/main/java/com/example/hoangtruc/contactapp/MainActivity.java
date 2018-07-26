package com.example.hoangtruc.contactapp;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mTextView_name, mTextView_phno;
    private Button mButton_load;
    private static final int PICK_CONTACT =1;
    private static final int REQUEST_MULTIPLE_PERMISSIONS = 124;
    private static final String READ_CONTACTS="Read Contacts";
    private static final String WRITE_CONTACTS="Write Contacts";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            accessContact();
        }
        mButton_load.setOnClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void accessContact() {
        final List<String> permissionsNeeded=new ArrayList<>();
        final List<String> permissionsList =new ArrayList<>();
        if (!addPermission(permissionsList, Manifest.permission.READ_CONTACTS)){
            permissionsNeeded.add(READ_CONTACTS);
        }
        if (!addPermission(permissionsList,Manifest.permission.WRITE_CONTACTS)){
            permissionsNeeded.add(WRITE_CONTACTS);
        }
        if (permissionsList.size()>0){
            if (permissionsNeeded.size()>0){
                String message="You need to grant access to " +permissionsNeeded.get(0);
                for(int i=1;i<permissionsNeeded.size();i++)
                     message=message + ","+ permissionsNeeded.get(i);
                showMessageOkCancel(message,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),REQUEST_MULTIPLE_PERMISSIONS);
                            }
                        }
                );
             return;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),REQUEST_MULTIPLE_PERMISSIONS);
           return;
        }
    }

    private void initializeViews() {
        mTextView_name = findViewById(R.id.textview_name);
        mTextView_phno = findViewById(R.id.textview_phno);
        mButton_load = findViewById(R.id.button_load);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);

            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }
    private void showMessageOkCancel(String message, DialogInterface.OnClickListener okListener ){
        new AlertDialog.Builder(MainActivity.this)
                         .setMessage(message)
                          .setPositiveButton("Ok",okListener)
                           .setNegativeButton("Cancel", null)
                            .create()
                             .show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_load :
                Intent intent=new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent,PICK_CONTACT);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case (PICK_CONTACT) :
                if (resultCode== Activity.RESULT_OK){
                    Uri contactData =data.getData();
                    Cursor c=managedQuery(contactData,null,null,null,null);
                    if(c.moveToFirst()){
                        String id=c.getString(c.getColumnIndexOrThrow((ContactsContract.Contacts._ID)));
                        String hasPhone=c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                         try {
                             if(hasPhone.equalsIgnoreCase("1")){
                                 Cursor phones=getContentResolver().query(
                                         ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                                         ,null
                                         ,ContactsContract.CommonDataKinds.Phone.CONTACT_ID+"="+id
                                         ,null
                                         ,null);
                                 phones.moveToFirst();
                                 String cNumber =phones.getString(phones.getColumnIndex("data1"));
                                 mTextView_phno.setText("Phone Number is :" +cNumber);
                             }
                             String name=c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                             mTextView_name.setText("Name is :"+name);
                         }catch (Exception ex){}
                    }
                }
                break;
        }
    }
}
