package com.example.capstoneapp.ui.Feature02.WorkoutPlanDetail

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstoneapp.R
import com.example.capstoneapp.data.pref.EditWorkoutPlanBody
import com.example.capstoneapp.data.response.DataItem
import com.example.capstoneapp.data.response.GetDataItem
import com.example.capstoneapp.data.response.GetWorkoutsItem
import com.example.capstoneapp.databinding.ActivityWorkoutPlanDetailBinding
import com.example.capstoneapp.ui.Feature02.WorkoutList.WorkoutListAdapter
import com.example.capstoneapp.viewmodel.Feature02.WorkoutPlanDetail.WorkoutPlanDetailViewModel
import com.example.capstoneapp.viewmodel.ViewModelFactory

class WorkoutPlanDetailActivity : AppCompatActivity() {
    private var detail: GetDataItem? = null
    private lateinit var binding: ActivityWorkoutPlanDetailBinding
    private var daysLocal = mutableListOf<Int>()
    private var selectedWorkoutsId = mutableListOf<String>()
    private val viewModel by viewModels<WorkoutPlanDetailViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.black)
        binding = ActivityWorkoutPlanDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val favoriteWorkoutLayoutManager = LinearLayoutManager(this)
        binding.workoutPlanDetailRvFavoriteWorkoutList.layoutManager = favoriteWorkoutLayoutManager

        val recommendedWorkoutLayoutManager = LinearLayoutManager(this)
        binding.workoutPlanDetailRvRecommendedWorkoutList.layoutManager =
            recommendedWorkoutLayoutManager

        detail = intent.getParcelableExtra(KEY_DETAIL) as GetDataItem?

        initDisplay(detail!!)
        setAction(detail!!)
        onDaySelection()

        viewModel.deleteResponse.observe(this) { result ->
            Log.d("DELETION STATS", result.status.toString())
            if (result.status == 200) {
                finish()
            }
        }

        viewModel.updateWorkoutPlanResponse.observe(this) { result ->
            if (result.status == 200) {
                finish()
            }
        }
    }

    private fun setAction(detail: GetDataItem) {
        binding.workoutPlanDetailIvBackButton.setOnClickListener {
            finish()
        }

        binding.workoutPlanDetailClDeleteWorkoutPlanButton.setOnClickListener {
            viewModel.getSession().observe(this) { user ->
                val token = "Bearer ${user.token}"
                viewModel.deleteWorkoutPlan(token, detail.id!!)
            }
        }

        binding.workoutPlanDetailTvSaveWorkoutPlanButton.setOnClickListener {
            val editWorkoutPlanBody = EditWorkoutPlanBody(daysLocal, selectedWorkoutsId)

            if (validateWorkout() && !isDayEmpty()) {
                viewModel.getSession().observe(this) { user ->
                    val token = "Bearer ${user.token}"
                    viewModel.updateWorkoutPlan(token, detail.id!!, editWorkoutPlanBody)
                }
            }
        }
    }

    // VALIDATION
    private fun validateWorkout(): Boolean {
        if (!checkAmountOfWorkout()) {
            val alertDialog = AlertDialog.Builder(this).apply {
                setTitle("Can not proceed yet!")
                setMessage("You need to choose at least " + determineAmountOfWorkout() + " workouts")
                setPositiveButton("Close") { dialog, _ ->
                    dialog.dismiss()
                }
                create()
            }

            alertDialog.show()
        }

        return checkAmountOfWorkout()
    }

    private fun isDayEmpty(): Boolean {
        if (daysLocal.isEmpty()) {
            val alertDialog = AlertDialog.Builder(this).apply {
                setTitle("Can not proceed yet!")
                setMessage("You need to choose at least one day")
                setPositiveButton("Close") { dialog, _ ->
                    dialog.dismiss()
                }
                create()
            }

            alertDialog.show()
        }
        return daysLocal.isEmpty()
    }

    private fun determineAmountOfWorkout(): String {
        return when (detail?.level) {
            "Beginner" -> "5"
            "Intermediate" -> "7"
            "Advance" -> "10"
            else -> ""
        }
    }

    private fun checkAmountOfWorkout(): Boolean {
        return when (detail?.level) {
            "Beginner" -> selectedWorkoutsId.size >= 5
            "Intermediate" -> selectedWorkoutsId.size >= 7
            "Advance" -> selectedWorkoutsId.size >= 10
            else -> false
        }
    }

    // WORKOUTS
    private fun initDisplay(detail: GetDataItem) {
        setInitDays(detail.days!!)
        setInitFavoriteWorkouts(detail.workouts!!)
        setInitRecommendedWorkout(detail)
    }

    private fun setInitRecommendedWorkout(detail: GetDataItem) {
        viewModel.getSession().observe(this) { user ->
            val token = "Bearer ${user.token}"
            viewModel.getRecommendedWorkout(token, detail!!)
        }

        viewModel.recommendedWorkouts.observe(this) { workouts ->
            setRecommendedAdapter(workouts)
        }
    }

    private fun setInitFavoriteWorkouts(workouts: List<GetWorkoutsItem?>) {
        val dataItemWorkouts = workouts.map { workout ->
            DataItem(
                shortDescription = workout!!.shortDescription,
                instructions = workout.instructions,
                rating = workout.rating,
                equipment = workout.equipment,
                id = workout.id,
                guideImgUrl = workout.guideImgUrl,
                bodyGroup = workout.bodyGroup,
                youtubeLinks = workout.youtubeLinks,
                youtubeTitle = workout.youtubeTitle,
                exerciseImages = workout.exerciseImages,
                exerciseName = workout.exerciseName,
                option = workout.option
            )
        }

        viewModel._favoriteWorkouts.value = dataItemWorkouts
        dataItemWorkouts.forEach {
            selectedWorkoutsId.add(it.id!!)
        }
        setFavoriteAdapter(dataItemWorkouts)
    }

    private fun setFavoriteAdapter(workoutList: List<DataItem?>) {
        val adapter = WorkoutListAdapter(
            onItemClicked = { workoutItem -> onWorkoutItemClicked(workoutItem) },
            isSelected = { id -> selectedWorkoutsId.contains(id) }
        )
        adapter.submitList(workoutList)
        binding.workoutPlanDetailRvFavoriteWorkoutList.adapter = adapter
    }

    private fun setRecommendedAdapter(workoutList: List<DataItem?>) {
        val adapter = WorkoutListAdapter(
            onItemClicked = { workoutItem -> onWorkoutItemClicked(workoutItem) },
            isSelected = { id -> selectedWorkoutsId.contains(id) }
        )
        adapter.submitList(workoutList)
        binding.workoutPlanDetailRvRecommendedWorkoutList.adapter = adapter
    }

    private fun onWorkoutItemClicked(workoutItem: DataItem) {
        if (!selectedWorkoutsId.contains(workoutItem.id.toString())) {
            selectedWorkoutsId.add(workoutItem.id.toString())
            viewModel.addFavoriteWorkouts(workoutItem)
        } else {
            selectedWorkoutsId.remove(workoutItem.id.toString())
            viewModel.removeFavoriteWorkouts(workoutItem)
        }

        Log.d("Workout Size", selectedWorkoutsId.size.toString())

    }

    // DAYS
    private fun setInitDays(days: List<Int?>) {
        days.forEach { setClickedOnDayUI(it!!) }
        daysLocal = days as MutableList<Int>
    }

    private fun onDaySelection() {
        binding.workoutPlanDetailClDayContainerSunday.setOnClickListener {
            setDays(0)
        }

        binding.workoutPlanDetailClDayContainerMonday.setOnClickListener {
            setDays(1)
        }

        binding.workoutPlanDetailClDayContainerTuesday.setOnClickListener {
            setDays(2)
        }

        binding.workoutPlanDetailClDayContainerWednesday.setOnClickListener {
            setDays(3)
        }

        binding.workoutPlanDetailClDayContainerThursday.setOnClickListener {
            setDays(4)
        }

        binding.workoutPlanDetailClDayContainerFriday.setOnClickListener {
            setDays(5)
        }

        binding.workoutPlanDetailClDayContainerSaturday.setOnClickListener {
            setDays(6)
        }
    }

    private fun setDays(dayIndex: Int) {
        if (!daysLocal.contains(dayIndex)) {
            daysLocal.add(dayIndex)
            setClickedOnDayUI(dayIndex)
        } else {
            daysLocal.remove(dayIndex)
            setClickedOffDayUI(dayIndex)
        }

        Log.d("DAY LOCAL", daysLocal.toString())
    }

    private fun setClickedOnDayUI(dayIndex: Int) {
        val lightBlue = ContextCompat.getColor(this, R.color.paleBlue)
        val darkBlue = ContextCompat.getColor(this, R.color.darkBlue)
        when (dayIndex) {
            0 -> {binding.workoutPlanDetailClDayContainerSunday.setBackgroundColor(lightBlue!!)
                binding.workoutPlanDetailTvDaySunday.setTextColor(darkBlue!!)}
            1 -> {binding.workoutPlanDetailClDayContainerMonday.setBackgroundColor(lightBlue!!)
                binding.workoutPlanDetailTvDayMonday.setTextColor(darkBlue!!)}
            2 -> {binding.workoutPlanDetailClDayContainerTuesday.setBackgroundColor(lightBlue!!)
                binding.workoutPlanDetailTvDayTuesday.setTextColor(darkBlue!!)}
            3 -> {binding.workoutPlanDetailClDayContainerWednesday.setBackgroundColor(lightBlue!!)
                binding.workoutPlanDetailTvDayWednesday.setTextColor(darkBlue!!)}
            4 -> {binding.workoutPlanDetailClDayContainerThursday.setBackgroundColor(lightBlue!!)
                binding.workoutPlanDetailTvDayThursday.setTextColor(darkBlue!!)}
            5 -> {binding.workoutPlanDetailClDayContainerFriday.setBackgroundColor(lightBlue!!)
                binding.workoutPlanDetailTvDayFriday.setTextColor(darkBlue!!)}
            6 -> {binding.workoutPlanDetailClDayContainerSaturday.setBackgroundColor(lightBlue!!)
                binding.workoutPlanDetailTvDaySaturday.setTextColor(darkBlue!!)}
        }
    }

    private fun setClickedOffDayUI(dayIndex: Int) {
        val white = ContextCompat.getColor(this, R.color.white)
        val darkBlue = ContextCompat.getColor(this, R.color.darkBlue)
        when (dayIndex) {
            0 -> {binding.workoutPlanDetailClDayContainerSunday.setBackgroundColor(darkBlue)
                binding.workoutPlanDetailTvDaySunday.setTextColor(white)}
            1 -> {binding.workoutPlanDetailClDayContainerMonday.setBackgroundColor(darkBlue)
                binding.workoutPlanDetailTvDayMonday.setTextColor(white)}
            2 -> {binding.workoutPlanDetailTvDayTuesday.setBackgroundColor(darkBlue)
                binding.workoutPlanDetailTvDayTuesday.setTextColor(white)}
            3 -> {binding.workoutPlanDetailClDayContainerWednesday.setBackgroundColor(darkBlue)
                binding.workoutPlanDetailTvDayWednesday.setTextColor(white)}
            4 -> {binding.workoutPlanDetailClDayContainerThursday.setBackgroundColor(darkBlue)
                binding.workoutPlanDetailTvDayThursday.setTextColor(white)}
            5 -> {binding.workoutPlanDetailClDayContainerFriday.setBackgroundColor(darkBlue)
                binding.workoutPlanDetailTvDayFriday.setTextColor(white)}
            6 -> {binding.workoutPlanDetailClDayContainerSaturday.setBackgroundColor(darkBlue)
                binding.workoutPlanDetailTvDaySaturday.setTextColor(white)}
        }
    }

    companion object {
        const val KEY_DETAIL = "key-detail"
    }
}