package com.augmos.iink.prototype;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.myscript.iink.ConversionState;
import com.myscript.iink.Editor;
import com.myscript.iink.MimeType;
import com.myscript.iink.uireferenceimplementation.EditorView;

import java.io.IOException;


public class EditorFragmentActivity extends AppCompatActivity {
    EditorView editorView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ErrorActivity.installHandler(this);
        //Show Editor Layout composed out of 2 Fragments
        setContentView(R.layout.activity_fragment_editor);
        editorView = findViewById(R.id.editor_view);
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
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    //-----------Methoden von Augmos-----------

    private void analyze(){
        Editor editor = editorView.getEditor();
        ConversionState[] supportedStates = editor.getSupportedTargetConversionStates(null);
        if (supportedStates.length > 0)
            editor.convert(editor.getRootBlock(), supportedStates[0]);
        /*try {
            //Hier zu fb pushen bzw ausgeben
            ExerciseSolution solution = new ExerciseSolution(currentExcerciseID, editor.export_(editor.getRootBlock(), MimeType.JIIX), editor.export_(editor.getRootBlock(), MimeType.MATHML));
            studentRef.collection("solutions").document().set(solution);
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }
}
