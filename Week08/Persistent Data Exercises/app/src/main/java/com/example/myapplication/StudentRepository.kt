package com.example.myapplication

import android.content.Context

class StudentRepository(private val context: Context) {

    fun readStudents(): List<Student> {

        val students = mutableListOf<Student>()

        val reader = context.resources
            .openRawResource(R.raw.students)
            .bufferedReader()

        reader.use {

            it.forEachLine { line ->

                val parts = line.split(",")

                val name = parts[0].trim()
                val age = parts[1].trim().toInt()

                val student = Student(name, age)

                students.add(student)
            }
        }

        return students
    }
}