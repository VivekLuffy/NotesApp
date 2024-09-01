package com.example.notes.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.notes.Database.NotesDatabase;
import com.example.notes.R;
import com.example.notes.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class createnoteactivity extends AppCompatActivity {
    private EditText inputNoteTitle, inputNoteText;
    private TextView textDateTime,textWebURL;
    private String selectorNoteColor;
    private  String selectedImagePath;
    private View viewSubtitleIndicator;
    private ImageView imageNote;
    private LinearLayout layoutWebURL;
    private AlertDialog dialogAddURL;
    private AlertDialog dialogDeleteNote;
    private Note alreadyAvailableNote;

    private static  final  int REQUEST_CODE_STORAGE_PERMISSION=1;
    private static final  int REQUEST_CODE_SELECT_IMAGE=2;


    ImageView iv2;
    ImageView iv3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createnoteactivity);

        iv2 = findViewById(R.id.image_back);
        iv3 = findViewById(R.id.image_save);
        imageNote=findViewById(R.id.imageNote);
        textWebURL=findViewById(R.id.textWebURL);
        layoutWebURL=findViewById(R.id.layoutWebURL);

        iv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            }
        });

        inputNoteTitle = findViewById(R.id.notetitle);
        inputNoteText = findViewById(R.id.notedetails);
        textDateTime = findViewById(R.id.textDateTime);
        viewSubtitleIndicator = findViewById(R.id.subtitleindicator);

        textDateTime.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date()));

        iv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            }
        });

        selectorNoteColor = "#333333";
        selectedImagePath="";
