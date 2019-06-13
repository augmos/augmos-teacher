package com.augmos.iink.prototype;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PathEffect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

public class TeacherActivity extends AppCompatActivity {

    private final static Logger LOG = LoggerFactory.getLogger(TeacherActivity.class);

    LinearLayout linearLayout;

    GridLayout gridLayout;

    private FirebaseFirestore fb;

    private List<Student> studentList = new ArrayList<>();
    private List<String> studentID = new ArrayList<>();

    private ArrayList<String> themen = new ArrayList<>();
    private ArrayList<String> themenID = new ArrayList<>();




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);

        ErrorActivity.installHandler(this);
        setContentView(R.layout.activity_teacher_main);

        //Layouts
        linearLayout = findViewById(R.id.linearLayout2);
        gridLayout = findViewById(R.id.gridlayout1);

        //firebase
        LOG.info("Connecting to Firebase...");
        fb = FirebaseFirestore.getInstance();
        LOG.info("Connection to Firebase established");

        LOG.info("Firebase: Getting student collection");
        fb.collection("students").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                for (QueryDocumentSnapshot document : value) {
                    Log.d("TeacherActivity", document.getId() + " => " + document.getData());
                    if(studentID.contains(document.getId())){
                        studentList.set(studentID.indexOf(document.getId()), document.toObject(Student.class));
                    }else{
                        studentID.add(document.getId());
                        studentList.add(document.toObject(Student.class));
                    }

                }
                gridLayout.removeAllViews();
                fillContent();
            }});


        //Lehrer Themen

        LOG.info("Firebase: Getting exercise collection");
        fb.collection("exercises").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    LOG.info("Firebase: Received exercise collection");
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("TeacherActivity", document.getId() + " => " + document.getData());
                        themen.add(document.get("name").toString());
                        themenID.add(document.getId());
                    }
                } else {
                    Log.d("TeacherActivity", "Error getting documents: ", task.getException());
                    LOG.error("Firebase: Error receiving exercise collection");
                }
            }
        });






    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_main, menu);

        MenuItem convertMenuItem = menu.findItem(R.id.menu_convert);
        convertMenuItem.setEnabled(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_convert:
            {
                onCreateDialog(null).show();
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }




    public void fillContent(){


        gridLayout.setRowCount(3);
        gridLayout.setColumnCount(4);


        for (int i = 0; i < studentList.size(); i++) {
            final Student curStudent = studentList.get(i);
            final String curStudentID = studentID.get(i);


            final LinearLayout container = new LinearLayout(this);
            container.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            container.setOrientation(LinearLayout.VERTICAL);
            container.setGravity(Gravity.CENTER);
            container.setPadding(10,10,10,10);
            //container.setBackgroundColor(Color.WHITE);





            final ImageButton imageButton;

            imageButton = new ImageButton(this); //<-- this is the activity
            imageButton.setBackgroundDrawable(null);
            imageButton.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDetail(curStudent, curStudentID);
                }
            });

            container.addView(imageButton);


            //Name
            TextView textView = new TextView(this);
            textView.setText(curStudent.getName());
            textView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            container.addView(textView);

            //progress
            ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setIndeterminate(false);
            progressBar.setProgress(curStudent.getProgress());
            container.addView(progressBar);

            gridLayout.addView(container);

            // Reference to an image file in Cloud Storage
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(curStudentID + ".JPG");

            GlideApp.with(this /* context */)
                    .load(storageReference)
                    .centerCrop()
                    .placeholder(R.drawable.profile)
                    .into(imageButton);



        }

    }

    public void showDetail(Student student, String studentid){

        if(linearLayout.getChildCount() > 0)
            linearLayout.removeAllViews();

        if(student != null){
            //TODO RecyclerView
            //Obere Detailcard
            CardView profileCard = new CardView(this);
            profileCard.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            profileCard.setRadius(4);

            LinearLayout cardLinearLayout = new LinearLayout(this);
            cardLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            cardLinearLayout.setOrientation(LinearLayout.VERTICAL);
            cardLinearLayout.setPadding(10,10,10,10);
            cardLinearLayout.setGravity(Gravity.CENTER);
            profileCard.addView(cardLinearLayout);

            //image
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.profile);
            cardLinearLayout.addView(imageView);

            //Name
            TextView textView = new TextView(this);
            textView.setText(student.getName());
            textView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            cardLinearLayout.addView(textView);

            //progress
            ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setIndeterminate(false);
            progressBar.setProgress(student.getProgress());
            cardLinearLayout.addView(progressBar);

            linearLayout.addView(profileCard);

            Space space = new Space(this);
            space.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    10));
            linearLayout.addView(space);

            //----Ende Obere Card

            //für chart die value list
            final List<PointValue> values = new ArrayList<PointValue>();
            //Daten über ExcerciseSolutions der Schüler letzte 10
            LOG.info("Firebase: Getting student collection");

            fb.collection("students").document(studentid).collection("solutions").orderBy("timestamp", Query.Direction.DESCENDING).limit(10).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value,
                                    @Nullable FirebaseFirestoreException e) {
                    int i = 0;
                    if(value.isEmpty()){
                        return;
                    }
                    for (QueryDocumentSnapshot document : value) {
                        Log.d("TeacherActivity", document.getId() + " => correct: " + document.getData().get("correct"));
                        ExerciseSolution ex = document.toObject(ExerciseSolution.class);
                        if(ex.getCorrect()){
                            values.add(new PointValue(i++,1));
                            //Log.d("TeacherActivity", "true");
                        }else{
                            values.add(new PointValue(i++,0));
                            //Log.d("TeacherActivity", "false");
                        }
                    }
                    //hier der ganze Rest zum erstellen des Views
                    CardView chartCardView = new CardView(TeacherActivity.this);
                    chartCardView.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    chartCardView.setRadius(4);

                    LinearLayout chartLinearLayout = new LinearLayout(TeacherActivity.this);
                    chartLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    chartLinearLayout.setOrientation(LinearLayout.VERTICAL);
                    chartLinearLayout.setPadding(10,10,10,10);
                    chartCardView.addView(chartLinearLayout);

                    TextView title = new TextView(TeacherActivity.this);
                    title.setText("Richtige Aufgaben");
                    chartLinearLayout.addView(title);

                    LineChartView chart = new LineChartView(TeacherActivity.this);
                    chart.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            400));

                    //In most cased you can call data model methods in builder-pattern-like manner.
                    Line line = new Line(values).setColor(ContextCompat.getColor(TeacherActivity.this, R.color.colorAccent)).setCubic(false).setStrokeWidth(2);
                    List<Line> lines = new ArrayList<Line>();
                    lines.add(line);

                    LineChartData data = new LineChartData();
                    data.setLines(lines);
                    data.setAxisXBottom(Axis.generateAxisFromRange(0,values.size(),1));
                    data.setAxisYLeft(Axis.generateAxisFromRange(0,1,1));

                    chart.setLineChartData(data);
                    chartLinearLayout.addView(chart);

                    linearLayout.addView(chartCardView);


                }});


            // Reference to an image file in Cloud Storage
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(studentid + ".JPG");
            Log.d("Reference", studentid + ".JPG");

            GlideApp.with(this /* context */)
                    .load(storageReference)
                    .override(300)
                    .centerCrop()
                    .placeholder(R.drawable.profile)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView);

        }



    }


    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Themengebiet wählen")
                .setItems(themen.toArray(new String[]{}), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        fb.collection("teachers").document(Session.getTeacherID()).update("currentExerciseField", themenID.get(which));
                    }
                });
        return builder.create();
    }


}

