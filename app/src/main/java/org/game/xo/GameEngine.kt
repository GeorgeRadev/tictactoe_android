package org.game.xo

/**
 * Pure tic-tac-toe game state, no Android dependencies.
 *
 * Rules: a full row of [winLength] wins (3 on 3x3, 4 on 4x4); a full board
 * without a line is a draw; in "alternating" mode the starting player
 * alternates each game, in "xFirst" mode X always starts.
 */
class GameEngine(val gridSize: Int = 3, val xFirst: Boolean = false) {

    companion object {
        const val EMPTY = 0
        const val X = 1
        const val O = 2

        /** (dr, dc) of the four line directions: horizontal, vertical, both diagonals. */
        private val DIRECTIONS = arrayOf(0 to 1, 1 to 0, 1 to 1, 1 to -1)
    }

    val cells = IntArray(gridSize * gridSize)

    /** Number of marks in a row needed to win: 3 on 3x3, 4 on 4x4. */
    val winLength: Int get() = gridSize

    /** 1-based number of the game that is in progress. */
    var gameNumber = 0
        private set

    var currentPlayer = X
        private set

    var gameOver = false
        private set

    /** Result of the last finished game: [X], [O], or [EMPTY] for a draw. */
    var lastResult = EMPTY
        private set

    var xWins = 0
        private set

    var oWins = 0
        private set

    /** Results of all finished games, oldest first: [X], [O], or [EMPTY] for a draw. */
    val results = mutableListOf<Int>()

    /** Cell indexes of the winning line, or null (cleared when the game resets). */
    var winningLine: List<Int>? = null
        private set

    init {
        require(gridSize == 3 || gridSize == 4) { "unsupported grid size: $gridSize" }
        startGame()
    }

    val statusText: String?
        get() = when {
            !gameOver -> null
            lastResult == X -> "X wins"
            lastResult == O -> "O wins"
            else -> "Draw"
        }

    /** Player that starts game number [gameIx] (0-based). */
    fun startingPlayer(gameIx: Int): Int = if (xFirst) X else if (gameIx % 2 == 0) X else O

    /** Clears the board and starts the next game. */
    fun startGame() {
        cells.fill(EMPTY)
        gameOver = false
        lastResult = EMPTY
        winningLine = null
        currentPlayer = startingPlayer(gameNumber)
        gameNumber++
    }

    /**
     * Places the current player's mark at (row, col). Returns false (and changes
     * nothing) when the game is over or the cell is already occupied.
     */
    fun placeMark(row: Int, col: Int): Boolean {
        if (gameOver) return false
        if (row !in 0 until gridSize || col !in 0 until gridSize) return false
        val ix = row * gridSize + col
        if (cells[ix] != EMPTY) return false

        cells[ix] = currentPlayer

        val win = checkWinner()
        when {
            win != null -> endGame(win)
            cells.none { it == EMPTY } -> endGame(null) // draw
            else -> currentPlayer = if (currentPlayer == X) O else X
        }
        return true
    }

    private fun endGame(win: Win?) {
        val result = win?.player ?: EMPTY
        gameOver = true
        lastResult = result
        winningLine = win?.line
        results.add(result)
        when (result) {
            X -> xWins++
            O -> oWins++
        }
    }

    /** Returns the win (player + its [winLength] cell indexes) when there is one, else null. */
    private fun checkWinner(): Win? {
        val n = winLength
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                for ((dr, dc) in DIRECTIONS) {
                    val r1 = row + dr * (n - 1)
                    val c1 = col + dc * (n - 1)
                    if (r1 !in 0 until gridSize || c1 !in 0 until gridSize) continue
                    lineWinner(row, col, dr, dc)?.let { return it }
                }
            }
        }
        return null
    }

    /**
     * The win of the uniform [winLength]-cell line from (r0, c0) in direction
     * (dr, dc), or null when the line is not a uniform mark. Only full-length
     * lines count, so on 4x4 only the two corner-to-corner diagonals can win.
     */
    private fun lineWinner(r0: Int, c0: Int, dr: Int, dc: Int): Win? {
        var mark = EMPTY
        val n = winLength
        for (k in 0 until n) {
            val v = cells[(r0 + dr * k) * gridSize + (c0 + dc * k)]
            when {
                v == EMPTY -> return null
                mark == EMPTY -> mark = v
                mark != v -> return null
            }
        }
        val line = (0 until n).map { (r0 + dr * it) * gridSize + (c0 + dc * it) }
        return Win(mark, line)
    }

    private class Win(val player: Int, val line: List<Int>)
}
