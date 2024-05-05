package com.example.xvso

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import android.view.ViewGroup
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.view.Gravity
import android.graphics.drawable.shapes.RectShape
import kotlin.random.Random

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var gridLayout: GridLayout
    private lateinit var resetButton: Button
    private lateinit var modeButton: Button
    private lateinit var startButton: Button

    private var currentPlayer = Player.X
    private var computerMoved = false
    private var gameBoard = Array(3) { IntArray(3) }
    private var gameMode = Mode.PAIR // Domyślny tryb gry to gra w parze
    private var gameStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gridLayout = findViewById(R.id.gridLayout)
        resetButton = findViewById(R.id.resetButton)
        modeButton = findViewById(R.id.modeButton)
        startButton = findViewById(R.id.startButton)

        resetButton.setOnClickListener {
            resetGame()
        }

        modeButton.setOnClickListener {
            toggleGameMode()
        }

        startButton.setOnClickListener {
            startGame()
        }

        initializeBoard()
    }

    private fun initializeBoard() {
        // Inicjalizacja gameBoard z uwzględnieniem rozmiarów GridLayout
        gameBoard = Array(gridLayout.rowCount) { IntArray(gridLayout.columnCount) }

        // Ustawienie szerokości i wysokości przycisku w pikselach
        val buttonWidthPx = resources.getDimensionPixelSize(R.dimen.button_width)
        val buttonHeightPx = resources.getDimensionPixelSize(R.dimen.button_height)

        for (i in 0 until gridLayout.rowCount) {
            for (j in 0 until gridLayout.columnCount) {
                val button = Button(this)
                // Ustawienie rozmiarów przycisku
                button.layoutParams = ViewGroup.LayoutParams(buttonWidthPx, buttonHeightPx)
                button.setBackgroundColor(Color.WHITE)

                // Stworzenie prostokątnej ramki
                val border = ShapeDrawable(RectShape())
                border.paint.color = Color.BLACK
                border.paint.style = Paint.Style.STROKE
                border.paint.strokeWidth = 2f
                button.background = border

                button.setOnClickListener(this)
                button.tag = Cell(i, j)
                gridLayout.addView(button)
            }
        }
    }

    private fun startGame() {
        gameStarted = true
        startButton.visibility = View.GONE
        resetButton.visibility = View.VISIBLE
        modeButton.visibility = View.VISIBLE
        modeButton.isEnabled = false
    }

    override fun onClick(v: View?) {
        if (v is Button && gameStarted) {
            val cell = v.tag as Cell
            if (gameBoard[cell.row][cell.column] == 0) {
                updateButton(v, currentPlayer.toString(), if (currentPlayer == Player.X) Color.RED else Color.GREEN)

                gameBoard[cell.row][cell.column] = currentPlayer.value

                if (checkForWin(currentPlayer.value)) {
                    Toast.makeText(this, "${currentPlayer.toString()} wygrywa!", Toast.LENGTH_SHORT).show()
                    disableBoard()
                } else if (isBoardFull()) {
                    Toast.makeText(this, "Remis!", Toast.LENGTH_SHORT).show()
                } else {
                    if (gameMode == Mode.SOLO && currentPlayer == Player.X) {
                        makeAIMove()
                    } else {
                        currentPlayer = if (currentPlayer == Player.X) Player.O else Player.X
                    }
                }
            }
        }
    }

    private fun makeAIMove() {
        // Pobierz listę wszystkich pustych komórek
        val emptyCells = mutableListOf<Cell>()
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (gameBoard[i][j] == 0) {
                    emptyCells.add(Cell(i, j))
                }
            }
        }
        // Jeśli są puste komórki, wybierz losową i ustaw symbol
        if (emptyCells.isNotEmpty()) {
            val randomCell = emptyCells.random()
            val button = gridLayout.findViewWithTag<Button>(randomCell)
            updateButton(button, "O", Color.GREEN)
            gameBoard[randomCell.row][randomCell.column] = Player.O.value

            if (checkForWin(Player.O.value)) {
                Toast.makeText(this, "Komputer wygrywa!", Toast.LENGTH_SHORT).show()
                disableBoard()
            }
        }
    }

    private fun updateButton(button: Button, text: String, backgroundColor: Int) {
        button.text = text
        button.setBackgroundColor(backgroundColor)
        button.setTextColor(Color.BLACK)
        button.gravity = Gravity.CENTER
        button.textAlignment = View.TEXT_ALIGNMENT_CENTER
    }

    private fun checkForWin(player: Int): Boolean {
        for (i in 0..2) {
            if (gameBoard[i][0] == player && gameBoard[i][1] == player && gameBoard[i][2] == player) {
                return true
            }
            if (gameBoard[0][i] == player && gameBoard[1][i] == player && gameBoard[2][i] == player) {
                return true
            }
        }
        if (gameBoard[0][0] == player && gameBoard[1][1] == player && gameBoard[2][2] == player) {
            return true
        }
        if (gameBoard[0][2] == player && gameBoard[1][1] == player && gameBoard[2][0] == player) {
            return true
        }
        return false
    }

    private fun isBoardFull(): Boolean {
        for (row in gameBoard) {
            for (cell in row) {
                if (cell == 0) {
                    return false
                }
            }
        }
        return true
    }

    private fun resetGame() {
        startButton.visibility = View.VISIBLE // Pokaż przycisk "Start"
        resetButton.visibility = View.VISIBLE // Pokaż przycisk "Reset"
        modeButton.visibility = View.VISIBLE // Pokaż przycisk trybu gry
        modeButton.isEnabled = true

        for (i in 0 until gridLayout.childCount) {
            val button = gridLayout.getChildAt(i) as Button
            button.text = ""
            button.setBackgroundColor(Color.WHITE)
            button.isEnabled = true // Włącz ponownie przycisk

            // Ustaw obramowanie dla przycisków "X" i "O"
            val border = ShapeDrawable(RectShape())
            border.paint.color = Color.BLACK
            border.paint.style = Paint.Style.STROKE
            border.paint.strokeWidth = 2f
            button.background = border
        }
        gameBoard = Array(3) { IntArray(3) }
        currentPlayer = Player.X
        gameStarted = false // Ustaw flagę gameStarted na false, aby rozpocząć nową grę od startu
    }

    private fun disableBoard() {
        for (i in 0 until gridLayout.childCount) {
            val button = gridLayout.getChildAt(i) as Button
            button.isEnabled = false
        }
    }

    private fun toggleGameMode() {
        gameMode = if (gameMode == Mode.PAIR) Mode.SOLO else Mode.PAIR
        modeButton.text = if (gameMode == Mode.PAIR) "Tryb: Gra w parze" else "Tryb: Solo"
        if (gameMode == Mode.SOLO && currentPlayer == Player.O) {
            // Jeśli gra w trybie solo i komputer zaczyna, wykonaj pierwszy ruch AI
            makeAIMove()
        }
    }

    private data class Cell(val row: Int, val column: Int)

    private enum class Player(val value: Int) {
        X(1),
        O(2)
    }

    private enum class Mode {
        PAIR,
        SOLO
    }
}
