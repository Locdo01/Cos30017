package com.example.carrental.data.repository



/** Keeps the current user's credit balance in memory for this app session. */
object WalletRepository {

    private const val STARTING_BALANCE = 500
    private var currentBalance = STARTING_BALANCE

    /** Returns the current balance. */
    fun getBalance(): Int = currentBalance

    /** Deducts credits only when the balance can cover the requested amount. */
    fun spend(amount: Int): Boolean {
        if (amount <= 0 || amount > currentBalance) return false
        currentBalance -= amount
        return true
    }

    /** Returns credits after a rental is cancelled or its cost is reduced. */
    fun refund(amount: Int) {
        if (amount > 0) currentBalance += amount
    }
}
