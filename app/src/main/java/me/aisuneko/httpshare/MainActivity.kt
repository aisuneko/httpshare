package me.aisuneko.httpshare

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_first.*
import kotlinx.android.synthetic.main.activity_main.fab
import me.aisuneko.httpshare.databinding.ActivityMainBinding
import java.io.IOException

const val PICK_CODE = 114

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var webServer: Server? = null
    private var isServerOn = false
//    private fun log(str: String) {
//        textview_first.text = textview_first.text.toString() + "\n" + str
//    }
    private fun closeServer(){
        if (this.webServer != null) {
            this.webServer?.closeAllConnections()
            this.webServer?.stop()
            this.isServerOn = false
            this.webServer = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener {
            val intent = Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)
            if (!isServerOn) {
                startActivityForResult(Intent.createChooser(intent, "Select a file"), PICK_CODE)
            } else {
                val start_icon = this.resources.getIdentifier("@android:drawable/ic_media_play",null,null)
                fab.setImageResource(start_icon)
                Snackbar.make(binding.root, "Server shutdown", Snackbar.LENGTH_LONG)
                    .setAnchorView(R.id.fab)
                    .setAction("Action", null).show()
                textview_first.text = getString(R.string.idle)
                closeServer()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeServer()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_CODE && resultCode == RESULT_OK) {
            try {
                val selectedFile = data?.data // The URI with the location of the file
                val path = selectedFile?.path
                if (path != null) {
                    val name = selectedFile.lastPathSegment
                    webServer = Server(selectedFile, this)
                    webServer!!.start()
                    isServerOn = true
                    val serverRunningStr = getString(
                        R.string.server_running,
                        NetworkUtils.getLocalIpAddress(),
                        webServer?.listeningPort
                    )
                    textview_first.text = name + "\n" + serverRunningStr
                    val stop_icon = this.resources.getIdentifier("@android:drawable/ic_media_stop",null,null)
                    fab.setImageResource(stop_icon)
                    Snackbar.make(binding.root, serverRunningStr, Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show()

                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}