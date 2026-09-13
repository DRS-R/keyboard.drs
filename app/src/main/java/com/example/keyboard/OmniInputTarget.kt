package com.example.keyboard

import android.view.KeyEvent

interface OmniInputTarget {
    fun commitText(text: String)
    fun deleteBackward(count: Int = 1)
    fun deleteForward(count: Int = 1) {}
    fun moveCursor(offset: Int)
    fun performAction()
    fun getTextBeforeCursor(length: Int = 50): String
    fun getTextAfterCursor(length: Int = 50): String
    fun getSelectedText(): String
    fun replaceSelection(text: String)
    fun hideKeyboard() {}
    fun switchToNextIme() {}
    fun sendKeyEvent(event: KeyEvent) {}
    fun sendKey(keyCode: Int, metaState: Int = 0) {}
    fun copy() {}
    fun cut() {}
    fun paste() {}
    fun selectAll() {}
    fun undo() {}
    fun redo() {}
}
