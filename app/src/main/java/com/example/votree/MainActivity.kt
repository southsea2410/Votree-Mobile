package com.example.votree

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.votree.databinding.ActivityMainBinding
import com.example.votree.users.activities.SignInActivity
import com.example.votree.utils.AuthHandler
import com.example.votree.utils.PermissionManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionManager: PermissionManager
    private lateinit var authHandler: AuthHandler

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authHandler = AuthHandler(this)
        checkUserAuthentication()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupPermissions()
        setupNavigation()
        setupLogoutButton()
    }

    private fun checkUserAuthentication() {
        if (!authHandler.isUserAuthenticated) {
            authHandler.redirectToSignIn()
            finish()
        } else {
            authHandler.storeUserIdInSharedPreferences()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar.toolbar)
        binding.toolbar.btnCart.setOnClickListener {
            navigateToCart()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setupPermissions() {
        permissionManager = PermissionManager(this)
        permissionManager.checkPermissions()
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.productList_f) as NavHostFragment
        val navController = navHostFragment.navController
        setupActionBarWithNavController(navController, AppBarConfiguration(navController.graph))
    }

    private fun navigateToCart() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.productList_f) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.cartList)
    }

    private fun setupLogoutButton() {
        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.productList_f)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}

