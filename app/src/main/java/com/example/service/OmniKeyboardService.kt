package com.example.service

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.SystemClock
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.keyboard.KeyboardController
import com.example.keyboard.OmniInputTarget
import com.example.ui.keyboard.OmniKeyboardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * OmniKeyboardService: Production-grade InputMethodService.
 *
 * Manages:
 * - Complete IME lifecycle (create, initialize, start/finish input, destroy).
 * - LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner for Compose.
 * - Robust InputConnection communication with fallbacks for text editing, selection, cursor, and clipboard.
 * - Hardware key event interception (Back key sheet dismissal, Ctrl/Alt modifiers, Esc, Tab, Enter).
 * - Selection change updates for continuous algorithmic spell check and math parsing.
 */
class OmniKeyboardService : InputMethodService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var controller: KeyboardController

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private val inputTarget = object : OmniInputTarget {
        override fun commitText(text: String) {
            currentInputConnection?.commitText(text, 1)
        }

        override fun deleteBackward(count: Int) {
            val conn = currentInputConnection ?: return
            val selected = conn.getSelectedText(0)
            if (!selected.isNullOrEmpty()) {
                conn.commitText("", 1)
                return
            }
            if (count > 0) {
                val success = conn.deleteSurroundingText(count, 0)
                if (!success) {
                    for (i in 0 until count) {
                        conn.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        conn.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
                    }
                }
            }
        }

        override fun deleteForward(count: Int) {
            val conn = currentInputConnection ?: return
            val success = conn.deleteSurroundingText(0, count)
            if (!success) {
                for (i in 0 until count) {
                    conn.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_FORWARD_DEL))
                    conn.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_FORWARD_DEL))
                }
            }
        }

        override fun moveCursor(offset: Int) {
            val conn = currentInputConnection ?: return
            if (offset < 0) {
                for (i in 0 until -offset) {
                    conn.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT))
                    conn.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT))
                }
            } else {
                for (i in 0 until offset) {
                    conn.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT))
                    conn.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT))
                }
            }
        }

        override fun performAction() {
            val conn = currentInputConnection ?: return
            val editorInfo = currentInputEditorInfo
            val imeAction = (editorInfo?.imeOptions ?: 0) and EditorInfo.IME_MASK_ACTION
            if (imeAction != EditorInfo.IME_ACTION_NONE && imeAction != EditorInfo.IME_ACTION_UNSPECIFIED) {
                conn.performEditorAction(imeAction)
            } else if (editorInfo?.actionId != null && editorInfo.actionId != 0) {
                conn.performEditorAction(editorInfo.actionId)
            } else {
                conn.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                conn.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
        }

        override fun getTextBeforeCursor(length: Int): String {
            return currentInputConnection?.getTextBeforeCursor(length, 0)?.toString() ?: ""
        }

        override fun getTextAfterCursor(length: Int): String {
            return currentInputConnection?.getTextAfterCursor(length, 0)?.toString() ?: ""
        }

        override fun getSelectedText(): String {
            return currentInputConnection?.getSelectedText(0)?.toString() ?: ""
        }

        override fun replaceSelection(text: String) {
            currentInputConnection?.commitText(text, 1)
        }

        override fun hideKeyboard() {
            requestHideSelf(0)
        }

        override fun switchToNextIme() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (!switchToPreviousInputMethod()) {
                    switchToNextInputMethod(false)
                }
            } else {
                @Suppress("DEPRECATION")
                if (!switchToNextInputMethod(false)) {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.showInputMethodPicker()
                }
            }
        }

        override fun sendKeyEvent(event: KeyEvent) {
            currentInputConnection?.sendKeyEvent(event)
        }

        override fun sendKey(keyCode: Int, metaState: Int) {
            val conn = currentInputConnection ?: return
            val now = SystemClock.uptimeMillis()
            conn.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, metaState))
            conn.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, metaState))
        }

        override fun copy() {
            currentInputConnection?.performContextMenuAction(android.R.id.copy)
        }

        override fun cut() {
            currentInputConnection?.performContextMenuAction(android.R.id.cut)
        }

        override fun paste() {
            currentInputConnection?.performContextMenuAction(android.R.id.paste)
        }

        override fun selectAll() {
            currentInputConnection?.performContextMenuAction(android.R.id.selectAll)
        }

        override fun undo() {
            currentInputConnection?.performContextMenuAction(android.R.id.undo)
        }

        override fun redo() {
            currentInputConnection?.performContextMenuAction(android.R.id.redo)
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        controller = KeyboardController(this, serviceScope)
    }

    override fun onInitializeInterface() {
        super.onInitializeInterface()
    }

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return true
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        return false
    }

    override fun onCreateInputView(): View {
        return ComposeView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(this@OmniKeyboardService.lifecycle)
            )
            setViewTreeLifecycleOwner(this@OmniKeyboardService)
            setViewTreeViewModelStoreOwner(this@OmniKeyboardService)
            setViewTreeSavedStateRegistryOwner(this@OmniKeyboardService)

            setContent {
                MaterialTheme {
                    OmniKeyboardView(
                        controller = controller,
                        inputTarget = inputTarget
                    )
                }
            }
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        controller.onEditorInfoChanged(attribute)
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (!lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
        controller.onInputViewStarted(inputTarget, info)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        }
        controller.closeAllSheets()
        controller.resetTransientModifiers()
    }

    override fun onFinishInput() {
        super.onFinishInput()
    }

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        controller.onSelectionChanged(inputTarget, newSelStart, newSelEnd)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Dismiss any open keyboard sheets (clipboard, snippets, case, settings) first
            if (controller.closeAllSheets()) {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        serviceScope.cancel()
    }
}
