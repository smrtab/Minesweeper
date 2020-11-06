package minesweeper

import java.lang.Exception
import java.util.*
import kotlin.random.Random
import kotlin.random.nextInt

class MinesweeperStatusException(message: String) : Exception(message)

class Minesweeper(val dimension: Int, val minesCount: Int) {

    private val mineField: IntArray = IntArray(dimension * dimension)
    private var gameField: IntArray = IntArray(dimension * dimension)

    companion object State {

        var inProcess: Boolean = true
        var statusMessage: String? = ""

        var mineSign: Char = 'X'
        var emptySign: Char = '.'
        var openSign: Char = '/'
        var clueSign: Char = '*'

        fun getCellSign(cell: Int): Char {
            return when (cell) {
                0 -> emptySign
                -1 -> mineSign
                -2 -> clueSign
                -3 -> openSign
                else -> cell.toString()[0]
            }
        }
    }

    init {
        var counter = 1
        while (counter <= this.minesCount) {

            var index = Random.nextInt(0..this.mineField.lastIndex)

            if (isMined(index))
                continue

            this.mineField[index] = -1

            var shift = if (isLeftVerticalSide(index)) 0 else -1
            var count = if (isVerticalSide(index)) 1 else 2

            updateCells(from = index - this.dimension + shift, count = count)
            updateCells(from = index + shift, count = count)
            updateCells(from = index + this.dimension + shift, count = count)

            counter++
        }
    }

    fun move(pointX: Int, pointY: Int, type: String) {

        try {
            var index = this.dimension * (pointY - 1) + (pointX - 1)
            if (type == "free") freeCell(index) else mineCell(index)
            this.checkState()
        } catch (e: MinesweeperStatusException) {
            State.inProcess = false
            State.statusMessage = e.message
        }

        this.drawField()
    }

    /**
     * Simply draw a field
     */
    fun drawField() {

        for (i in 1..this.dimension) {
            when (i) {
                1 -> print(" │$i")
                this.dimension -> print("$i│\n")
                else -> print(i)
            }
        }

        println("—│" + "—".repeat(this.dimension) + "│")

        for (i in this.gameField.indices) {
            var cellState = State.getCellSign(this.gameField[i])
            when {
                i % this.dimension == 0 -> print("${i / this.dimension + 1}│$cellState")
                (i + 1) % this.dimension == 0 -> print("$cellState│\n")
                else -> print(cellState)
            }
        }

        println("—|" + "—".repeat(this.dimension) + "|")
    }


    private fun openCell(index: Int) {

        var shift = if (isLeftVerticalSide(index)) 0 else -1
        var count = if (isVerticalSide(index)) 1 else 2

        updateCellsAround(from = index - this.dimension + shift, count)
        updateCellsAround(from = index + shift, count)
        updateCellsAround(from = index + this.dimension + shift, count)
    }

    /**
     * Free cell type has been changed
     */
    private fun freeCell(index: Int) {
        when {
            this.mineField[index] == -1 -> {
                for (i in 0..this.mineField.lastIndex) {
                    if (this.mineField[i] == -1)
                        this.gameField[i] = -1
                }
                throw MinesweeperStatusException("You stepped on a mine and failed!")
            }
            this.mineField[index] > 0 -> this.gameField[index] = this.mineField[index]
            else -> openCell(index)
        }
    }

    /**
     * Mine cell type has been changed
     */
    private fun mineCell(index: Int) {
        if (this.gameField[index] == 0) {
            this.gameField[index] = -2
        } else this.gameField[index] = 0
    }

    /**
     * Count revealed and so far empty cells
     */
    private fun checkState() {

        var emptyCellsLeft = 0
        var minesRevealed = 0

        this.gameField.forEach {
            if (it == 0) emptyCellsLeft++
            if (it == -2) minesRevealed++
        }

        if (minesRevealed == this.minesCount ||
                (minesRevealed == 0 && emptyCellsLeft == this.minesCount)
        )
            throw MinesweeperStatusException("Congratulations! You found all the mines!")
    }

    /**
     * Analyze a given cell and update it if needed
     */
    private fun updateCells(from: Int, count: Int) {
        for (index in from..from + count) {
            if (this.mineField.getOrNull(index) == null) continue
            if (this.mineField[index] >= 0) this.mineField[index] += 1
        }
    }

    /**
     * Recursively open possible cells
     */
    private fun updateCellsAround(from: Int, count: Int) {

        for (index in from..from + count) {

            if (this.gameField.getOrNull(index) == null || this.gameField[index] !in arrayOf(0, -2)) continue

            if (this.mineField[index] in arrayOf(0, -2)) {
                this.gameField[index] = -3
                openCell(index)
            } else {
                this.gameField[index] = this.mineField[index]
            }
        }
    }

    private fun isMined(index: Int) = this.mineField[index] == -1
    private fun isLeftVerticalSide(index: Int): Boolean = index % this.dimension == 0
    private fun isVerticalSide(index: Int): Boolean =
            index % this.dimension == 0 || index % this.dimension == this.dimension - 1
}

fun main() {
    val scanner = Scanner(System.`in`)

    print("How many mines do you want on the field? ")

    val minesCount = scanner.nextInt()
    val minesweeper = Minesweeper(
            dimension = 9,
            minesCount = minesCount
    )

    minesweeper.drawField()

    while (Minesweeper.State.inProcess) {

        print("Set/delete mine marks (x and y coordinates): ")

        val pointX = scanner.nextInt()
        val pointY = scanner.nextInt()
        val type = scanner.next()

        minesweeper.move(pointX, pointY, type)
    }

    println(Minesweeper.State.statusMessage)
}
