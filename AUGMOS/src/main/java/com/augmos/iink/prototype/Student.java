package com.augmos.iink.prototype;

public class Student {


    private String name;
    private int progress;
    private String teacher;


    public Student(){
        progress = 70;
        name = "Max Mustermann";

    }

    /*

    public Student(String name, int progress, String teacher) {
        this.name = name;
        this.progress = progress;
        this.teacher = teacher;
    }

    public Student(String name, String teacher) {
        this.name = name;
        this.teacher = teacher;
    }

    public Student(String name) {
        this.name = name;
    }

*/



    public void setName(String name) {
        this.name = name;
    }

    /*
    public void setProgress(int progress) {
        this.progress = progress;
    }


    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }
    */

    public String getName(){
        return name;
    }

    public int getProgress(){
        return progress;
    }

}
