package com.strange_1.one_button_a_day

import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.ads.*
import com.google.android.material.navigation.NavigationView
import com.instacart.library.truetime.TrueTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var progressBar: ProgressBar
    private lateinit var preferences: SharedPreferences
    lateinit var adView: AdView
    private lateinit var adRequest: AdRequest
    lateinit var button: ImageButton

    private var scope = CoroutineScope(Dispatchers.Default)
    private lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MobileAds.initialize(baseContext)
        scope.launch {
            TrueTime.build().initialize()
        }
        initVariables()
        initLayout()
        initSettings()
        initAd()
    }

    private fun initVariables() {
        drawerLayout = findViewById(R.id.dl_main_drawer_root)
        navigationView = findViewById(R.id.nv_main_navigation_root)
        toolbar = findViewById(R.id.main_toolbar)
        drawerToggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close)
        progressBar = findViewById(R.id.main_content_progress)
        button = findViewById(R.id.main_content_button)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Toast.makeText(this,
                when (item.itemId) {
                    R.id.item_settings -> "settings"
                    R.id.item_progress -> "progress"
                    R.id.item_about -> "about"
                    else -> ""
                }, Toast.LENGTH_SHORT).show()
        drawerLayout.closeDrawer(GravityCompat.START)
        return false
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }

    private fun initLayout() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp)

        drawerLayout.addDrawerListener(drawerToggle)
        navigationView.setNavigationItemSelectedListener(this)

        button.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                button.setImageResource(
                        if (event!!.action == MotionEvent.ACTION_DOWN) R.drawable.ic_red_button_pushed
                        else R.drawable.ic_red_button
                )
                return false
            }
        })

    }

    private fun initSettings() {

        preferences = getPreferences(MODE_PRIVATE)

        /*settings = Settings(
                "{\"id\":\"8637131717293890311\",\"FirstRun\":true,\"Time\":0,\"HighScore\":0,\"PushList\":[]}"
                )*/

        settings = Settings(preferences.getString("Settings", null))

        Log.d("DATA", settings.getData())


    }

    private fun initAd() {
        adView = findViewById(R.id.main_content_adView)
        adView.adUnitId = R.string.banner_ad_unit_id_for_test.toString()
        adView.adSize = AdSize.BANNER
        adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(p0: LoadAdError?) {
                adView.visibility = View.INVISIBLE
                super.onAdFailedToLoad(p0)
            }
        }
        adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun getTime(): Long {
        return TrueTime.now().time
    }

    fun push(view: View) {
        if (TrueTime.isInitialized()) {
            findViewById<ImageView>(R.id.main_content_connectionError).visibility = View.INVISIBLE
            StepProgress(100)
        } else
            findViewById<ImageView>(R.id.main_content_connectionError).visibility = View.VISIBLE

        /* 시간 계산. 현 시간이 정해진 시간에서 미래 방향으로 1시간 이내인지 확인 */
        if (getTime() % (1000L * 60 * 60 * 24) - settings.dailyTime < (1000L * 60 * 60)
                && getTime() % (1000L * 60 * 60 * 24) - settings.dailyTime >= 0) {
            // 맞다면, 연속으로 며칠째인지 계산하고 리스트에 기록
            // 연속 입력이 깨졌다면 카운트와 기록을 초기화하고 처음부터 시작
            settings.pushList
        }
        /* 아니라면, 에러 메세지 표시 */
    }

    private fun changeProgress(value: Int) {
        val max: Double = progressBar.max.toDouble()
        progressBar.progress = value
        progressBar.progressTintList =
                ColorStateList.valueOf(/*Alpha*/(0xFF000000
                        /*Red*/ + ((0xFF * (value / max)).toInt() * 0x00010000)
                        /*Green*/ + ((0xFF * (max - value) / max).toInt() * 0x00000100)).toInt())
    }

    private fun StepProgress(value: Int) {
        changeProgress(
                if (progressBar.progress + value > progressBar.max)
                    progressBar.max
                else
                    progressBar.progress + value
        )
    }

}