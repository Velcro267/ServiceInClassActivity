package edu.temple.myapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private var timerBinder: TimerService.TimerBinder? = null
    private var isBound = false

    private lateinit var counterTextView: TextView
    private lateinit var startPauseButton: Button
    private lateinit var stopButton: Button


    //menu action buttons
    private var startPauseMenueItem: MenuItem? = null
    private var stopMenueItem: MenuItem? = null


    private val timerHandler = object: Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            counterTextView.text = msg.what.toString()
        }
    }


    private val connection = object: ServiceConnection{
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            timerBinder = p1 as TimerService.TimerBinder
            timerBinder?.setHandler(timerHandler)
            isBound = true
            updateStartButtonLabel()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            TODO("Not yet implemented")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        counterTextView = findViewById(R.id.textView)
        startPauseButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)


        startPauseButton.setOnClickListener {
            handleStartPauseAction()
        }

        stopButton.setOnClickListener {
            handleStopAction()
        }


    }

    //Menu Setup
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        startPauseMenueItem = menu.findItem(R.id.action_start)
        stopMenueItem = menu.findItem(R.id.action_stop)
        updateStartButtonLabel()
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.action_start -> {
                handleStartPauseAction()
                true
            }

            R.id.action_stop ->{
                handleStopAction()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }



    private fun handleStartPauseAction(){
        if(!isBound || timerBinder == null) return

        val binder = timerBinder!!

        when{
            !binder.isRunning && !binder.paused -> {
                binder.start(10)   // or whatever your assignment start value is
            }
            binder.paused -> {
                binder.pause()     // unpause
            }
            else -> {
                binder.pause()     // pause
            }
        }
        updateStartButtonLabel()

    }


    private fun handleStopAction{
        if (!isBound || timerBinder == null) return

        val binder = timerBinder!!
        binder.stop()

        updateStartButtonLabel()

    }

    private fun updateStartButtonLabel(){

        val label = if(!isBound || timerBinder == null){
            "Start"
        }else{
            val binder = timerBinder!!
            when{
                !binder.isRunning && !binder.paused -> "Start"
                binder.paused -> "Unpause"
                else -> "Pause"
            }
        }

        if(this::startPauseButton.isInitialized){
            startPauseButton.text = label
        }


        //Menu item label
        startPauseMenueItem?.title = label

        //enable Stop based on running state
        val stopEnabled =isBound && timerBinder?.isRunning == true
        if(this::stopButton.isInitialized){
            stopButton.isEnabled = stopEnabled
        }

        stopMenueItem?.isEnabled = stopEnabled


    }


    override fun onStart() {
        super.onStart()
        Intent(this, TimerService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
            isBound = true
        }
    }


    override fun onStop() {
        super.onStop()
        if(isBound){
            unbindService(connection)
            isBound = false
        }
    }
}