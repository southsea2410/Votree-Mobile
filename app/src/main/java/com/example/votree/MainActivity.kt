package com.example.votree

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.databinding.ActivityMainBinding
import com.example.votree.users.activities.RegisterToSeller
import com.example.votree.users.activities.StoreManagement
import com.example.votree.utils.AuthHandler
import com.example.votree.utils.PermissionManager
import com.example.votree.utils.RoleManagement
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

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

        permissionManager = PermissionManager(this)
        permissionManager.checkPermissions()

        setupPermissions()

        RoleManagement.checkUserRole(firebaseAuth = AuthHandler.firebaseAuth, onSuccess = {
            role = it ?: ""
            setupToolbar()
            setupNavigation()

            when (it) {
                "user" -> Toast.makeText(this, "Welcome User", Toast.LENGTH_SHORT).show()
                "store" -> Toast.makeText(this, "Welcome Seller", Toast.LENGTH_SHORT).show()
                "admin" -> {
                    val intent = Intent(this, AdminMainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        })
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

        if (role == "store") {
            navController.setGraph(R.navigation.nav_seller_graph)
            bottomNavigation.inflateMenu(R.menu.nav_seller)
        } else {
            navController.setGraph(R.navigation.nav_user_graph)
            bottomNavigation.inflateMenu(R.menu.nav_user)
        }
        val hiddenDestinations = setOf(R.id.productDetail2, R.id.productDetail)
        navController.addOnDestinationChangedListener { _ , destination, _  ->
            if(destination.id in hiddenDestinations) {
                bottomNavigation.visibility = View.GONE
            } else {
                bottomNavigation.visibility = View.VISIBLE
            }
        }

        bottomNavigation.setupWithNavController(navController)
        setupActionBarWithNavController(navController, AppBarConfiguration(navController.graph))
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar.toolbar)
        binding.toolbar.btnCart.setOnClickListener {
            navigateToCart()
        }
        gotoAccountManagement()
    }

    private fun navigateToCart() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_navigation_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.cartList)
    }

    private fun gotoAccountManagement() {
        binding.toolbar.btnAvatar.setOnClickListener {
            RoleManagement.checkUserRole(firebaseAuth = AuthHandler.firebaseAuth, onSuccess = {
                if (it == "user") {
                    Log.d("MainActivity", "User")
                } else if (it == "store") {
                    Log.d("MainActivity", "Store")
                    // Start Activity StoreManagement
                    val intent = Intent(this, StoreManagement::class.java)
                    startActivity(intent)
                    finish()
                }
            })
        }
    }

    private fun setupRegisterToSellerButton() {
        // Call function registerToSeller of SignInActivity
        findViewById<FloatingActionButton>(R.id.registerToSeller_btn).setOnClickListener {
            val intent = Intent(this, RegisterToSeller::class.java)
            startActivityForResult(intent, RegisterToSeller.REGISTER_TO_SELLER_CODE)
        }
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

