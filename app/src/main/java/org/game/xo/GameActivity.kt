package org.game.xo

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {

    private lateinit var board: BoardView
    private lateinit var scoreboard: ScoreboardView
    private lateinit var turnText: TextView
    private lateinit var resultText: TextView
    private lateinit var nextGameButton: TextView
    private lateinit var engine: GameEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val gridSize = intent.getIntExtra(MainActivity.EXTRA_GRID_SIZE, 3)
        val xFirst = intent.getBooleanExtra(MainActivity.EXTRA_X_FIRST, false)
        engine = GameEngine(gridSize, xFirst)

        board = findViewById(R.id.board)
        scoreboard = findViewById(R.id.scoreboard)
        turnText = findViewById(R.id.turnText)
        resultText = findViewById(R.id.resultText)
        nextGameButton = findViewById(R.id.nextGameButton)
        findViewById<TextView>(R.id.title).text =
            "${gridSize}x$gridSize - ${if (xFirst) "X first" else "alternating"}"

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }
        nextGameButton.setOnClickListener {
            engine.startGame()
            syncViews()
        }

        board.gridSize = gridSize
        board.onCellClick = { row, col -> onCellClick(row, col) }

        syncViews()
    }

    private fun onCellClick(row: Int, col: Int) {
        if (!engine.placeMark(row, col)) return
        syncViews()
    }

    private fun syncViews() {
        board.cells = engine.cells
        board.winningLine = engine.winningLine
        scoreboard.xTotal = engine.xWins
        scoreboard.oTotal = engine.oWins
        scoreboard.results = engine.results
        scoreboard.activePlayer = when {
            !engine.gameOver -> engine.currentPlayer
            engine.lastResult == GameEngine.X -> GameEngine.X
            engine.lastResult == GameEngine.O -> GameEngine.O
            else -> 0
        }
        if (engine.gameOver) {
            turnText.visibility = View.GONE
            resultText.text = engine.statusText
            resultText.visibility = View.VISIBLE
            // keep the button in place: hidden and inert during the game,
            // visible and active when it ends
            nextGameButton.alpha = 1f
            nextGameButton.isEnabled = true
        } else {
            turnText.text = getString(
                if (engine.currentPlayer == GameEngine.X) R.string.turn_x else R.string.turn_o
            )
            turnText.visibility = View.VISIBLE
            resultText.visibility = View.GONE
            nextGameButton.alpha = 0f
            nextGameButton.isEnabled = false
        }
        scoreboard.invalidate()
        board.invalidate()
    }
}
