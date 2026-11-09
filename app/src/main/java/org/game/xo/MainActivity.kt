package org.game.xo

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_GRID_SIZE = "grid_size"
        const val EXTRA_X_FIRST = "x_first"
    }

    // default selections: 3x3 and alternating turns
    private var gridSize = 3
    private var xFirst = false

    private lateinit var size3: TextView
    private lateinit var size4: TextView
    private lateinit var modeAlt: TextView
    private lateinit var modeX: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        size3 = findViewById(R.id.optionSize3)
        size4 = findViewById(R.id.optionSize4)
        modeAlt = findViewById(R.id.optionModeAlternating)
        modeX = findViewById(R.id.optionModeXFirst)

        size3.setOnClickListener { selectSize(3) }
        size4.setOnClickListener { selectSize(4) }
        modeAlt.setOnClickListener { selectMode(false) }
        modeX.setOnClickListener { selectMode(true) }

        findViewById<TextView>(R.id.startButton).setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra(EXTRA_GRID_SIZE, gridSize)
            intent.putExtra(EXTRA_X_FIRST, xFirst)
            startActivity(intent)
        }

        refreshSelection()
    }

    private fun selectSize(size: Int) {
        gridSize = size
        refreshSelection()
    }

    private fun selectMode(xFirstMode: Boolean) {
        xFirst = xFirstMode
        refreshSelection()
    }

    private fun refreshSelection() {
        size3.setBackgroundResource(
            if (gridSize == 3) R.drawable.bg_option_selected else R.drawable.bg_option
        )
        size4.setBackgroundResource(
            if (gridSize == 4) R.drawable.bg_option_selected else R.drawable.bg_option
        )
        modeAlt.setBackgroundResource(
            if (!xFirst) R.drawable.bg_option_selected else R.drawable.bg_option
        )
        modeX.setBackgroundResource(
            if (xFirst) R.drawable.bg_option_selected else R.drawable.bg_option
        )
    }
}
