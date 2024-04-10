package com.example.votree


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.votree.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.votree.users.activities.RegisterToSeller
import com.example.votree.users.activities.SignInActivity
import com.example.votree.utils.AuthHandler
import com.example.votree.utils.PermissionManager
import com.example.votree.utils.RoleManagement
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionManager: PermissionManager
    private val bottomNavigation by lazy { findViewById<BottomNavigationView>(R.id.bottom_navigation_view) }
    private lateinit var authHandler: AuthHandler

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authHandler = AuthHandler(this)
        checkUserAuthentication()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionManager = PermissionManager(this)
        permissionManager.checkPermissions()

        setupToolbar()
        setupPermissions()
//        setupNavigation()
        setupLogoutButton()
        setupRegisterToSellerButton()

        RoleManagement.checkUserRole(firebaseAuth = authHandler.firebaseAuth, onSuccess = {
            if (it == "user") {
                Toast.makeText(this, "Welcome User", Toast.LENGTH_SHORT).show()
            } else if (it == "store") {
                Toast.makeText(this, "Welcome Seller", Toast.LENGTH_SHORT).show()
            }
        })

//        val navHostFragment =
//        supportFragmentManager.findFragmentById(R.id.main_navigation_fragment) as NavHostFragment
//        val navController = navHostFragment.navController
//        navController.setGraph(R.navigation.nav_user_graph)
//        bottomNavigation.setupWithNavController(navController)
//        bottomNavigation.inflateMenu(R.menu.nav_user)
//    }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.main_navigation_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun checkUserAuthentication() {
        if (!authHandler.isUserAuthenticated) {
            authHandler.redirectToSignIn()
            finish()
        } else {
            authHandler.storeUserIdInSharedPreferences()
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
        navController.setGraph(R.navigation.nav_user_graph)
        bottomNavigation.setupWithNavController(navController)
        bottomNavigation.inflateMenu(R.menu.nav_user)
        setupActionBarWithNavController(navController, AppBarConfiguration(navController.graph))
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar.toolbar)
        binding.toolbar.btnCart.setOnClickListener {
//            navigateToCart()
        }
    }

    private fun setupLogoutButton() {
        // Call function logout of SignInActivity
        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            SignInActivity().signOut()
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
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
        RoleManagement.updateUserRole(authHandler.firebaseAuth, role)
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