if(getIntent().getBooleanExtra("isFromQuickActions",false)){
    String type= getIntent().getStringExtra("quickActionType");
    if(type!=null){
        if(type.equals("image")){
            selectedImagePath=getIntent().getStringExtra("imagePath");
            imageNote.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
            imageNote.setVisibility(View.VISIBLE);
            findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
        } else if (type.equals("URL")) {
            textWebURL.setText(getIntent().getStringExtra("URL"));
            layoutWebURL.setVisibility(View.VISIBLE);
            
        }
    }
}
        initMiscellaneous();
        setSubtitleIndicatorColor();
        findViewById(R.id.imageRemoveWebURL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textWebURL.setText(null);
                layoutWebURL.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.imageRemoveImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageNote.setImageBitmap(null);
                imageNote.setVisibility(View.GONE);
                findViewById(R.id.imageRemoveImage).setVisibility(View.GONE);
                selectedImagePath="";
            }
        });
        if(getIntent().getBooleanExtra("isViewOrUpdate",false)){
            alreadyAvailableNote=(Note)getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }
    }

    private  void  setViewOrUpdateNote(){
        inputNoteTitle.setText(alreadyAvailableNote.getTitle());
        inputNoteText.setText(alreadyAvailableNote.getNotetext());
        textDateTime.setText(alreadyAvailableNote.getDatetime());
        if(alreadyAvailableNote.getImagepath()!=null && !alreadyAvailableNote.getImagepath().trim().isEmpty()){
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getImagepath()));
            imageNote.setVisibility(View.VISIBLE);
            findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);
            selectedImagePath=alreadyAvailableNote.getImagepath();
        }
        if(alreadyAvailableNote.getWeblink()!=null && !alreadyAvailableNote.getWeblink().trim().isEmpty()){
            textWebURL.setText(alreadyAvailableNote.getWeblink());
            layoutWebURL.setVisibility(View.VISIBLE);
        }

    }

    private void saveNote() {
        if (inputNoteTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note title can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        final Note note = new Note();
        note.setTitle(inputNoteTitle.getText().toString());
        note.setNotetext(inputNoteText.getText().toString());
        note.setDatetime(textDateTime.getText().toString());
        note.setColor(selectorNoteColor);
        note.setImagepath(selectedImagePath);
        if(layoutWebURL.getVisibility()==View.VISIBLE){
            note.setWeblink(textWebURL.getText().toString());
        }

        if(alreadyAvailableNote!=null){
            note.setId(alreadyAvailableNote.getId());
        }

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                Intent i = new Intent();
                setResult(RESULT_OK, i);
                finish();
            }
        }

        new SaveNoteTask().execute();
    }

    private void initMiscellaneous() {
        final LinearLayout layoutMiscellaneous2 = findViewById(R.id.layoutMiscellaneous1);

        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous2);

        Log.i(String.valueOf(layoutMiscellaneous2),"hll");

        layoutMiscellaneous2.findViewById(R.id.textMiscellaneous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        final ImageView imageColor1 = layoutMiscellaneous2.findViewById(R.id.imageColor1);
        final ImageView imageColor2 = layoutMiscellaneous2.findViewById(R.id.imageColor2);
        final ImageView imageColor3 = layoutMiscellaneous2.findViewById(R.id.imageColor3);
        final ImageView imageColor4 = layoutMiscellaneous2.findViewById(R.id.imageColor4);
        final ImageView imageColor5 = layoutMiscellaneous2.findViewById(R.id.imageColor5);

        layoutMiscellaneous2.findViewById(R.id.viewColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectorNoteColor = "#333333";
                imageColor1.setImageResource(R.drawable.done);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMiscellaneous2.findViewById(R.id.viewColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectorNoteColor = "#FDBE3B";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.done);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMiscellaneous2.findViewById(R.id.viewColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectorNoteColor = "#FF4842";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.done);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMiscellaneous2.findViewById(R.id.viewColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectorNoteColor = "#3A52Fc";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(R.drawable.done);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMiscellaneous2.findViewById(R.id.viewColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectorNoteColor = "#000000";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(R.drawable.done);
                setSubtitleIndicatorColor();
            }
        });
        if(alreadyAvailableNote!=null && alreadyAvailableNote.getColor()!=null && !alreadyAvailableNote.getColor().trim().isEmpty()){
            switch (alreadyAvailableNote.getColor()){
                case "#FDBE3B":
                    layoutMiscellaneous2.findViewById(R.id.viewColor2).performClick();
                    break;
                case "#FF4842":
                    layoutMiscellaneous2.findViewById(R.id.viewColor3).performClick();
                    break;
                case "#3A52Fc":
                    layoutMiscellaneous2.findViewById(R.id.viewColor4).performClick();
                    break;
                case "#000000":
                    layoutMiscellaneous2.findViewById(R.id.viewColor5).performClick();
                    break;
            }
        }
        layoutMiscellaneous2.findViewById(R.id.layoutAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if(ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                )!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            createnoteactivity.this,new String[] {
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                            },REQUEST_CODE_STORAGE_PERMISSION
                    );
                }
                else {
                    selectImage();
                }

            }
        });
        layoutMiscellaneous2.findViewById(R.id.layoutAddUri).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddURLDialog();
            }
        });
        if(alreadyAvailableNote==null){

            layoutMiscellaneous2.findViewById(R.id.layoutDeleteNote).setVisibility(View.VISIBLE);
            layoutMiscellaneous2.findViewById(R.id.layoutDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                     showDeleteNoteDialog();


                }
            });
        }

    }
    private void  showDeleteNoteDialog(){
        if(dialogDeleteNote==null){
            AlertDialog.Builder builder=new AlertDialog.Builder(createnoteactivity.this);
            View view=LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note,(ViewGroup) findViewById(R.id.layoutDeleteNoteContainer)
            );
            builder.setView(view);
            dialogDeleteNote=builder.create();
            if(dialogDeleteNote.getWindow()!=null){
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.textDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    @SuppressLint("StaticFieldLeak")
                    class DeleteNoteTask extends AsyncTask<Void,Void,Void>{

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao().deleteNote(alreadyAvailableNote);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void unused) {
                            super.onPostExecute(unused);
                            Intent i=new Intent();
                            i.putExtra("isNoteDeleted",true);
                            setResult(RESULT_OK,i);
                            finish();
                        }
                    }
                    new DeleteNoteTask().execute();
                }
            });
            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogDeleteNote.dismiss();
                }
            });
        }
        dialogDeleteNote.show();
    }

    private void setSubtitleIndicatorColor() {
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectorNoteColor));
    }
    private void selectImage(){
    Intent i=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    if(i.resolveActivity(getPackageManager())!=null){
        startActivityForResult(i,REQUEST_CODE_SELECT_IMAGE);
    }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CODE_STORAGE_PERMISSION && grantResults.length>0){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                selectImage();
            }
            else {
                Toast.makeText(this,"Permission Denied!",Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE_SELECT_IMAGE && resultCode== RESULT_OK){
            if(data!=null){
                Uri selectImageUri=data.getData();
                if(selectImageUri!=null){
                    try {
                        InputStream inputStream=getContentResolver().openInputStream(selectImageUri);
                        Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);
                        findViewById(R.id.imageRemoveImage).setVisibility(View.VISIBLE);

                        selectedImagePath=getPathFromUri(selectImageUri);

                    }catch (Exception exception){
                        Toast.makeText(this,exception.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
    private  String getPathFromUri(Uri contentUri){
        String filePath;
        Cursor cursor=getContentResolver().query(contentUri,null,null,null,null);
        if(cursor==null){
            filePath=contentUri.getPath();
        }
        else {
            cursor.moveToFirst();
            int index=cursor.getColumnIndex("_data");
            filePath=cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }
    private  void showAddURLDialog(){
        if(dialogAddURL==null){
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            View view= LayoutInflater.from(this).inflate(
                    R.layout.layout_add_uri,(ViewGroup)findViewById(R.id.layoutAddUrlContainer)
            );
            builder.setView(view);
            dialogAddURL= builder.create();
            if(dialogAddURL.getWindow()!=null){
                dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            final EditText inputUrl=view.findViewById(R.id.inputUrl);
            inputUrl.requestFocus();
            view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (inputUrl.getText().toString().trim().isEmpty()){
                        Toast.makeText(createnoteactivity.this,"Enter Url",Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(inputUrl.getText().toString()).matches()) {
                        Toast.makeText(createnoteactivity.this,"Enter valid URL",Toast.LENGTH_SHORT).show();


                        
                    }
                    else {
                        textWebURL.setText(inputUrl.getText().toString());
                        layoutWebURL.setVisibility(View.VISIBLE);
                        dialogAddURL.dismiss();
                    }

                }
            });
            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogAddURL.dismiss();
                }
            });
        }
        dialogAddURL.show();
    }
}
