package org.game.xo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    /** Plays the given cell indexes (row-major) in order; each must be a legal move. */
    private fun moves(engine: GameEngine, vararg indexes: Int) {
        for (ix in indexes) {
            assertTrue(
                "move to cell $ix was rejected (gameOver=${engine.gameOver}, cell=${engine.cells[ix]})",
                engine.placeMark(ix / engine.gridSize, ix % engine.gridSize)
            )
        }
    }

    @Test
    fun `new engine starts game 1 with X in both modes`() {
        for (xFirst in listOf(false, true)) {
            for (size in listOf(3, 4)) {
                val e = GameEngine(size, xFirst)
                assertEquals(GameEngine.X, e.currentPlayer)
                assertEquals(1, e.gameNumber)
                assertFalse(e.gameOver)
                assertNull(e.statusText)
            }
        }
    }

    @Test
    fun `turns alternate X then O`() {
        val e = GameEngine(3)
        assertEquals(GameEngine.X, e.currentPlayer)
        moves(e, 0)
        assertEquals(GameEngine.O, e.currentPlayer)
        moves(e, 1)
        assertEquals(GameEngine.X, e.currentPlayer)
    }

    @Test
    fun `clicking an occupied cell changes nothing`() {
        val e = GameEngine(3)
        moves(e, 0)
        assertFalse(e.placeMark(0, 0))
        assertEquals(GameEngine.X, e.cells[0])
        assertEquals(GameEngine.O, e.currentPlayer)
        assertFalse(e.gameOver)
    }

    @Test
    fun `two in a row is not a win`() {
        val e = GameEngine(3)
        moves(e, 0, 3, 1, 4, 6, 7) // X: 0,1,6  O: 3,4,7
        assertFalse(e.gameOver)
        assertEquals(GameEngine.EMPTY, e.lastResult)
    }

    @Test
    fun `a single mark is not a win`() {
        val e = GameEngine(3)
        moves(e, 0)
        assertFalse(e.gameOver)
        assertEquals(GameEngine.EMPTY, e.lastResult)
        moves(e, 4)
        assertFalse(e.gameOver)
    }

    @Test
    fun `no win until the line completes`() {
        val e = GameEngine(3)
        moves(e, 0, 3, 1, 4) // X: 0,1  O: 3,4
        assertFalse(e.gameOver)
        moves(e, 2) // X completes the top row
        assertTrue(e.gameOver)
        assertEquals(GameEngine.X, e.lastResult)
    }

    @Test
    fun `row win is scored for X`() {
        val e = GameEngine(3)
        moves(e, 0, 3, 1, 4, 2) // X: 0,1,2  O: 3,4
        assertTrue(e.gameOver)
        assertEquals(GameEngine.X, e.lastResult)
        assertEquals(listOf(0, 1, 2), e.winningLine)
        assertEquals(1, e.xWins)
        assertEquals(0, e.oWins)
        assertEquals(listOf(GameEngine.X), e.results)
        assertEquals("X wins", e.statusText)
    }

    @Test
    fun `column win is scored for O`() {
        val e = GameEngine(3)
        moves(e, 0, 2, 1, 5, 3, 8) // X: 0,1,3  O: 2,5,8 (col 2)
        assertTrue(e.gameOver)
        assertEquals(GameEngine.O, e.lastResult)
        assertEquals(listOf(2, 5, 8), e.winningLine)
        assertEquals(0, e.xWins)
        assertEquals(1, e.oWins)
        assertEquals("O wins", e.statusText)
    }

    @Test
    fun `both diagonals win`() {
        val main = GameEngine(3)
        moves(main, 0, 1, 4, 3, 8) // X: 0,4,8  O: 1,3
        assertEquals(GameEngine.X, main.lastResult)

        val anti = GameEngine(3)
        moves(anti, 2, 0, 6, 1, 4) // X: 2,6,4  O: 0,1
        assertEquals(GameEngine.X, anti.lastResult)
    }

    @Test
    fun `full board without a line is a draw and scores nothing`() {
        val e = GameEngine(3)
        // X O X / X O X / O X O
        moves(e, 0, 1, 2, 4, 3, 6, 5, 8, 7)
        assertTrue(e.gameOver)
        assertEquals(GameEngine.EMPTY, e.lastResult)
        assertNull(e.winningLine)
        assertEquals(0, e.xWins)
        assertEquals(0, e.oWins)
        assertEquals(listOf(GameEngine.EMPTY), e.results)
        assertEquals("Draw", e.statusText)
    }

    @Test
    fun `moves after game over are ignored`() {
        val e = GameEngine(3)
        moves(e, 0, 3, 1, 4, 2) // X wins
        assertFalse(e.placeMark(2, 2))
        assertEquals(GameEngine.EMPTY, e.cells[8])
        assertEquals(1, e.results.size)
    }

    @Test
    fun `x first mode always starts with X`() {
        val e = GameEngine(3, xFirst = true)
        moves(e, 0, 3, 1, 4, 2) // game 1
        e.startGame()
        assertEquals(GameEngine.X, e.currentPlayer)
        moves(e, 0, 3, 1, 4, 2) // game 2
        e.startGame()
        assertEquals(GameEngine.X, e.currentPlayer)
        assertEquals(3, e.gameNumber)
    }

    @Test
    fun `alternating mode alternates the starting player`() {
        val e = GameEngine(3, xFirst = false)
        assertEquals(GameEngine.X, e.currentPlayer)
        moves(e, 0, 3, 1, 4, 2) // game 1: X starts
        e.startGame()
        assertEquals(GameEngine.O, e.currentPlayer) // game 2: O starts
        moves(e, 0, 3, 1, 4, 2)
        e.startGame()
        assertEquals(GameEngine.X, e.currentPlayer) // game 3: X starts
    }

    @Test
    fun `startGame resets the board but keeps the score`() {
        val e = GameEngine(3)
        moves(e, 0, 3, 1, 4, 2)
        assertEquals(listOf(0, 1, 2), e.winningLine)
        e.startGame()
        assertTrue(e.cells.all { it == GameEngine.EMPTY })
        assertFalse(e.gameOver)
        assertNull(e.statusText)
        assertNull(e.winningLine)
        assertEquals(1, e.xWins)
        assertEquals(listOf(GameEngine.X), e.results)
    }

    @Test
    fun `results accumulate across games`() {
        val e = GameEngine(3, xFirst = false)
        moves(e, 0, 3, 1, 4, 2) // game 1: X starts and wins
        e.startGame()
        moves(e, 0, 3, 1, 4, 2) // game 2: O starts and wins
        assertEquals(listOf(GameEngine.X, GameEngine.O), e.results)
        assertEquals(1, e.xWins)
        assertEquals(1, e.oWins)
    }

    @Test
    fun `4x4 requires four in a row`() {
        val e = GameEngine(4)
        moves(e, 5, 0, 6, 1, 7, 4) // X: 5,6,7 (three)  O: 0,1,4
        assertFalse(e.gameOver)
        moves(e, 8) // X: 5,6,7,8 = full row 1
        assertTrue(e.gameOver)
        assertEquals(GameEngine.X, e.lastResult)
        assertEquals(listOf(5, 6, 7, 8), e.winningLine)
    }

    @Test
    fun `4x4 diagonal win`() {
        val e = GameEngine(4)
        moves(e, 0, 1, 5, 2, 10, 3, 15) // X: 0,5,10,15 = main diagonal
        assertTrue(e.gameOver)
        assertEquals(GameEngine.X, e.lastResult)
        assertEquals(listOf(0, 5, 10, 15), e.winningLine)
    }

    @Test
    fun `4x4 two in a row is not a win`() {
        val e = GameEngine(4)
        moves(e, 0, 4, 1, 5) // X: 0,1
        assertFalse(e.gameOver)
    }

    @Test
    fun `4x4 full board without a line is a draw`() {
        val e = GameEngine(4)
        // X X O O / O O X X / X X O O / O O X X
        moves(e, 0, 2, 1, 3, 6, 4, 7, 5, 8, 10, 9, 11, 14, 12, 15, 13)
        assertTrue(e.gameOver)
        assertEquals(GameEngine.EMPTY, e.lastResult)
        assertEquals(0, e.xWins)
        assertEquals(0, e.oWins)
    }
}
