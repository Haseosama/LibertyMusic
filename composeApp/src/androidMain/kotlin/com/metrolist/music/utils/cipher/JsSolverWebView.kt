package com.metrolist.music.utils.cipher

import android.content.Context
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Structural, version-independent cipher solver for YouTube's player.js.
 *
 * Unlike [CipherWebView] (which needs a function NAME extracted ahead of time — via
 * [FunctionNameExtractor]'s regexes or [PlayerConfigStore]'s per-version config — then invokes
 * that named function from within a live copy of the player.js), this loads yt-dlp's `ejs`
 * solver (`meriyah` to parse player.js into an AST, `astring` to regenerate source from it,
 * `yt.solver.core.js` to structurally locate the sig/n functions by AST shape). Because it
 * recognizes the function by what it *does* rather than by name or a per-version pattern, it
 * keeps working across player.js updates that break name-based extraction — the exact case that
 * showed up as "Vidéo non disponible" once [PlayerConfigStore]'s remote config stopped covering
 * newly-seen player hashes.
 *
 * Used as a fallback in [CipherDeobfuscator] when the existing name-based extraction fails.
 */
class JsSolverWebView private constructor(
    context: Context,
    // to be used exactly once only during initialization!
    private val initContinuation: Continuation<JsSolverWebView>,
) {
    private val logger = Logger.withTag(TAG)
    private val webView = WebView(context)

    // The JS side reads its input through this bridge (rather than embedding it inline in the
    // evaluated script) so arbitrarily large/weird player.js text never has to be escaped into a
    // JS string literal.
    @Volatile
    private var pendingInputJson: String = "{}"

    private val initResumed = AtomicBoolean(false)

    init {
        val settings = webView.settings
        @Suppress("SetJavaScriptEnabled")
        settings.javaScriptEnabled = true
        settings.blockNetworkLoads = true // solver is fully self-contained, no network needed

        webView.addJavascriptInterface(this, JS_INTERFACE)

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(m: ConsoleMessage): Boolean {
                when (m.messageLevel()) {
                    ConsoleMessage.MessageLevel.ERROR -> logger.e("JS: ${m.message()}")
                    ConsoleMessage.MessageLevel.WARNING -> logger.w("JS: ${m.message()}")
                    else -> logger.v("JS: ${m.message()}")
                }
                return super.onConsoleMessage(m)
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                logger.d("Solver page finished loading")
                if (initResumed.compareAndSet(false, true))
                    initContinuation.resume(this@JsSolverWebView)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?,
            ) {
                if (initResumed.compareAndSet(false, true))
                    initContinuation.resumeWithException(
                        SolverInitException("WebView error loading solver: ${error?.description}")
                    )
            }
        }
    }

    @JavascriptInterface
    fun getInput(): String = pendingInputJson

    private fun loadSolver(meriyahJs: String, astringJs: String, solverCoreJs: String) {
        val html = buildString {
            append("<!DOCTYPE html><html><head><script>")
            append(meriyahJs)
            append("</script><script>")
            append(astringJs)
            append("</script><script>")
            append(solverCoreJs)
            append("</script></head><body></body></html>")
        }
        // Base URL only matters for relative resource resolution, which this page has none of.
        webView.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "utf-8", null)
    }

    /**
     * Runs [input] through the solver. Must be called on [Dispatchers.Main] internally
     * (WebView requirement) — callers don't need to switch threads themselves.
     */
    suspend fun solve(input: SolverInput): SolverOutput = withContext(Dispatchers.Main) {
        val requestJson = json.encodeToString(SolverInput.serializer(), input)
        val resultJson = suspendCancellableCoroutine { cont ->
            pendingInputJson = requestJson
            // Wrapped in try/catch so a thrown JS error (e.g. "found 2 sig function
            // possibilities") comes back as a normal value instead of WebView's ambiguous
            // "null" response to an uncaught exception.
            val script = """
                (function() {
                    try {
                        return jsc(JSON.parse($JS_INTERFACE.getInput()));
                    } catch (e) {
                        return { type: 'error', error: (e && e.stack) ? e.stack : String(e) };
                    }
                })()
            """.trimIndent()
            webView.evaluateJavascript(script) { result ->
                cont.resume(result ?: "null")
            }
        }
        json.decodeFromString(SolverOutput.serializer(), resultJson)
    }

    fun close() {
        webView.stopLoading()
        webView.loadUrl("about:blank")
        webView.destroy()
    }

    class SolverInitException(message: String) : Exception(message)

    companion object {
        private const val TAG = "Metrolist_JsSolver"
        private const val JS_INTERFACE = "SolverBridge"

        private val logger = Logger.withTag(TAG)
        private val json = Json { ignoreUnknownKeys = true }

        suspend fun create(context: Context): JsSolverWebView {
            logger.d("Creating JsSolverWebView...")
            val (meriyahJs, astringJs, solverCoreJs) = withContext(Dispatchers.IO) {
                fun readAsset(name: String) =
                    context.assets.open("solver/$name").bufferedReader().use { it.readText() }

                Triple(
                    readAsset("meriyah.js"),
                    readAsset("astring.js"),
                    readAsset("yt.solver.core.js"),
                )
            }

            return withContext(Dispatchers.Main) {
                suspendCancellableCoroutine { cont ->
                    val instance = JsSolverWebView(context, cont)
                    instance.loadSolver(meriyahJs, astringJs, solverCoreJs)
                }
            }
        }
    }
}

@Serializable
data class SolverRequest(
    val type: String, // "sig" or "n"
    val challenges: List<String>,
)

@Serializable
data class SolverInput(
    val type: String, // "player" or "preprocessed"
    val player: String? = null,
    @SerialName("preprocessed_player")
    val preprocessedPlayer: String? = null,
    val requests: List<SolverRequest>,
    @SerialName("output_preprocessed")
    val outputPreprocessed: Boolean = false,
)

@Serializable
data class SolverResponse(
    val type: String, // "result" or "error"
    val data: Map<String, String>? = null,
    val error: String? = null,
)

@Serializable
data class SolverOutput(
    val type: String,
    val responses: List<SolverResponse>? = null,
    val error: String? = null,
    @SerialName("preprocessed_player")
    val preprocessedPlayer: String? = null,
)
