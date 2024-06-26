package com.example.capstoneapp.ui.Feature02.WorkoutFinish

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.capstoneapp.R
import com.example.capstoneapp.data.response.GetDataItem
import com.example.capstoneapp.databinding.ActivityWorkoutFinishBinding
import com.example.capstoneapp.ui.Feature02.WorkoutSequence.WorkoutSequenceActivity
import com.example.capstoneapp.ui.MainActivity

class WorkoutFinishActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutFinishBinding
    private var detail: GetDataItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        window.statusBarColor = getColor(R.color.black)
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutFinishBinding.inflate(layoutInflater)
        setContentView(binding.root)

        detail = intent.getParcelableExtra(WorkoutSequenceActivity.KEY_WORKOUTS) as GetDataItem?

        setAction()
        setDisplay()
    }

    private fun setDisplay() {
        binding.workoutFinishTvPageTitle.text = "${detail!!.target} Body Workout"
    }

    private fun setAction() {
        binding.workoutStartClCloseButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
    }

    companion object {
        const val KEY_WORKOUTS = "workouts"
    }
}