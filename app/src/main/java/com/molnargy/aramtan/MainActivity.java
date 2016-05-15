package com.molnargy.aramtan;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    private ArrayList<Question> questionContainer;
    private int questionCount;
    private int progress;
    private Vector<Integer> fiftiesProgress;
    private Vector<Integer> tensProgress;
    private int tensMin;
    private int tensMax;
    private boolean questionMode;
    private Random random;

    private Context context;

    private int prevTenQuestion; //index inside the vector.

    private TextView onesTextView;
    private WebView webView;

    private boolean repeat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InputStream in = this.getResources().openRawResource(R.raw.aramtan);
        InputStreamReader iReader = new InputStreamReader(in);
        BufferedReader buffReader = new BufferedReader(iReader);

        String line;
        String[] workArray;
        String[] answers = null;
        String question;
        this.questionContainer = new ArrayList<Question>();

        this.fiftiesProgress = new Vector<Integer>();
        this.tensProgress = new Vector<Integer>();

        this.random = new Random();

        //Get app context.
        this.context = getApplicationContext();

        //Store static UI elements
        this.onesTextView = (TextView)findViewById(R.id.progressCounter);
        this.webView = (WebView)findViewById(R.id.webView);

        //Loading the questions
        try{
            this.questionCount=0;
            while((line = buffReader.readLine())!=null){
                workArray = line.split("/");
                question = workArray[0];
                answers = new String[workArray.length-1];
                System.arraycopy(workArray, 1, answers, 0, workArray.length-1);
                questionContainer.add(new Question(question,answers));
                this.questionCount++;
            }
        }catch (Exception e){
            Log.e("onCreate",e.getMessage()); //i don't care
        }

        //Load the progress
        this.progress = loadProgress(); //this sets it to 0 if nothing was saved.

        //Let's get the current 10, and 50 bounds.
        if(this.progress % 50 == 0 && this.progress != 0){
            //in this case we gonna set the bounds according to this.
            setBounds(progress-50, 50);
        }else {
            this.setBounds(progress,10);
        }

        //So we are in question mode.
        this.questionMode = true;

        //Let's display the first question.
        this.displayQuestion();

        //Set the counters
        this.setCounters();

        //set listener on webView
        final MainActivity that = this;
        this.webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    that.showAnswer(v);
                }
                return false;
            }

        });

        this.repeat = false;
    }

    private void displayQuestion(){
        //display a random question out of that 10
        this.prevTenQuestion = this.random.nextInt(
                this.tensProgress.size()
        );

        this.webView.clearCache(true);
        this.webView.clearHistory();
        this.webView.loadDataWithBaseURL(
                null,
                this.questionContainer.get(
                        this.tensProgress.get(
                                this.prevTenQuestion
                        )
                ).getQuestion(),
                "text/html; charset=UTF-8",
                null,
                null
        );
        this.questionMode = true;
    }

    private void setBounds(int start, int noOfQuestions){

        this.tensMin = start;
        if((tensMin+noOfQuestions-1) > (this.questionCount-1)){
            this.tensMax = this.questionCount-1;
        }else{
            this.tensMax = this.tensMin+noOfQuestions-1;
        }

        //now let's populate the corresponding vectors.
        this.tensProgress.clear();
        for(int i=tensMin;i<=tensMax;i++){
            this.tensProgress.add(i);
        }

    }

    private void setCounters(){
        onesTextView.setText(
                String.valueOf(this.tensMin + 1)
                        + "-"
                        + String.valueOf(this.tensMax + 1)
        );
    }

    private int loadProgress(){
        int progressVal = 0;
        try {
            FileInputStream fis = openFileInput("progress");
            ObjectInputStream ois = new ObjectInputStream(fis);
            progressVal = (Integer)ois.readObject();
            //ois.close();
            return progressVal;
        }catch (Exception e){
            Log.e("loadProgress",e.getMessage());
            //the above line is simply skipped somehow...
            //but hey...it's smartphone stuff, so it's ugly and wrong and shit, so it's all right.
        }
        return progressVal;
    }

    private void saveProgress(){
        try {
            FileOutputStream fos = openFileOutput("progress", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this.progress);
            oos.close();
        }catch (Exception e){
            Log.e("saveProgress",e.getMessage());
        }
    }

    public void badAnswer(View view){
        if(!this.questionMode) {
            this.displayQuestion();
            this.setCounters();
        }
    }

    public void goodAnswer(View view){
        if(!this.questionMode) {

            this.tensProgress.remove(this.prevTenQuestion);

            if (this.tensProgress.isEmpty()) { //maybe this is it.
                //if it was empty, then increment the progress, and get the next 10.
                if (this.progress + 10 >= this.questionCount) {
                    this.showAlertDialog();
                }else {
                    //when we first get there, we are over 10 questions, and progress == 0.
                    //thus when we are over 50 questions, the progress is 40.
                    this.progress += 10;
                    if(this.progress % 50 ==0 && !this.repeat){
                        this.repeat = true;
                        //in this case we gonna set the bounds according to this.
                        setBounds(this.progress-50, 50);
                    }else if(this.repeat){
                        //this means that we have just completed a 50 cycle
                        this.repeat=false;
                        this.progress-=10;
                        this.setBounds(this.progress,10);
                    }else {
                        this.setBounds(this.progress,10);
                    }
                    this.displayQuestion();
                    this.setCounters();
                }
                saveProgress();
            }else {
                this.displayQuestion();
                this.setCounters();
            }
        }
    }

    private void showAlertDialog(){
        final MainActivity that = this;

        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Gratulálási.");
        alertDialog.setMessage("Szeretnéd újrakezdeni?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Igen!",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        that.progress = 0;
                        that.setBounds(0,10);
                        that.displayQuestion();
                        that.questionMode = true;
                        that.setCounters();
                        dialog.dismiss();
                    }
                });
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void resetProgress(){
        this.progress=0;
        this.setBounds(0,10);
        this.setCounters();
        this.repeat=false;
        this.saveProgress();
        this.displayQuestion();
    }

    public void showAnswer(View view){

        if(this.questionMode) {
            this.webView.clearCache(true);
            this.webView.clearHistory();
            this.webView.loadDataWithBaseURL(
                    null,
                    this.questionContainer.get(
                            this.tensProgress.get(
                                    this.prevTenQuestion
                            )
                    ).getAnswer(),
                    "text/html; charset=UTF-8",
                    null,
                    null
            );
        }
        this.questionMode=false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.restart) {

            this.resetProgress();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
