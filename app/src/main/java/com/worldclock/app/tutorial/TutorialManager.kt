package com.worldclock.app.tutorial

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.worldclock.app.R
import com.worldclock.app.databinding.TutorialOverlayBinding

class TutorialManager(private val activity: Activity) {
    
    private val prefs: SharedPreferences = activity.getSharedPreferences("tutorial_prefs", Context.MODE_PRIVATE)
    private var currentStep = 0
    private var tutorialOverlay: View? = null
    private var binding: TutorialOverlayBinding? = null
    
    companion object {
        private const val TUTORIAL_COMPLETED_KEY = "tutorial_completed"
        private const val TUTORIAL_STEP_KEY = "tutorial_step"
        
        // Шаги обучения
        const val STEP_WELCOME = 0
        const val STEP_ADD_METER = 1
        const val STEP_ADD_TARIFF = 2
        const val STEP_ADD_READINGS = 3
        const val STEP_VIEW_MAIN = 4
        const val STEP_CLICK_ADDRESS = 5
        const val STEP_CLICK_METER = 6
        const val STEP_ADD_READING_FROM_COSTS = 7
        const val STEP_COMPLETE = 8
        
        private val TUTORIAL_STEPS = listOf(
            TutorialStep(
                step = STEP_WELCOME,
                message = "Добро пожаловать в приложение \"Домовой\"!\n\nЭто приложение поможет вам управлять приборами учета коммунальных услуг. Давайте пройдем краткое обучение, чтобы вы могли быстро начать работу.",
                showArrow = false
            ),
            TutorialStep(
                step = STEP_ADD_METER,
                message = "Начнем с добавления прибора учета. \n\nПерейдите в раздел \"Приборы учета\" через меню и Нажмите на кнопку \"+\" в правом нижнем углу, чтобы добавить новый счетчик. Введите номер прибора и адрес установки.",
                showArrow = true,
                targetViewId = R.id.fabAddMeter
            ),
            TutorialStep(
                step = STEP_ADD_TARIFF,
                message = "Отлично! После добавления прибора система предложит добавить тариф.\n\nТариф необходим для расчета затрат. Вы можете добавить его сейчас или пропустить и сделать это позже.",
                showArrow = false
            ),
            TutorialStep(
                step = STEP_ADD_READINGS,
                message = "Теперь добавим показания счетчика.\n\nПерейдите в раздел \"Показания\" через меню и добавьте два показания: предыдущее и текущее. Это нужно для расчета потребления.",
                showArrow = true,
                targetViewId = R.id.menuItemReadings
            ),
            TutorialStep(
                step = STEP_VIEW_MAIN,
                message = "Вернемся на главную страницу и посмотрим результат.\n\nЗдесь вы увидите сгруппированные затраты по адресам установки приборов.",
                showArrow = true,
                targetViewId = R.id.imageViewBackButton
            ),
            TutorialStep(
                step = STEP_CLICK_ADDRESS,
                message = "Нажмите на любую плашку с адресом.\n\nЭто откроет детализацию затрат по отдельным приборам учета для выбранного адреса.",
                showArrow = true,
                targetViewId = R.id.recyclerViewCosts
            ),
            TutorialStep(
                step = STEP_CLICK_METER,
                message = "Теперь нажмите на затраты по конкретному прибору.\n\nЭто покажет всю историю переданных показаний для выбранного счетчика.",
                showArrow = true,
                targetViewId = R.id.recyclerViewMeters
            ),
            TutorialStep(
                step = STEP_ADD_READING_FROM_COSTS,
                message = "Обратите внимание на кнопку внизу экрана.\n\nС её помощью можно быстро добавить новые показания прямо с этой страницы, не переходя в другие разделы.",
                showArrow = true,
                targetViewId = R.id.fabAddReading
            ),
            TutorialStep(
                step = STEP_COMPLETE,
                message = "Поздравляем! Вы прошли обучение.\n\nТеперь вы знаете, как пользоваться приложением \"Домовой\". Желаем успехов в управлении приборами учета!",
                showArrow = false
            )
        )
    }
    
    data class TutorialStep(
        val step: Int,
        val message: String,
        val showArrow: Boolean,
        val targetViewId: Int? = null
    )
    
    fun isTutorialCompleted(): Boolean {
        return prefs.getBoolean(TUTORIAL_COMPLETED_KEY, false)
    }
    
    fun startTutorial() {
        currentStep = prefs.getInt(TUTORIAL_STEP_KEY, 0)
        showTutorialStep()
    }
    
    fun nextStep() {
        currentStep++
        if (currentStep >= TUTORIAL_STEPS.size) {
            completeTutorial()
        } else {
            prefs.edit().putInt(TUTORIAL_STEP_KEY, currentStep).apply()
            showTutorialStep()
        }
    }
    
    fun skipTutorial() {
        completeTutorial()
    }
    
    fun completeTutorial() {
        prefs.edit()
            .putBoolean(TUTORIAL_COMPLETED_KEY, true)
            .putInt(TUTORIAL_STEP_KEY, 0)
            .apply()
        hideTutorial()
    }
    
    fun hideTutorial() {
        tutorialOverlay?.let { overlay ->
            val parent = overlay.parent as? ViewGroup
            parent?.removeView(overlay)
        }
        tutorialOverlay = null
        binding = null
    }
    
    private fun showTutorialStep() {
        if (currentStep >= TUTORIAL_STEPS.size) {
            completeTutorial()
            return
        }
        
        val step = TUTORIAL_STEPS[currentStep]
        showTutorialOverlay(step)
    }
    
    private fun showTutorialOverlay(step: TutorialStep) {
        hideTutorial()
        
        val inflater = LayoutInflater.from(activity)
        tutorialOverlay = inflater.inflate(R.layout.tutorial_overlay, null)
        binding = TutorialOverlayBinding.bind(tutorialOverlay!!)
        
        // Настраиваем содержимое
        binding?.textStepCounter?.text = "${currentStep + 1}/${TUTORIAL_STEPS.size}"
        binding?.textTutorialMessage?.text = step.message
        
        // Настраиваем кнопки
        binding?.buttonNextTutorial?.setOnClickListener {
            nextStep()
        }
        
        binding?.buttonSkipTutorial?.setOnClickListener {
            skipTutorial()
        }
        
        // Показываем стрелку если нужно
        if (step.showArrow && step.targetViewId != null) {
            binding?.imageArrow?.visibility = View.VISIBLE
            // Здесь можно добавить логику позиционирования стрелки
        } else {
            binding?.imageArrow?.visibility = View.GONE
        }
        
        // Добавляем overlay к корневому view
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        rootView.addView(tutorialOverlay)
    }
    
    fun getCurrentStep(): Int = currentStep
    
    fun isTutorialActive(): Boolean = tutorialOverlay != null
}