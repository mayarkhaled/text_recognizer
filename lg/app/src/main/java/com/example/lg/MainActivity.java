package com.example.lg;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;

    EditText mResultId;
    ImageView mPreviewIV;
    Button button;
    TextView textView;

    String CameraPermission[];
    String StoragePermission[];
    Uri uri_image;
    int c=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle("Click to insert image");

        mResultId = findViewById(R.id.resultEt);
        mPreviewIV = findViewById(R.id.imageIv);
        button = findViewById(R.id.go);
        textView = findViewById(R.id.txt);

        //camera permission
        CameraPermission = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //storage permission
        StoragePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    }

    //action bar menu ..
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate menu
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    //handle action bar item selected

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.addImage) {
            showImageImportDialog();
        }
       /* else if(id==R.id.settings)
        {
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd");
            String formattedDate = df.format(c);
            Toast.makeText(getApplication(),formattedDate,Toast.LENGTH_LONG).show();
        }*/
        return super.onOptionsItemSelected(item);
    }

    private void showImageImportDialog() {
        //items to display in dialog
        String[] items = {"Camera","gallary"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        //set title
        dialog.setTitle("Select image ");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //camera option
                if (i == 0) {
                    if (!checkCameraPermission()) {
                        requestCameraPemission();
                    } else {
                        //take photo
                        pickCamera();
                    }
                }
                if (i == 1) {
                    //gallery opened
                    if (!checkStroagePermission()) {
                        requestStoragePemission();
                    } else {
                        //open gallery
                        pickGallary();
                    }
                }

            }
        });
        //show dialog
        dialog.create().show();
    }

    private void pickGallary()
    {
        //intent to pick image from gallary
        Intent intent = new Intent(Intent.ACTION_PICK);
        //set intent type to image
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        //intent to take camera , save to storage to get high quality
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Pic"); // title of pic
        values.put(MediaStore.Images.Media.DESCRIPTION, "image to text"); //description
        uri_image = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri_image);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    //handel permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case CAMERA_REQUEST_CODE :
                if(grantResults.length > 0)
                {
                    boolean cameraAccepted = grantResults[0]==
                            PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0]==
                            PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted&&writeStorageAccepted)
                    {
                        pickCamera();
                    }
                    else
                    {
                        Toast.makeText(this,"Permission denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE :
                if(grantResults.length > 0)
                {
                    boolean writeStorageAccepted = grantResults[0]==
                            PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted)
                    {
                        pickGallary();
                    }
                    else
                    {
                        Toast.makeText(this,"Permission denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    //handel image result

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        //got image from camera
        if(resultCode == RESULT_OK)
        {
            if(requestCode==IMAGE_PICK_CAMERA_CODE)
            {
                //got image , now crop it
                CropImage.activity(uri_image)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);// enable image guidlines

            }
            if(requestCode ==IMAGE_PICK_GALLERY_CODE)
            {
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
        }
        //get cropped image
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK)
            {
                Uri resultUri = result.getUri(); // get image uri
                //set image to image view
                mPreviewIV.setImageURI(resultUri);
                //get drawable bitmap for text recognization
                BitmapDrawable bitmapDrawable = (BitmapDrawable)mPreviewIV.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                if(!recognizer.isOperational())
                {
                    Toast.makeText(this,"Error",Toast.LENGTH_LONG).show();
                }
                else
                {
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = recognizer.detect(frame);
                    final StringBuilder sb=new StringBuilder();
                    //get text from string builder untill there is no text
                    for (int i=0;i<items.size();i++)
                    {
                        TextBlock myitem = items.valueAt(i);
                        sb.append(myitem.getValue());
                        sb.append("\n");
                    }
                    //set to edit text
                    mResultId.setText(sb.toString());
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                // c += 1;
                                //string in edit text
                                String str = mResultId.getText().toString();
                                String s = "";
                            /* =Character.toString(str.charAt(0));
                            s+=Character.toString(str.charAt(1));
                            s+=Character.toString(str.charAt(2));*/


                                int ind = str.indexOf(':');
                                boolean is = false;
                                for (int i = 0; i < str.length(); i++) {
                                    char ch = str.charAt(i);
                                    if (Character.isLetter(ch)) {
                                        if (is)
                                            break;
                                    } else if (ch >= '0' && ch <= '9') {
                                        is = true;
                                        s += ch;

                                    }

                                    //textView.setText(s);

                            /*String month = Character.toString(s.charAt(1));
                            month +=Character.toString(s.charAt(2));*/
                                    String month = Character.toString(s.charAt(s.length() - 2));
                                    month += Character.toString(s.charAt(s.length() - 1));

                                    String year = "";
                                    if (s.length() == 3) {
                                        year = Character.toString(s.charAt(0));
                                    } else if (s.length() == 4) {
                                        year = Character.toString(s.charAt(0));
                                        year += Character.toString(s.charAt(1));
                                    }

                                    ///month of code as an integer
                                    int m = Integer.parseInt(month);
                                    int y = Integer.parseInt(year);
                                    //date of the day
                                    Date c = Calendar.getInstance().getTime();
                                    SimpleDateFormat df = new SimpleDateFormat("dd");
                                    SimpleDateFormat yf = new SimpleDateFormat("yyyy");
                                    SimpleDateFormat mf = new SimpleDateFormat("MM");

                                    String dateday = df.format(c);
                                    String dateyear = yf.format(c);
                                    String datemonth = mf.format(c);

                                    String requestedYear = "";
                                    int a = Integer.parseInt(Character.toString(dateyear.charAt(dateyear.length() - 2)));
                                    if (a == 1) {
                                        requestedYear += Character.toString(dateyear.charAt(dateyear.length() - 1));
                                    } else {
                                        requestedYear += Character.toString(dateyear.charAt(dateyear.length() - 2));
                                        requestedYear += Character.toString(dateyear.charAt(dateyear.length() - 1));
                                    }

                                    int currentday = Integer.parseInt(dateday);
                                    int currentmonth = Integer.parseInt(datemonth);
                                    int currentyear = Integer.parseInt(requestedYear);

                                /*Toast.makeText(getApplication(), "current Year = " + currentyear + " year = " + y
                                                + " month = " + currentmonth + " day = " + currentday
                                        , Toast.LENGTH_LONG).show();*/

                                    if ((y == currentyear && (m == currentmonth || m == currentmonth - 1 || m == currentmonth - 2 || m == currentmonth - 3))
                                            || (y > currentyear)) {
                                        if (requestedYear.length() == 1)
                                            textView.setText(currentday - 3 + "/" + currentmonth + "/201" + currentyear);
                                        else
                                            textView.setText(currentday - 3 + "/" + currentmonth + "/20" + currentyear);
                                        return;
                                    }

                                    //  Toast.makeText(getApplication(),formattedDate,Toast.LENGTH_LONG).show();
                                    if (m >= 1 && m <= 9) {
                                        m += 3;
                                    } else if (m == 10) {
                                        m = 1;
                                        y += 1;
                                    } else if (m == 11) {
                                        m = 2;
                                        y += 1;
                                    } else if (m == 12) {
                                        m = 3;
                                        y += 1;
                                    }
                                    //Date current = Calendar.getInstance().getTime();
                                    if (m == 2) {
                                        textView.setText("28/" + m + "/201" + y);
                                    } else if (m == 4 || m == 6 || m == 9 || m == 11) {
                                        textView.setText("30/" + m + "/201" + y);
                                    } else {
                                        textView.setText("31/" + m + "/201" + y);
                                    }

                                    //Toast.makeText(getApplication(),current.toString(),Toast.LENGTH_LONG).show();


                                }
                            }
                            catch (Exception e)
                            {
                                Toast.makeText(getApplication(),"Error,please try again..",Toast.LENGTH_LONG).show();
                                mResultId.setText("");
                                mPreviewIV.setImageDrawable(null);
                            }
                        }
                    });

                }
            }
            else if(resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
                Toast.makeText(this,""+error,Toast.LENGTH_LONG).show();
            }
        }
    }

    private void requestStoragePemission() {
        ActivityCompat.requestPermissions(this, StoragePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkStroagePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPemission() {
        ActivityCompat.requestPermissions(this, CameraPermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        //storage permission to get high quality image we have to save image to ex storage first
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
}