package com.example.dell.retrieveimage;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AdminProf extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST=1;
    Button Uploadbtn,Choosebtn;
    EditText enterFile;
    TextView showUploads;
    ProgressBar progressBar;
    ImageView imageView;

    private Uri mImageUri;
    StorageReference mStorageRef;
    DatabaseReference mDatabaseRef;
    StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_prof);

        mStorageRef= FirebaseStorage.getInstance().getReference("Uploads");
        mDatabaseRef= FirebaseDatabase.getInstance().getReference("Uploads");

        Uploadbtn=findViewById(R.id.Upload);
        Choosebtn=findViewById(R.id.chooseImage);
        enterFile=findViewById(R.id.fileName);
        showUploads=findViewById(R.id.Show);
        progressBar=findViewById(R.id.ProgressBar);
        imageView=findViewById(R.id.Image);


        Choosebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();

            }
        });

        Uploadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask!=null&&mUploadTask.isInProgress()) {

                    Toast.makeText(AdminProf.this, "Upload is in Progress", Toast.LENGTH_SHORT).show();
                }else{
                    uploadFile();
                }
            }
        });
        showUploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openShowUploads();
            }
        });

    }

    private void openFileChooser(){
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==PICK_IMAGE_REQUEST &&resultCode==RESULT_OK
                && data!=null&&data.getData() !=null){
            mImageUri=data.getData();
            Picasso.with(this).load(mImageUri).into(imageView);
            imageView.setImageURI(mImageUri);
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cr=getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));

    }
    private void uploadFile(){

        if (mImageUri!=null){
            StorageReference fileReference=mStorageRef.child(System.currentTimeMillis()+
                    "." + getFileExtension(mImageUri));
            mUploadTask=fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Handler handler=new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress(0);
                                }
                            },500);
                            Toast.makeText(AdminProf.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                            Upload upload=new Upload(enterFile.getText().toString().trim(),
                                    taskSnapshot.getDownloadUrl().toString());
                            String uploadId=mDatabaseRef.push().getKey();
                            mDatabaseRef.child(uploadId).setValue(upload);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(AdminProf.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            double progress=100.0* taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                            progressBar.setProgress((int) progress);
                        }
                    });
        }else {
            Toast.makeText(this, "No File Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private  void openShowUploads(){
        Intent intent=new Intent(AdminProf.this,ShowUploads.class);
        startActivity(intent);
    }
}
