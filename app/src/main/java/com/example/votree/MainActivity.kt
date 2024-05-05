package com.example.votree

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.databinding.ActivityMainBinding
import com.example.votree.tips.AdManager
import com.example.votree.users.activities.MyFirebaseMessagingService
import com.example.votree.users.activities.RegisterToSeller
import com.example.votree.utils.AuthHandler
import com.example.votree.utils.PermissionManager
import com.example.votree.utils.RoleManagement
import com.google.android.gms.ads.AdView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionManager: PermissionManager
    private val bottomNavigation by lazy { findViewById<BottomNavigationView>(R.id.bottom_navigation_view) }
    private var role = ""

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkUserAuthentication()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_activity_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val adView = findViewById<AdView>(R.id.adView)
        AdManager.loadBannerAd(adView)

        permissionManager = PermissionManager(this)
        permissionManager.checkPermissions()

        setupPermissions()

        RoleManagement.checkUserRole(firebaseAuth = AuthHandler.firebaseAuth, onSuccess = {
            role = it ?: ""
            when (it) {
                "user" -> {
                    Toast.makeText(this, "Welcome User", Toast.LENGTH_SHORT).show()
                    setupNavigation()
                }
                "store" -> {
                    Toast.makeText(this, "Welcome Seller", Toast.LENGTH_SHORT).show()
                    setupNavigation()
                }
                "admin" -> {
                    val intent = Intent(this, AdminMainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        })

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Manually invoke onNewToken
                Log.d("FCM Token", task.result)
                val newToken = task.result
                MyFirebaseMessagingService().onNewToken(newToken)
            }
            else {
                Toast.makeText(this, "Failed to get token", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.main_navigation_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun checkUserAuthentication() {
        if (!AuthHandler.isUserAuthenticated) {
            AuthHandler.redirectToSignIn(this)
            finish()
        } else {
            AuthHandler.storeUserIdInSharedPreferences(this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setupPermissions() {
        permissionManager = PermissionManager(this)
        permissionManager.checkPermissions()
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_navigation_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.setGraph(R.navigation.nav_seller_graph)
        bottomNavigation.inflateMenu(R.menu.nav_seller)

        if (role != "store") {
            bottomNavigation.menu.removeItem(R.id.storeManagement2)
        }
        val showDestinations = setOf(R.id.productList, R.id.main_tip_fragment, R.id.user_profile_fragment, R.id.notifications_fragment, R.id.storeManagement2, R.id.pointTransactionFragment, R.id.orderDetailsFragment, R.id.orderManagementForStoreFragment)
        navController.addOnDestinationChangedListener { _ , destination, _  ->
            if(destination.id in showDestinations) {
                bottomNavigation.visibility = View.VISIBLE
            } else {
                bottomNavigation.visibility = View.GONE
            }
        }

        bottomNavigation.setupWithNavController(navController)
    }

    private fun updateUserToSeller(role: String){
        // If RegisterToSeller activity is successful, and return the role as store, then update the user role to store
        RoleManagement.updateUserRole(AuthHandler.firebaseAuth, role)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RegisterToSeller.REGISTER_TO_SELLER_CODE && resultCode == RESULT_OK) {
            val role = data?.getStringExtra("role")
            if (role == "store") {
                updateUserToSeller(role)
            }
        }
    }
}

