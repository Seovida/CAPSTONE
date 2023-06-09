package com.example.life

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ToDoActivity : AppCompatActivity() {

    private lateinit var adapter: ToDoAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo)

        val selectedYear = intent.getIntExtra("selectedYear", -1)
        val selectedMonth = intent.getStringExtra("selectedMonth")
        val selectedWeek = intent.getIntExtra("selectedWeek", -1)

        val id = intent.getStringExtra("id")
        val year = selectedYear.toString()
        val month = selectedMonth.toString()
        val week = selectedWeek.toString()

        val dateTextView = findViewById<TextView>(R.id.dateTextView)
        dateTextView.text = "Selected Year: $selectedYear, Month: $selectedMonth, Week: $selectedWeek, id: $id"

        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ToDoAdapter(mutableListOf(), progressBar, id ?: "", year, month, week, 0)
        recyclerView.adapter = adapter

        id?.let {
            getTodos(it, year, month, week)
        }

        val addButton = findViewById<Button>(R.id.addButton)
        addButton.setOnClickListener {
            id?.let {
                addTodo(it, year, month, week)
            }
        }

        val removeButton = findViewById<Button>(R.id.removeButton)
        removeButton.setOnClickListener {
            if (adapter.itemCount > 0) {
                val position = adapter.itemCount - 1
                val text = adapter.getItemText(position)
                id?.let {
                    removeTodo(it, year, month, week, text, position)
                }
            }
        }

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun getTodos(id: String, year: String, month: String, week: String) {
        RetrofitClient.api.getTodoID(id).enqueue(object: Callback<List<ToDoDTO>> {
            override fun onResponse(call: Call<List<ToDoDTO>>, response: Response<List<ToDoDTO>>) {
                val result = response.body()
                val todoList = mutableListOf<ToDo>()
                result?.let { todoDtos ->
                    for(dto in todoDtos) {
                        if(dto.year == year && dto.month == month && dto.week == week) {
                            val todo = ToDo(0, dto.text, dto.status == 1)
                            todoList.add(todo)
                        }
                    }
                }
                adapter.todos.clear()
                adapter.todos.addAll(todoList)
                adapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<List<ToDoDTO>>, t: Throwable) {
                Toast.makeText(this@ToDoActivity, "Error occurred", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addTodo(id: String, year: String, month: String, week: String) {
        RetrofitClient.api.insTodoInfo(id, year, month, week, "", 0).enqueue(object: Callback<Unit>{
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                adapter.addTodo(
                    ToDo(
                        adapter.itemCount + 1,
                        "",
                        false
                    )
                )
            }
            override fun onFailure(call: Call<Unit>, t: Throwable) {
                return
            }
        })
    }

    private fun removeTodo(id: String, year: String, month: String, week: String, text: String, position: Int) {
        RetrofitClient.api.delTodoInfo(id, year, month, week, text)
            .enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if(response.isSuccessful) {
                        adapter.removeTodo(position)
                    } else {
                        Log.d("ResponseError", "Status Code: ${response.code()}")
                        Log.d("ResponseError", "Response Body: ${response.errorBody()?.string()}")
                        Toast.makeText(this@ToDoActivity, "Failed to delete ToDo", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Toast.makeText(this@ToDoActivity, "Failed to delete ToDo", Toast.LENGTH_SHORT).show()
                }
            })
    }

}
