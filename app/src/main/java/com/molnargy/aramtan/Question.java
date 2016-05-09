package com.molnargy.aramtan;

import android.text.Html;

import java.util.ArrayList;

/**
 * Created by Gyula on 2016. 05. 07..
 */
public class Question {
    private String question;
    private String answer;

    Question(String question, String answer){
        this.question = question;
        this.answer = answer;
    }

    Question(String question, String[] answers){
        this.question = question;
        for(int i=0; i<answers.length; i++){
            this.question = this.question.replaceFirst("_", "<font color='#FFFFFF'>"+answers[i]+"</font>");
        }

        this.answer = this.question.replaceAll("#FFFFFF", "#FF8C00");

    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer(){
        return answer;
    }
}
