package com.example.votree.admin.activities

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.votree.R
import com.example.votree.admin.adapters.AccountListAdapter
import com.example.votree.admin.adapters.ReportListAdapter
import com.example.votree.admin.adapters.TipListAdapter
import com.example.votree.admin.fragments.AccountListFragment
import com.example.votree.admin.fragments.ReportListFragment
import com.example.votree.admin.fragments.TipListFragment
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.models.Report
import com.example.votree.models.Tip
import com.example.votree.models.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Suppress("DEPRECATION")
class AdminMainActivity : AppCompatActivity(), OnItemClickListener, SearchView.OnQueryTextListener, NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var topAppBar: MaterialToolbar
    private val db = Firebase.firestore
    private val tipAdapter: TipListAdapter by lazy { TipListAdapter(this) }
    private val accountAdapter: AccountListAdapter by lazy { AccountListAdapter(this) }
    private val reportAdapter: ReportListAdapter by lazy { ReportListAdapter(this) }
    private val tipList = mutableListOf<Tip>()
    private val accountList = mutableListOf<User>()
    private val reportList = mutableListOf<Report>()
    private val SharedPrefs = "sharedPrefs"
    private var currentFragment: Fragment? = null
    private var currentFlag: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_admin)

        drawerLayout = findViewById(R.id.mainLayout)
        topAppBar = findViewById(R.id.topAppBar)
        setSupportActionBar(topAppBar)

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.more -> {
                    Log.d("ListActivity", "More clicked")
                    true
                }
                else -> false
            }
        }

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {

                when (currentFlag) {
                    getCurrentFlag() -> return
                    else -> {
                        setCurrentFlag(currentFlag)
                    }
                }

                currentFragment = when (getCurrentFlag()) {
                    0 -> TipListFragment()
                    1 -> AccountListFragment()
                    2 -> ReportListFragment()
                    else -> TipListFragment()
                }

                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, currentFragment!!)
                    .addToBackStack(null)
                    .commit()

            }

            override fun onDrawerStateChanged(newState: Int) {}
        })

        val toggle = ActionBarDrawerToggle(this, drawerLayout, topAppBar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val defaultFragment = when (getCurrentFlag()) {
            0 -> TipListFragment()
            1 -> AccountListFragment()
            2 -> ReportListFragment()
            else -> TipListFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, defaultFragment)
            .addToBackStack(null)
            .commit()

        fetchDataFromFirestore()

        supportFragmentManager.addOnBackStackChangedListener(backStackListener)
    }

    override fun onDestroy() {
        // Remove the listener when the activity is destroyed to avoid memory leaks
        supportFragmentManager.removeOnBackStackChangedListener(backStackListener)
        super.onDestroy()
    }

    override fun onTipItemClicked(view: View?, position: Int) {

        val topAppBar: MaterialToolbar = findViewById(R.id.topAppBar)
        topAppBar.menu.clear()
        topAppBar.setNavigationIcon(R.drawable.icon_back)
        topAppBar.setNavigationOnClickListener {
            setupNormalActionBar()
            supportFragmentManager.popBackStack()
        }
    }

    override fun onAccountItemClicked(view: View?, position: Int) {

        val topAppBar: MaterialToolbar = findViewById(R.id.topAppBar)
        topAppBar.menu.clear()
        topAppBar.setNavigationIcon(R.drawable.icon_back)
        topAppBar.setNavigationOnClickListener {
            setupNormalActionBar()
            supportFragmentManager.popBackStack()
        }
    }

    override fun onReportItemClicked(view: View?, position: Int) {

        val topAppBar: MaterialToolbar = findViewById(R.id.topAppBar)
        topAppBar.menu.clear()
        topAppBar.setNavigationIcon(R.drawable.icon_back)
        topAppBar.setNavigationOnClickListener {
            setupNormalActionBar()
            supportFragmentManager.popBackStack()
        }
    }

    override fun searchItem(query: String) { }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.top_app_bar, menu)

        val search = menu?.findItem(R.id.search)
        val searchView = search?.actionView as SearchView
        searchView.isSubmitButtonEnabled = true
        searchView.setOnQueryTextListener(this)

        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            val fragment = when (getCurrentFlag()) {
                0 -> supportFragmentManager.findFragmentById(R.id.fragment_container) as? TipListFragment
                1 -> supportFragmentManager.findFragmentById(R.id.fragment_container) as? AccountListFragment
                2 -> supportFragmentManager.findFragmentById(R.id.fragment_container) as? ReportListFragment
                else -> supportFragmentManager.findFragmentById(R.id.fragment_container) as? TipListFragment
            }
            fragment?.searchItem(query)
        }
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) {
            val fragment = when (getCurrentFlag()) {
                0 -> supportFragmentManager.findFragmentById(R.id.fragment_container) as? TipListFragment
                1 -> supportFragmentManager.findFragmentById(R.id.fragment_container) as? AccountListFragment
                2 -> supportFragmentManager.findFragmentById(R.id.fragment_container) as? ReportListFragment
                else -> supportFragmentManager.findFragmentById(R.id.fragment_container) as? TipListFragment
            }
            fragment?.searchItem(query)
        }
        return true
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        when (p0.itemId) {
            R.id.nav_tips -> {
                currentFlag = 0
                drawerLayout.closeDrawer(GravityCompat.START)
                topAppBar.title = "Tips"

                return true
            }
            R.id.nav_accounts -> {
                currentFlag = 1
                drawerLayout.closeDrawer(GravityCompat.START)
                topAppBar.title = "Accounts"

                return true
            }
            R.id.nav_reports -> {
                currentFlag = 2
                drawerLayout.closeDrawer(GravityCompat.START)
                topAppBar.title = "Reports"

                return true
            }
            else -> return false
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        setupNormalActionBar()

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
            return
        }

        super.onBackPressed()
    }

    private fun getCurrentFlag() : Int {
        val sharedPreferences = getSharedPreferences(SharedPrefs, MODE_PRIVATE)
        return sharedPreferences.getInt("currentFlag", 0)
    }

    private fun setCurrentFlag(currentFlag: Int) {
        val sharedPreferences = getSharedPreferences(SharedPrefs, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("currentFlag", currentFlag)
        editor.apply()
    }

    private fun fetchTipFromFirestore() {
        db.collection("ProductTip2")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("TipListActivity", "listen:error", e)
                    return@addSnapshotListener
                }

                tipList.clear()

                for (doc in snapshots!!) {
                    val tip = doc.toObject(Tip::class.java)
                    tip.id = doc.id
                    tipList.add(tip)
                }

                tipAdapter.setData(tipList)
            }
    }

    private fun fetchAccountFromFirestore() {
        db.collection("users")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("AccountListActivity", "listen:error", e)
                    return@addSnapshotListener
                }

                tipList.clear()

                for (doc in snapshots!!) {
                    val account = doc.toObject(User::class.java)
                    account.id = doc.id
                    accountList.add(account)
                }

                accountAdapter.setData(accountList)
            }
    }

    private fun fetchReportFromFirestore() {
        db.collection("reports")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("ReportListActivity", "listen:error", e)
                    return@addSnapshotListener
                }

                reportList.clear()

                for (doc in snapshots!!) {
                    val report = doc.toObject(Report::class.java)
                    report.id = doc.id
                    reportList.add(report)
                }

                reportAdapter.setData(reportList)
            }
    }

    private fun fetchDataFromFirestore() {
        when(getCurrentFlag()) {
            0 -> fetchTipFromFirestore()
            1 -> fetchAccountFromFirestore()
            2 -> fetchReportFromFirestore()
            else -> fetchTipFromFirestore()
        }
    }

    private val backStackListener = FragmentManager.OnBackStackChangedListener {
        if (supportFragmentManager.backStackEntryCount == 0) {
            setupNormalActionBar()
        }
    }

    fun setupNormalActionBar() {
        setSupportActionBar(topAppBar)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, topAppBar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.more -> {
                    Log.d("ListActivity", "More clicked")
                    true
                }
                else -> false
            }
        }
    }
}
