package com.molnargy.aramtan;

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
            this.question = this.question.replaceFirst("_", "<nobr><font style=\"border-bottom: 1px solid black;\" color=\"#FFFFFF\">"+answers[i]+"</font></nobr>");
        }
        this.question = "<html><div style=\"text-align: center; position: relative; top:50%; transform: translateY(-50%);\"><h2 style=\"line-height: 150%;\">" + this.question + "</h2></div></html>";

        this.answer = this.question.replaceAll("#FFFFFF", "#FF8C00");

    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer(){
        return answer;
    }
}
