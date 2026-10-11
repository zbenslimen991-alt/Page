package com.ziedquant.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import kotlin.math.abs

private val Bg = Color(0xFF070B12)
private val Panel = Color(0xFF101824)
private val Panel2 = Color(0xFF151F2D)
private val Neon = Color(0xFF16D99A)
private val Purple = Color(0xFF9A72FF)
private val Cyan = Color(0xFF4BCBEE)
private val Red = Color(0xFFFF6376)
private val Muted = Color(0xFF8C9BAD)
private val White = Color(0xFFF3F7FB)

private enum class AppTab { HOME, MARKETS, SIGNALS, PORTFOLIO, BACKTEST, AI, RISK, NEWS, SETTINGS }
private val tabIcons = mapOf(
    AppTab.HOME to Icons.Default.Dashboard,
    AppTab.MARKETS to Icons.Default.ShowChart,
    AppTab.SIGNALS to Icons.Default.Bolt,
    AppTab.PORTFOLIO to Icons.Default.AccountBalanceWallet,
    AppTab.BACKTEST to Icons.Default.QueryStats,
    AppTab.AI to Icons.Default.Psychology,
    AppTab.RISK to Icons.Default.Security,
    AppTab.NEWS to Icons.Default.Newspaper,
    AppTab.SETTINGS to Icons.Default.Settings
)

private val translations = mapOf(
    "fr" to mapOf("home" to "Accueil", "markets" to "Marchés", "signals" to "Signaux", "portfolio" to "Portefeuille", "backtest" to "Backtest", "ai" to "IA & Stratégies", "risklab" to "Laboratoire de risque", "news" to "Actualités", "settings" to "Paramètres", "overview" to "Vue d'ensemble", "refresh" to "Actualiser", "connect" to "Connexion aux données", "owner" to "Propriétaire GitHub", "repo" to "Dépôt du moteur", "branch" to "Branche", "token" to "Jeton GitHub (facultatif)", "save" to "Enregistrer et synchroniser", "loading" to "Chargement des données…", "error" to "Connexion indisponible", "live" to "Prix OKX en direct", "noData" to "Aucune donnée disponible", "totalSignals" to "Signaux historiques", "strategies" to "Stratégies suivies", "newsSources" to "Sources d'actualités", "models" to "Modèles IA", "latestSignals" to "Derniers signaux", "topMarkets" to "Marchés suivis", "performance" to "Performance des stratégies", "risk" to "Risque & décisions", "dataStatus" to "État des données", "ready" to "Prêt", "language" to "Langue de l'application", "githubHelp" to "Le jeton est facultatif pour un dépôt public. Pour un dépôt privé, utilisez un jeton à accès minimal.", "notAdvice" to "Analyse informative — pas un conseil financier.", "loaded" to "Données synchronisées", "noToken" to "Mode dépôt public"),
    "en" to mapOf("home" to "Home", "markets" to "Markets", "signals" to "Signals", "portfolio" to "Portfolio", "backtest" to "Backtest", "ai" to "AI & Strategies", "risklab" to "Risk Lab", "news" to "News", "settings" to "Settings", "overview" to "Overview", "refresh" to "Refresh", "connect" to "Data connection", "owner" to "GitHub owner", "repo" to "Engine repository", "branch" to "Branch", "token" to "GitHub token (optional)", "save" to "Save & sync", "loading" to "Loading data…", "error" to "Connection unavailable", "live" to "Live OKX prices", "noData" to "No data available", "totalSignals" to "Historical signals", "strategies" to "Tracked strategies", "newsSources" to "News sources", "models" to "AI models", "latestSignals" to "Latest signals", "topMarkets" to "Tracked markets", "performance" to "Strategy performance", "risk" to "Risk & decisions", "dataStatus" to "Data status", "ready" to "Ready", "language" to "App language", "githubHelp" to "Token is optional for public repositories. For a private repository, use a least-privilege token.", "notAdvice" to "Informational analysis — not financial advice.", "loaded" to "Data synchronized", "noToken" to "Public repository mode"),
    "ar" to mapOf("home" to "الرئيسية", "markets" to "الأسواق", "signals" to "الإشارات", "portfolio" to "المحفظة", "backtest" to "الاختبار التاريخي", "ai" to "الذكاء والاستراتيجيات", "risklab" to "مختبر المخاطر", "news" to "الأخبار", "settings" to "الإعدادات", "overview" to "نظرة عامة", "refresh" to "تحديث", "connect" to "الاتصال بالبيانات", "owner" to "مالك GitHub", "repo" to "مستودع المحرك", "branch" to "الفرع", "token" to "رمز GitHub (اختياري)", "save" to "حفظ ومزامنة", "loading" to "جارٍ تحميل البيانات…", "error" to "الاتصال غير متاح", "live" to "أسعار OKX مباشرة", "noData" to "لا توجد بيانات", "totalSignals" to "الإشارات التاريخية", "strategies" to "الاستراتيجيات المتابعة", "newsSources" to "مصادر الأخبار", "models" to "نماذج الذكاء الاصطناعي", "latestSignals" to "أحدث الإشارات", "topMarkets" to "الأسواق المتابعة", "performance" to "أداء الاستراتيجيات", "risk" to "المخاطر والقرارات", "dataStatus" to "حالة البيانات", "ready" to "جاهز", "language" to "لغة التطبيق", "githubHelp" to "الرمز اختياري للمستودع العام. للمستودع الخاص استخدم رمزًا بأقل الصلاحيات.", "notAdvice" to "تحليل للمعلومات فقط، وليس نصيحة مالية.", "loaded" to "تمت مزامنة البيانات", "noToken" to "وضع المستودع العام"),
    "es" to mapOf("home" to "Inicio", "markets" to "Mercados", "signals" to "Señales", "portfolio" to "Cartera", "backtest" to "Backtest", "ai" to "IA y estrategias", "risklab" to "Laboratorio de riesgo", "news" to "Noticias", "settings" to "Ajustes", "overview" to "Resumen", "refresh" to "Actualizar", "connect" to "Conexión de datos", "owner" to "Propietario GitHub", "repo" to "Repositorio", "branch" to "Rama", "token" to "Token GitHub (opcional)", "save" to "Guardar y sincronizar", "loading" to "Cargando datos…", "error" to "Conexión no disponible", "live" to "Precios OKX en vivo", "noData" to "Sin datos disponibles", "totalSignals" to "Señales históricas", "strategies" to "Estrategias seguidas", "newsSources" to "Fuentes de noticias", "models" to "Modelos IA", "latestSignals" to "Últimas señales", "topMarkets" to "Mercados seguidos", "performance" to "Rendimiento de estrategias", "risk" to "Riesgo y decisiones", "dataStatus" to "Estado de datos", "ready" to "Listo", "language" to "Idioma de la aplicación", "githubHelp" to "El token es opcional para repositorios públicos. Para privados, usa un token de privilegios mínimos.", "notAdvice" to "Análisis informativo; no es asesoramiento financiero.", "loaded" to "Datos sincronizados", "noToken" to "Modo repositorio público"),
    "tr" to mapOf("home" to "Ana Sayfa", "markets" to "Piyasalar", "signals" to "Sinyaller", "portfolio" to "Portföy", "backtest" to "Backtest", "ai" to "YZ ve Stratejiler", "risklab" to "Risk Laboratuvarı", "news" to "Haberler", "settings" to "Ayarlar", "overview" to "Genel Bakış", "refresh" to "Yenile", "connect" to "Veri bağlantısı", "owner" to "GitHub sahibi", "repo" to "Motor deposu", "branch" to "Dal", "token" to "GitHub token (isteğe bağlı)", "save" to "Kaydet ve eşitle", "loading" to "Veriler yükleniyor…", "error" to "Bağlantı kullanılamıyor", "live" to "Canlı OKX fiyatları", "noData" to "Veri yok", "totalSignals" to "Geçmiş sinyaller", "strategies" to "İzlenen stratejiler", "newsSources" to "Haber kaynakları", "models" to "YZ modelleri", "latestSignals" to "Son sinyaller", "topMarkets" to "İzlenen piyasalar", "performance" to "Strateji performansı", "risk" to "Risk ve kararlar", "dataStatus" to "Veri durumu", "ready" to "Hazır", "language" to "Uygulama dili", "githubHelp" to "Herkese açık depolar için token isteğe bağlıdır. Özel depolarda en az yetkili token kullanın.", "notAdvice" to "Bilgilendirme amaçlı analiz; finansal tavsiye değildir.", "loaded" to "Veriler eşitlendi", "noToken" to "Herkese açık depo modu"),
    "zh" to mapOf("home" to "首页", "markets" to "市场", "signals" to "信号", "portfolio" to "投资组合", "backtest" to "回测", "ai" to "AI与策略", "risklab" to "风险实验室", "news" to "新闻", "settings" to "设置", "overview" to "总览", "refresh" to "刷新", "connect" to "数据连接", "owner" to "GitHub所有者", "repo" to "引擎仓库", "branch" to "分支", "token" to "GitHub令牌（可选）", "save" to "保存并同步", "loading" to "正在加载数据…", "error" to "连接不可用", "live" to "OKX实时价格", "noData" to "暂无数据", "totalSignals" to "历史信号", "strategies" to "跟踪策略", "newsSources" to "新闻来源", "models" to "AI模型", "latestSignals" to "最新信号", "topMarkets" to "关注市场", "performance" to "策略表现", "risk" to "风险与决策", "dataStatus" to "数据状态", "ready" to "就绪", "language" to "应用语言", "githubHelp" to "公开仓库无需令牌。私有仓库请使用最小权限令牌。", "notAdvice" to "仅供参考，不构成财务建议。", "loaded" to "数据已同步", "noToken" to "公开仓库模式")
)

private fun t(lang: String, key: String): String =
    translations[lang]?.get(key) ?: translations["fr"]?.get(key) ?: key

data class Market(val symbol: String, val price: Double, val change: Double, val volume: Double)
data class Signal(val symbol: String, val side: String, val score: Double, val probability: Double, val price: Double, val target: Double, val stop: Double, val regime: String, val strategy: String = "—", val expectedValue: Double = 0.0, val decay: Double = 0.0, val sizing: Double = 0.0, val noTrade: Boolean = false)
data class PortfolioItem(val symbol: String, val side: String, val entry: Double, val mark: Double, val units: Double, val allocated: Double, val pnl: Double, val status: String)
data class Snapshot(
    val markets: List<Market> = emptyList(),
    val signals: List<Signal> = emptyList(),
    val strategyCount: Int = 0,
    val strategyNames: List<String> = emptyList(),
    val strategyStats: List<Triple<String, Double, Double>> = emptyList(),
    val newsCount: Int = 0,
    val newsTotalSources: Int = 0,
    val newsHighImpact: Int = 0,
    val newsRisk: String = "—",
    val newsStatus: String = "—",
    val newsHeadlines: List<String> = emptyList(),
    val modelCount: Int = 0,
    val discoveryCandidates: Int = 0,
    val discoveryValidated: Int = 0,
    val entryMethodCount: Int = 0,
    val autoTuneUpdates: Int = 0,
    val strategyUniverseCount: Int = 0,
    val portfolioOpenCount: Int = 0,
    val portfolioPendingCount: Int = 0,
    val portfolioClosedCount: Int = 0,
    val openPositions: List<PortfolioItem> = emptyList(),
    val pendingOrders: List<PortfolioItem> = emptyList(),
    val closedTrades: List<PortfolioItem> = emptyList(),
    val backtestCount: Int = 0,
    val backtestSummary: List<Pair<String, String>> = emptyList(),
    val riskSummary: List<Pair<String, String>> = emptyList(),
    val portfolioSummary: List<Pair<String, String>> = emptyList(),
    val updatedAt: String = "—",
    val errors: List<String> = emptyList()
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createSignalNotificationChannel()
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 3001)
        }
        setContent { ZiedQuantApp(this) }
    }

    private fun createSignalNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SIGNAL_CHANNEL_ID,
                "Zied quant traide alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for newly detected trading signals"
                enableVibration(true)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun notifyNewSignal(signal: Signal) {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val manager = getSystemService(NotificationManager::class.java)
        val id = (signal.symbol + signal.side + signal.target.toString()).hashCode() and 0x7fffffff
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.Notification.Builder(this, SIGNAL_CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            android.app.Notification.Builder(this)
        }.setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle("Zied quant traide • ${signal.side} ${signal.symbol}")
            .setContentText("Score ${signal.score} • Prix ${signal.price} • TP ${signal.target} • SL ${signal.stop}")
            .setStyle(android.app.Notification.BigTextStyle().bigText(
                "Nouvelle signal détecté: ${signal.symbol} / ${signal.side}\n" +
                    "Score: ${signal.score} • Probabilité: ${signal.probability}%\n" +
                    "Stratégie: ${signal.strategy} • EV: ${signal.expectedValue}\n" +
                    "Prix: ${signal.price} • TP: ${signal.target} • SL: ${signal.stop}"
            ))
            .setAutoCancel(true)
            .build()
        manager.notify(id, notification)
    }

    companion object {
        const val SIGNAL_CHANNEL_ID = "zied_quant_signals"
    }
}

private fun securePrefs(context: Context) = EncryptedSharedPreferences.create(
    context,
    "zied_quant_secure",
    MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)

private class DataClient(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(18, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .callTimeout(45, TimeUnit.SECONDS)
        .build()

    private fun get(url: String, token: String = ""): String {
        val builder = Request.Builder().url(url).header("User-Agent", "ZiedQuant-Android/1.0")
        if (token.isNotBlank()) builder.header("Authorization", "Bearer $token")
        val response = client.newCall(builder.build()).execute()
        response.use {
            if (!it.isSuccessful) throw IllegalStateException("HTTP ${it.code}: ${url.substringBefore('?')}")
            return it.body?.string().orEmpty()
        }
    }

    private fun repoFile(owner: String, repo: String, branch: String, path: String, token: String): String {
        val encodedPath = path.split("/").joinToString("/") { URLEncoder.encode(it, "UTF-8").replace("+", "%20") }
        val metaUrl = "https://api.github.com/repos/$owner/$repo/contents/$encodedPath?ref=${URLEncoder.encode(branch, "UTF-8")}"
        val meta = JSONObject(get(metaUrl, token))
        val downloadUrl = meta.optString("download_url")
        if (downloadUrl.isNotBlank() && downloadUrl != "null") return get(downloadUrl, token)
        val encoded = meta.optString("content").replace("\n", "")
        if (encoded.isBlank()) throw IllegalStateException("Empty GitHub file: $path")
        return String(Base64.decode(encoded, Base64.DEFAULT), StandardCharsets.UTF_8)
    }

    fun load(): Snapshot {
        val p = securePrefs(context)
        val owner = p.getString("owner", "zbenslimen991-alt")!!.trim()
        val repo = p.getString("repo", "Zied")!!.trim()
        val branch = p.getString("branch", "main")!!.trim()
        val token = p.getString("token", "")!!.trim()
        val errors = mutableListOf<String>()
        fun text(path: String): String? = try { repoFile(owner, repo, branch, path, token) } catch (e: Exception) {
            errors += "$path: ${e.message ?: "error"}"; null
        }
        fun textAny(vararg paths: String): String? {
            for (path in paths) {
                try { return repoFile(owner, repo, branch, path, token) } catch (_: Exception) { /* try the legacy filename */ }
            }
            errors += "${paths.joinToString(" / ")}: unavailable"
            return null
        }
        fun json(path: String): JSONObject? = text(path)?.let { runCatching { JSONObject(it) }.getOrNull() }
        fun jsonAny(vararg paths: String): JSONObject? = textAny(*paths)?.let { runCatching { JSONObject(it) }.getOrNull() }
        fun arrayAny(vararg paths: String): JSONArray? = textAny(*paths)?.let { runCatching { JSONArray(it) }.getOrNull() }

        val stats = jsonAny("strategy_stats_0001.json", "strategy_stats.json") ?: JSONObject()
        val signalArray = arrayAny("signal_history_0001.json", "signal_history.json") ?: JSONArray()
        val news = json("news_intelligence.json") ?: JSONObject()
        val ml = jsonAny("ml_model_memory_0001.json", "ml_model_memory.json") ?: JSONObject()
        val discovery = json("candle_strategy_discovery.json") ?: JSONObject()
        val entryMethods = json("entry_method_memory.json") ?: JSONObject()
        val autoTune = json("auto_tuned_config.json") ?: JSONObject()
        val analysisMemory = json("analysis_memory_0001.json") ?: JSONObject()
        val topGainerLearning = json("top_gainer_learning.json") ?: JSONObject()
        val strategySource = text("okx_quant/strategy_registry.py").orEmpty()
        val strategyUniverse = Regex("""@strategy\(\s*["']([^"']+)["']""").findAll(strategySource).map { it.groupValues[1] }.distinct().toList()
        val backtestText = textAny("backtest_results_0001.json", "backtest_results.json")
        val backtestJson = backtestText?.let { runCatching { JSONObject(it) }.getOrNull() }
        val portfolioText = textAny("portfolio_0001.json", "portfolio.json")
        val portfolioJson = portfolioText?.let { runCatching { JSONObject(it) }.getOrNull() }
        val prices = runCatching {
            val raw = JSONObject(get("https://www.okx.com/api/v5/market/tickers?instType=SPOT"))
            val arr = raw.optJSONArray("data") ?: JSONArray()
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val id = o.optString("instId")
                if (!id.endsWith("-USDT")) return@mapNotNull null
                val last = o.optDouble("last", 0.0)
                val open = o.optDouble("open24h", 0.0)
                if (last <= 0) return@mapNotNull null
                Market(id, last, if (open > 0) (last / open - 1) * 100 else 0.0, o.optDouble("volCcy24h", 0.0))
            }.sortedByDescending { it.volume }.take(35)
        }.getOrElse { errors += "OKX: ${it.message}"; emptyList() }

        val signals = (0 until signalArray.length()).mapNotNull { i ->
            val o = signalArray.optJSONObject(i) ?: return@mapNotNull null
            Signal(
                symbol = o.optString("instId", "—"),
                side = o.optString("signal", "—"),
                score = o.optDouble("score", 0.0),
                probability = o.optDouble("probability", o.optDouble("technicalProbability", 0.0)),
                price = o.optDouble("price", 0.0),
                target = o.optDouble("target", 0.0),
                stop = o.optDouble("stop", 0.0),
                regime = o.optString("dynamicMarketRegime", "—"),
                strategy = o.optString("strategy", o.optString("metaStrategy", "—")),
                expectedValue = o.optDouble("expectedValue", o.optDouble("ev", 0.0)),
                decay = o.optDouble("decay", o.optDouble("strategyDecay", 0.0)),
                sizing = o.optDouble("positionSizeMultiplier", o.optDouble("sizing", 0.0)),
                noTrade = o.optBoolean("noTrade", o.optString("decision", "").contains("NO-TRADE", true))
            )
        }.sortedByDescending { it.score }.take(30)

        val names = mutableListOf<String>()
        val statRows = mutableListOf<Triple<String, Double, Double>>()
        val keys = stats.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val o = stats.optJSONObject(key) ?: continue
            names += key
            statRows += Triple(key, o.optDouble("weightedWinRate", o.optDouble("winRate", 0.0)), o.optDouble("profitFactor", 0.0))
        }
        val modelCount = ml.optJSONObject("models")?.length() ?: 0
        val feeds = news.optJSONArray("feedStatus")
        val headlines = mutableListOf<String>()
        val articles = news.optJSONArray("articles")
        if (articles != null) {
            for (i in 0 until minOf(articles.length(), 8)) {
                val article = articles.optJSONObject(i) ?: continue
                val title = article.optString("title")
                if (title.isNotBlank() && title != "null") headlines += title
            }
        }
        fun collectTitles(value: Any?, depth: Int = 0) {
            if (depth > 4 || headlines.size >= 8 || value == null) return
            when (value) {
                is JSONObject -> {
                    val iterator = value.keys()
                    while (iterator.hasNext()) {
                        val k = iterator.next()
                        val v = value.opt(k)
                        if (k.lowercase() in listOf("title", "headline", "name") && v is String && v.length > 8 && headlines.size < 8) headlines += v
                        else collectTitles(v, depth + 1)
                    }
                }
                is JSONArray -> for (i in 0 until minOf(value.length(), 100)) collectTitles(value.opt(i), depth + 1)
            }
        }
        if (headlines.isEmpty()) collectTitles(news)
        val portfolioPairs = mutableListOf<Pair<String, String>>()
        val openPositions = portfolioJson?.optJSONArray("openPositions")
        val pendingOrders = portfolioJson?.optJSONArray("pendingLimitOrders")
        val closedPositions = portfolioJson?.optJSONArray("closedPositions")
        fun portfolioItems(arr: JSONArray?, status: String): List<PortfolioItem> {
            if (arr == null) return emptyList()
            return (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val symbol = o.optString("instId", o.optString("symbol", "—"))
                val side = o.optString("side", o.optString("positionSide", "LONG"))
                val entry = o.optDouble("entryPrice", o.optDouble("avgEntryPrice", 0.0))
                val units = o.optDouble("units", o.optDouble("size", 0.0))
                val allocated = o.optDouble("allocatedUsd", o.optDouble("reservedUsd", 0.0))
                val mark = prices.firstOrNull { it.symbol.equals(symbol, true) }?.price
                    ?: o.optDouble("currentPrice", entry)
                val direction = if (side.equals("short", true) || side.equals("sell", true)) -1.0 else 1.0
                val pnl = if (status == "CLOSED") o.optDouble("realizedPnl", o.optDouble("pnl", o.optDouble("profit", o.optDouble("profitUsd", 0.0))))
                    else if (entry > 0 && units > 0 && mark > 0) (mark - entry) * units * direction
                    else o.optDouble("unrealizedPnl", o.optDouble("pnl", 0.0))
                PortfolioItem(symbol, side, entry, mark, units, allocated, pnl, status)
            }
        }
        val openItems = portfolioItems(openPositions, "OPEN")
        val pendingItems = portfolioItems(pendingOrders, "PENDING")
        val closedItems = portfolioItems(closedPositions, "CLOSED").takeLast(30).reversed()
        portfolioPairs += "Open positions" to (openPositions?.length() ?: 0).toString()
        portfolioPairs += "Pending orders" to (pendingOrders?.length() ?: 0).toString()
        portfolioPairs += "Closed positions" to (closedPositions?.length() ?: 0).toString()
        portfolioPairs += "Initial capital" to String.format("%.2f", portfolioJson?.optDouble("initialCapital", 0.0) ?: 0.0)
        var realizedPnl = 0.0
        if (closedPositions != null) for (i in 0 until closedPositions.length()) {
            val trade = closedPositions.optJSONObject(i) ?: continue
            realizedPnl += trade.optDouble("pnlUsd", trade.optDouble("realizedPnl", trade.optDouble("pnl", 0.0)))
        }
        portfolioPairs += "Realized P&L (available fields)" to String.format("%.2f", realizedPnl)
        fun addPrimitiveFields(
            obj: JSONObject?,
            target: MutableList<Pair<String, String>>,
            depth: Int = 0,
            prefix: String = ""
        ) {
            if (obj == null || depth > 3 || target.size >= 12) return
            val it = obj.keys()
            while (it.hasNext() && target.size < 12) {
                val k = it.next()
                val v = obj.opt(k)
                val fullKey = if (prefix.isBlank()) k else "$prefix.$k"
                if (v is Number || v is String || v is Boolean) {
                    if (fullKey.length <= 60 && v.toString().length <= 64) target += fullKey to v.toString()
                } else if (v is JSONObject) addPrimitiveFields(v, target, depth + 1, fullKey)
            }
        }
        addPrimitiveFields(portfolioJson, portfolioPairs)
        val backtestPairs = mutableListOf<Pair<String, String>>()
        addPrimitiveFields(backtestJson, backtestPairs)
        val riskPairs = mutableListOf<Pair<String, String>>()
        addPrimitiveFields(analysisMemory, riskPairs, prefix = "analysis")
        addPrimitiveFields(topGainerLearning, riskPairs, prefix = "learning")
        addPrimitiveFields(backtestJson, riskPairs, prefix = "backtest")
        val history = autoTune.optJSONArray("history")
        val candidates = discovery.optJSONArray("candidates")
        var validated = 0
        if (candidates != null) for (i in 0 until candidates.length()) {
            if (candidates.optJSONObject(i)?.optBoolean("validated", false) == true) validated++
        }
        val newsCount = news.optInt("sourceCountOk", news.optInt("sourceCount", feeds?.length() ?: 0))
        val newsTotal = news.optInt("sourceCount", feeds?.length() ?: newsCount)
        val health = news.optJSONObject("health")
        return Snapshot(
            markets = prices, signals = signals, strategyCount = maxOf(names.size, strategyUniverse.size),
            strategyNames = (strategyUniverse + names).distinct().sorted(),
            strategyStats = statRows.sortedByDescending { it.second }.take(12),
            newsCount = newsCount, newsTotalSources = newsTotal,
            newsHighImpact = news.optInt("highImpactCount", 0),
            newsRisk = news.optString("riskLevel", "—"),
            newsStatus = health?.optString("status", news.optString("status", "—")) ?: news.optString("status", "—"),
            newsHeadlines = headlines.distinct().take(8), modelCount = modelCount,
            discoveryCandidates = candidates?.length() ?: 0,
            discoveryValidated = validated,
            entryMethodCount = entryMethods.optJSONObject("stats")?.length() ?: 0,
            autoTuneUpdates = history?.length() ?: 0,
            strategyUniverseCount = strategyUniverse.size,
            portfolioOpenCount = openPositions?.length() ?: 0,
            portfolioPendingCount = pendingOrders?.length() ?: 0,
            portfolioClosedCount = closedPositions?.length() ?: 0,
            openPositions = openItems,
            pendingOrders = pendingItems,
            closedTrades = closedItems,
            backtestCount = backtestJson?.length() ?: 0,
            backtestSummary = backtestPairs,
            riskSummary = riskPairs.distinctBy { it.first }.take(24),
            portfolioSummary = portfolioPairs.distinctBy { it.first }.take(12),
            updatedAt = news.optString("updatedAt", "—"),
            errors = errors.distinct().take(8)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZiedQuantApp(context: Context) {
    val prefs = remember { securePrefs(context) }
    var lang by remember { mutableStateOf(prefs.getString("language", "fr") ?: "fr") }
    var tab by remember { mutableStateOf(AppTab.HOME) }
    var moreExpanded by remember { mutableStateOf(false) }
    var snapshot by remember { mutableStateOf(Snapshot()) }
    var loading by remember { mutableStateOf(false) }
    var lastError by remember { mutableStateOf("") }
    var owner by remember { mutableStateOf(prefs.getString("owner", "zbenslimen991-alt") ?: "zbenslimen991-alt") }
    var repo by remember { mutableStateOf(prefs.getString("repo", "Zied") ?: "Zied") }
    var branch by remember { mutableStateOf(prefs.getString("branch", "main") ?: "main") }
    var token by remember { mutableStateOf(prefs.getString("token", "") ?: "") }
    var hasCompletedInitialLoad by remember { mutableStateOf(false) }

    fun refresh() {
        loading = true
        lastError = ""
        Thread {
            val result = runCatching { DataClient(context).load() }
            (context as? ComponentActivity)?.runOnUiThread {
                result.onSuccess { fresh ->
                    if (hasCompletedInitialLoad) {
                        val previousKeys = snapshot.signals.map { s ->
                            "${s.symbol}|${s.side}|${s.target}|${s.stop}"
                        }.toSet()
                        fresh.signals
                            .filter { s -> "${s.symbol}|${s.side}|${s.target}|${s.stop}" !in previousKeys }
                            .take(5)
                            .forEach { signal -> (context as? MainActivity)?.notifyNewSignal(signal) }
                    }
                    snapshot = fresh
                    hasCompletedInitialLoad = true
                    lastError = fresh.errors.takeIf { e -> e.size >= 6 }?.joinToString("\n") ?: ""
                }.onFailure { lastError = it.message ?: "Unknown error" }
                loading = false
            }
        }.start()
    }

    LaunchedEffect(Unit) {
        refresh()
        while (true) {
            delay(60_000L)
            if (!loading) refresh()
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Neon, secondary = Purple, background = Bg, surface = Panel,
        onPrimary = Bg, onBackground = White, onSurface = White, error = Red
    )) {
        Scaffold(
            containerColor = Bg,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            Box(Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(Neon), contentAlignment = Alignment.Center) {
                                Text("Z", color = Bg, fontSize = 23.sp, fontWeight = FontWeight.Black)
                            }
                            Column {
                                Text("ZIED QUANT TRAIDE", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                                Text("LIVE QUANT • AI • RISK", fontSize = 9.sp, color = Muted, letterSpacing = 1.3.sp)
                            }
                        }
                    },
                    actions = {
                        if (loading) CircularProgressIndicator(Modifier.size(20.dp), color = Neon, strokeWidth = 2.dp)
                        else IconButton(onClick = { refresh() }) { Icon(Icons.Default.Refresh, t(lang, "refresh"), tint = Neon) }
                        Spacer(Modifier.width(4.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Bg, titleContentColor = White)
                )
            },
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF0C121C), contentColor = Muted) {
                    val primaryTabs = listOf(AppTab.HOME, AppTab.MARKETS, AppTab.SIGNALS, AppTab.PORTFOLIO)
                    primaryTabs.forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Icon(tabIcons[item] ?: Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(21.dp)) },
                            label = { Text(t(lang, item.name.lowercase()), fontSize = 10.sp, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = Neon, selectedTextColor = Neon, indicatorColor = Color(0xFF18372F), unselectedIconColor = Muted, unselectedTextColor = Muted)
                        )
                    }
                    NavigationBarItem(
                        selected = tab in listOf(AppTab.BACKTEST, AppTab.AI, AppTab.RISK, AppTab.NEWS, AppTab.SETTINGS),
                        onClick = { moreExpanded = true },
                        icon = { Icon(Icons.Default.GridView, contentDescription = null, modifier = Modifier.size(21.dp)) },
                        label = { Text(if (lang == "ar") "المزيد" else if (lang == "fr") "Plus" else if (lang == "zh") "更多" else if (lang == "tr") "Daha" else if (lang == "es") "Más" else "More", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Neon, selectedTextColor = Neon, indicatorColor = Color(0xFF18372F), unselectedIconColor = Muted, unselectedTextColor = Muted)
                    )
                    DropdownMenu(expanded = moreExpanded, onDismissRequest = { moreExpanded = false }) {
                        listOf(AppTab.BACKTEST, AppTab.AI, AppTab.RISK, AppTab.NEWS, AppTab.SETTINGS).forEach { item ->
                            DropdownMenuItem(
                                text = { Text(t(lang, item.name.lowercase())) },
                                leadingIcon = { Icon(tabIcons[item] ?: Icons.Default.Dashboard, null) },
                                onClick = { tab = item; moreExpanded = false }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (tab) {
                    AppTab.HOME -> HomeScreen(lang, snapshot, loading, lastError, ::refresh, { tab = AppTab.SETTINGS })
                    AppTab.MARKETS -> MarketsScreen(lang, snapshot)
                    AppTab.SIGNALS -> SignalsScreen(lang, snapshot)
                    AppTab.PORTFOLIO -> PortfolioScreen(lang, snapshot)
                    AppTab.BACKTEST -> DataScreen(lang, t(lang, "backtest"), Icons.Default.QueryStats, snapshot.backtestSummary, snapshot.errors, "backtest_results_0001.json")
                    AppTab.AI -> AiScreen(lang, snapshot)
                    AppTab.RISK -> RiskLabScreen(lang, snapshot)
                    AppTab.NEWS -> NewsScreen(lang, snapshot)
                    AppTab.SETTINGS -> SettingsScreen(
                        lang, { lang = it; prefs.edit().putString("language", it).apply() },
                        owner, { owner = it }, repo, { repo = it }, branch, { branch = it }, token, { token = it },
                        {
                            prefs.edit().putString("owner", owner.trim()).putString("repo", repo.trim())
                                .putString("branch", branch.trim()).putString("token", token.trim()).apply()
                            refresh()
                        }, loading, lastError
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = { item { Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = content) } }
    )
}

@Composable
private fun SectionTitle(title: String, subtitle: String? = null, action: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = White)
            if (subtitle != null) Text(subtitle, fontSize = 11.sp, color = Muted)
        }
        action?.invoke()
    }
}

@Composable
private fun MetricCard(label: String, value: String, accent: Color = Neon, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = Panel)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(label, fontSize = 11.sp, color = Muted, maxLines = 2)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = accent, maxLines = 1)
        }
    }
}

@Composable
private fun PanelCard(content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(11.dp), content = content)
    }
}

@Composable
private fun HomeScreen(lang: String, data: Snapshot, loading: Boolean, error: String, onRefresh: () -> Unit, onSettings: () -> Unit) {
    ScreenColumn {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(t(lang, "overview"), color = White, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                Text(t(lang, "live"), color = Muted, fontSize = 12.sp)
            }
            Surface(color = Color(0xFF12372D), shape = CircleShape) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(Neon))
                    Text(if (loading) t(lang, "loading") else t(lang, "ready"), color = Neon, fontSize = 10.sp)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard(t(lang, "totalSignals"), data.signals.size.toString(), Purple, Modifier.weight(1f))
            MetricCard(t(lang, "strategies"), data.strategyCount.toString(), Neon, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard(t(lang, "newsSources"), data.newsCount.toString(), Cyan, Modifier.weight(1f))
            MetricCard(t(lang, "models"), data.modelCount.toString(), Purple, Modifier.weight(1f))
        }
        PanelCard {
            SectionTitle(t(lang, "topMarkets"), "OKX · USDT")
            if (data.markets.isEmpty()) EmptyText(t(lang, "noData"))
            else data.markets.take(5).forEach { MarketRow(it) }
        }
        PanelCard {
            SectionTitle(t(lang, "latestSignals"))
            if (data.signals.isEmpty()) EmptyText(t(lang, "noData"))
            else data.signals.take(4).forEach { SignalRow(it) }
        }
        PanelCard {
            SectionTitle(t(lang, "performance"))
            if (data.strategyStats.isEmpty()) EmptyText(t(lang, "noData"))
            else data.strategyStats.take(5).forEach { (name, winRate, pf) ->
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(name, color = White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("${"%.1f".format(winRate)}%  ·  PF ${"%.2f".format(pf)}", color = if (pf >= 1) Neon else Red, fontSize = 11.sp)
                    }
                    Canvas(Modifier.fillMaxWidth().height(4.dp)) {
                        drawRoundRect(Color(0xFF263141), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f))
                        val fraction = (winRate / 100.0).toFloat().coerceIn(0f, 1f)
                        drawRoundRect(if (winRate >= 50) Neon else Purple, size = androidx.compose.ui.geometry.Size(size.width * fraction, size.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f))
                    }
                }
            }
        }
        if (error.isNotBlank()) PanelCard { Text("${t(lang, "error")}: $error", color = Red, fontSize = 11.sp) }
        Button(onClick = onRefresh, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Neon, contentColor = Bg), shape = RoundedCornerShape(13.dp)) {
            Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(8.dp)); Text(t(lang, "refresh"), fontWeight = FontWeight.Bold)
        }
        OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(13.dp)) { Text(t(lang, "connect")) }
        Text(t(lang, "notAdvice"), color = Muted, fontSize = 10.sp)
    }
}

@Composable
private fun MarketRow(m: Market) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(35.dp).clip(CircleShape).background(Panel2), contentAlignment = Alignment.Center) {
            Text(m.symbol.takeWhile { it != '-' }.take(1), color = Neon, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(m.symbol, color = White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text("Vol. ${compact(m.volume)}", color = Muted, fontSize = 10.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(price(m.price), color = White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(String.format("%+.2f%%", m.change), color = if (m.change >= 0) Neon else Red, fontSize = 11.sp)
        }
    }
    HorizontalDivider(color = Color(0xFF202B3A))
}

@Composable
private fun MarketsScreen(lang: String, data: Snapshot) {
    var query by remember { mutableStateOf("") }
    var sortByChange by remember { mutableStateOf(false) }
    val filtered = data.markets.filter { it.symbol.contains(query.trim(), ignoreCase = true) }
        .let { rows -> if (sortByChange) rows.sortedByDescending { it.change } else rows }
    ScreenColumn {
        SectionTitle(t(lang, "markets"), t(lang, "live"))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(if (lang == "ar") "ابحث عن عملة مثل BTC" else if (lang == "fr") "Rechercher une paire, ex. BTC" else "Search a pair, e.g. BTC") },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Muted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Neon, unfocusedBorderColor = Color(0xFF263141),
                focusedTextColor = White, unfocusedTextColor = White, cursorColor = Neon
            )
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !sortByChange, onClick = { sortByChange = false }, label = { Text(if (lang == "ar") "حسب الحجم" else if (lang == "fr") "Volume" else "Volume") })
            FilterChip(selected = sortByChange, onClick = { sortByChange = true }, label = { Text(if (lang == "ar") "أعلى تغير" else if (lang == "fr") "Variation" else "Top change") })
            Spacer(Modifier.weight(1f))
            Text("${filtered.size}", color = Muted, modifier = Modifier.align(Alignment.CenterVertically), fontSize = 12.sp)
        }
        PanelCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (lang == "ar") "الأصل" else if (lang == "fr") "Actif" else "Asset", color = Muted, fontSize = 10.sp)
                Text(if (lang == "ar") "السعر" else if (lang == "fr") "Prix" else "Price", color = Muted, fontSize = 10.sp)
            }
            if (filtered.isEmpty()) EmptyText(t(lang, "noData")) else filtered.forEach { MarketRow(it) }
        }
    }
}

@Composable
private fun SignalsScreen(lang: String, data: Snapshot) {
    ScreenColumn {
        SectionTitle(t(lang, "signals"), "Signal · Score · Probabilité · Target / Stop")
        if (data.signals.isEmpty()) PanelCard { EmptyText(t(lang, "noData")) }
        else data.signals.forEach { SignalRow(it) }
    }
}

@Composable
private fun SignalRow(s: Signal) {
    PanelCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(s.symbol, color = White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("Score ${"%.1f".format(s.score)} · ${s.regime}", color = Muted, fontSize = 10.sp)
                Text("Meta: ${s.strategy}", color = Cyan, fontSize = 10.sp)
            }
            Surface(color = if (s.side.contains("SHORT", true) || s.side.contains("SELL", true)) Color(0xFF421D2A) else Color(0xFF10372D), shape = RoundedCornerShape(8.dp)) {
                Text(s.side, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = if (s.side.contains("SHORT", true) || s.side.contains("SELL", true)) Red else Neon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniValue("Probability", "${"%.1f".format(s.probability)}%", Purple, Modifier.weight(1f))
            MiniValue("Price", price(s.price), White, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniValue("Target", price(s.target), Neon, Modifier.weight(1f))
            MiniValue("Stop", price(s.stop), Red, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniValue("Expected Value", if (s.expectedValue == 0.0) "—" else String.format("%.2f%%", s.expectedValue), Cyan, Modifier.weight(1f))
            MiniValue("Decay", if (s.decay == 0.0) "—" else String.format("%.1f%%", s.decay * 100), Purple, Modifier.weight(1f))
            MiniValue("Sizing", if (s.sizing == 0.0) "—" else String.format("%.2f×", s.sizing), Neon, Modifier.weight(1f))
        }
        if (s.noTrade) Text("NO-TRADE · signal blocked by risk logic", color = Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MiniValue(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(10.dp)).background(Panel2).padding(10.dp)) {
        Text(label, color = Muted, fontSize = 10.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PortfolioScreen(lang: String, data: Snapshot) {
    ScreenColumn {
        SectionTitle(t(lang, "portfolio"), "OKX mark prices · open / pending / closed")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("Open", data.portfolioOpenCount.toString(), Neon, Modifier.weight(1f))
            MetricCard("Pending", data.portfolioPendingCount.toString(), Purple, Modifier.weight(1f))
            MetricCard("Closed", data.portfolioClosedCount.toString(), Cyan, Modifier.weight(1f))
        }
        PanelCard {
            SectionTitle("Open positions")
            if (data.openPositions.isEmpty()) EmptyText(t(lang, "noData"))
            data.openPositions.forEach { PortfolioItemRow(it) }
        }
        PanelCard {
            SectionTitle("Pending orders")
            if (data.pendingOrders.isEmpty()) EmptyText(t(lang, "noData"))
            data.pendingOrders.forEach { PortfolioItemRow(it) }
        }
        PanelCard {
            SectionTitle("Recent closed trades")
            if (data.closedTrades.isEmpty()) EmptyText(t(lang, "noData"))
            data.closedTrades.forEach { PortfolioItemRow(it) }
        }
        PanelCard {
            SectionTitle("Portfolio summary", "portfolio_0001.json")
            data.portfolioSummary.forEach { (k, v) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(k, color = Muted, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Text(v.take(64), color = White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                }
                HorizontalDivider(color = Color(0xFF202B3A))
            }
        }
    }
}

@Composable
private fun PortfolioItemRow(item: PortfolioItem) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(item.symbol, color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("${item.side} · ${item.status}", color = Muted, fontSize = 10.sp)
            }
            Text((if (item.pnl >= 0) "+" else "") + String.format("%.2f", item.pnl), color = if (item.pnl >= 0) Neon else Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniValue("Entry", price(item.entry), White, Modifier.weight(1f))
            MiniValue("Mark", price(item.mark), Cyan, Modifier.weight(1f))
            MiniValue("Allocated", String.format("%.2f", item.allocated), Purple, Modifier.weight(1f))
        }
        HorizontalDivider(color = Color(0xFF202B3A))
    }
}

@Composable
private fun DataScreen(lang: String, title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, rows: List<Pair<String, String>>, errors: List<String>, file: String) {
    ScreenColumn {
        SectionTitle(title, file)
        PanelCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Icon(icon, null, tint = Neon)
                Text(t(lang, "dataStatus"), color = White, fontWeight = FontWeight.Bold)
            }
            if (rows.isEmpty()) EmptyText(t(lang, "noData"))
            rows.forEach { (k, v) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(k, color = Muted, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Text(v.take(64), color = White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                }
                HorizontalDivider(color = Color(0xFF202B3A))
            }
            if (rows.isEmpty() && errors.isNotEmpty()) Text(errors.take(3).joinToString("\n"), color = Red, fontSize = 10.sp)
        }
        PanelCard {
            Text(if (file.startsWith("portfolio")) "Portfolio JSON snapshot" else "Backtest JSON snapshot", color = Muted, fontSize = 11.sp)
            Text("Ces valeurs sont extraites du fichier actuel du moteur. Les commandes d'exécution et les ordres réels ne sont pas déclenchés depuis cet écran.", color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun RiskLabScreen(lang: String, data: Snapshot) {
    ScreenColumn {
        SectionTitle(t(lang, "risklab"), "Integrity · Strategy decay · Portfolio risk · Operations")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("Open positions", data.openPositions.size.toString(), Neon, Modifier.weight(1f))
            MetricCard("Pending orders", data.pendingOrders.size.toString(), Purple, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("Closed trades", data.closedTrades.size.toString(), Cyan, Modifier.weight(1f))
            MetricCard("Backtest metrics", data.backtestSummary.size.toString(), if (data.backtestSummary.isNotEmpty()) Neon else Red, Modifier.weight(1f))
        }
        PanelCard {
            SectionTitle(if (lang == "ar") "فحوصات المخاطر والموثوقية" else if (lang == "fr") "Contrôles de risque et fiabilité" else "Risk & reliability controls")
            val controls = listOf(
                "Backtest integrity" to "Review stored historical metrics",
                "Anti-overfitting / Walk-forward" to "Check available validation evidence",
                "Strategy reliability" to "Compare win rate and profit factor",
                "Market regime" to "Inspect the regime attached to each signal",
                "Portfolio risk" to "Review open positions and pending orders",
                "Performance drift / Strategy decay" to "Review strategy statistics and learning memory",
                "No-Trade Intelligence" to "Treat weak or incomplete signals conservatively"
            )
            controls.forEach { (name, detail) ->
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Shield, null, tint = Cyan, modifier = Modifier.size(20.dp))
                    Column(Modifier.weight(1f)) {
                        Text(name, color = White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(detail, color = Muted, fontSize = 10.sp)
                    }
                }
            }
        }
        PanelCard {
            SectionTitle(if (lang == "ar") "بيانات التشخيص المتاحة" else if (lang == "fr") "Données de diagnostic disponibles" else "Available diagnostic data")
            if (data.riskSummary.isEmpty()) EmptyText(t(lang, "noData"))
            data.riskSummary.forEach { (key, value) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Text(key, color = Muted, fontSize = 10.sp, modifier = Modifier.weight(1f))
                    Text(value.take(80), color = White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                }
                HorizontalDivider(color = Color(0xFF202B3A))
            }
        }
        PanelCard {
            SectionTitle(t(lang, "performance"))
            if (data.strategyStats.isEmpty()) EmptyText(t(lang, "noData"))
            data.strategyStats.forEach { (name, win, pf) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(name, color = White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Text("WR ${"%.1f".format(win)}% · PF ${"%.2f".format(pf)}", color = if (pf >= 1) Neon else Red, fontSize = 10.sp)
                }
            }
        }
        Text("Read-only diagnostics: this screen does not place or execute real orders.", color = Muted, fontSize = 10.sp)
    }
}

@Composable
private fun AiScreen(lang: String, data: Snapshot) {
    ScreenColumn {
        SectionTitle(t(lang, "ai"), "Meta-Strategy · EV · No-Trade · Decay · Sizing")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard(t(lang, "strategies"), data.strategyCount.toString(), Neon, Modifier.weight(1f))
            MetricCard(t(lang, "models"), data.modelCount.toString(), Purple, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("Candle discovery", "${data.discoveryValidated}/${data.discoveryCandidates}", Cyan, Modifier.weight(1f))
            MetricCard("Entry methods", data.entryMethodCount.toString(), Purple, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("Auto-tune cycles", data.autoTuneUpdates.toString(), Neon, Modifier.weight(1f))
            MetricCard("Strategy universe", data.strategyUniverseCount.toString(), Cyan, Modifier.weight(1f))
        }
        PanelCard {
            Text(t(lang, "risk"), color = White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            listOf(
                "Meta-Strategy Selector" to "اختيار الاستراتيجية وفق أداءها",
                "Expected Value Engine" to "تقييم القيمة المتوقعة للصفقة",
                "No-Trade Intelligence" to "الامتناع عند ضعف الإشارة",
                "Strategy Decay Detector" to "رصد تراجع أداء الاستراتيجيات",
                "Dynamic Position Sizing" to "تقدير حجم المركز وفق المخاطر",
                "Multi-timeframe Analysis" to "15m · 1H · 4H · 1D"
            ).forEach { (title, sub) ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF182A31)), contentAlignment = Alignment.Center) { Icon(Icons.Default.AutoAwesome, null, tint = Cyan, modifier = Modifier.size(18.dp)) }
                    Column {
                        Text(title, color = White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(sub, color = Muted, fontSize = 10.sp)
                    }
                }
            }
        }
        PanelCard {
            SectionTitle(t(lang, "performance"))
            if (data.strategyStats.isEmpty()) EmptyText(t(lang, "noData"))
            data.strategyStats.forEach { (name, win, pf) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(name, color = White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Text("WR ${"%.1f".format(win)}% · PF ${"%.2f".format(pf)}", color = if (pf >= 1) Neon else Red, fontSize = 10.sp)
                }
            }
        }
        PanelCard {
            SectionTitle(
                if (lang == "ar") "كل الاستراتيجيات المسجلة" else if (lang == "fr") "Toutes les stratégies enregistrées" else "All registered strategies",
                "${data.strategyNames.size} · strategy_registry.py"
            )
            if (data.strategyNames.isEmpty()) EmptyText(t(lang, "noData"))
            else data.strategyNames.forEachIndexed { index, name ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text((index + 1).toString().padStart(2, '0'), color = Neon, fontSize = 10.sp, modifier = Modifier.width(28.dp))
                    Text(name, color = White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                }
                HorizontalDivider(color = Color(0xFF202B3A))
            }
        }
    }
}

@Composable
private fun NewsScreen(lang: String, data: Snapshot) {
    ScreenColumn {
        SectionTitle(t(lang, "news"), "${data.newsCount} sources · ${data.newsStatus}")
        PanelCard {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(t(lang, "newsSources"), "${data.newsCount}/${data.newsTotalSources}", Cyan, Modifier.weight(1f))
                MetricCard("Risk", data.newsRisk, if (data.newsRisk.equals("LOW", true)) Neon else Red, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("High impact", data.newsHighImpact.toString(), Purple, Modifier.weight(1f))
                MetricCard("Health", data.newsStatus, if (data.newsStatus.equals("OK", true)) Neon else Red, Modifier.weight(1f))
            }
            Text("Last update: ${data.updatedAt}", color = Muted, fontSize = 10.sp)
        }
        PanelCard {
            SectionTitle("Latest headlines")
            if (data.newsHeadlines.isEmpty()) EmptyText(t(lang, "noData"))
            data.newsHeadlines.forEachIndexed { i, headline ->
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(25.dp).clip(CircleShape).background(Panel2), contentAlignment = Alignment.Center) { Text("${i + 1}", color = Neon, fontSize = 11.sp) }
                    Text(headline, color = White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                }
                HorizontalDivider(color = Color(0xFF202B3A))
            }
        }
    }
}

@Composable
private fun SettingsScreen(lang: String, onLanguage: (String) -> Unit, owner: String, setOwner: (String) -> Unit, repo: String, setRepo: (String) -> Unit, branch: String, setBranch: (String) -> Unit, token: String, setToken: (String) -> Unit, save: () -> Unit, loading: Boolean, error: String) {
    ScreenColumn {
        SectionTitle(t(lang, "settings"), t(lang, "connect"))
        PanelCard {
            Text(t(lang, "language"), color = White, fontWeight = FontWeight.Bold)
            val languages = listOf("fr" to "🇫🇷 Français", "en" to "🇬🇧 English", "ar" to "🇸🇦 العربية", "es" to "🇪🇸 Español", "tr" to "🇹🇷 Türkçe", "zh" to "🇨🇳 中文")
            languages.forEach { (code, label) ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, color = White, fontSize = 13.sp)
                    RadioButton(selected = lang == code, onClick = { onLanguage(code) }, colors = RadioButtonDefaults.colors(selectedColor = Neon))
                }
            }
        }
        PanelCard {
            Text(t(lang, "connect"), color = White, fontWeight = FontWeight.Bold)
            OutlinedTextField(owner, setOwner, label = { Text(t(lang, "owner")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(repo, setRepo, label = { Text(t(lang, "repo")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(branch, setBranch, label = { Text(t(lang, "branch")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(token, setToken, label = { Text(t(lang, "token")) }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            Text(t(lang, "githubHelp"), color = Muted, fontSize = 11.sp)
            Button(onClick = save, enabled = !loading && owner.isNotBlank() && repo.isNotBlank() && branch.isNotBlank(), modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Neon, contentColor = Bg), shape = RoundedCornerShape(12.dp)) {
                if (loading) CircularProgressIndicator(Modifier.size(18.dp), color = Bg, strokeWidth = 2.dp)
                else Text(t(lang, "save"), fontWeight = FontWeight.Bold)
            }
            if (error.isNotBlank()) Text(error, color = Red, fontSize = 10.sp)
        }
        PanelCard {
            Text("Security", color = White, fontWeight = FontWeight.Bold)
            Text("Le jeton saisi est stocké dans le stockage chiffré Android. Utilisez un jeton en lecture seule pour consulter les données; ne collez jamais une clé secrète OKX ici.", color = Muted, fontSize = 11.sp)
        }
        Text(t(lang, "notAdvice"), color = Muted, fontSize = 10.sp)
    }
}

@Composable
private fun EmptyText(text: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.CloudOff, null, tint = Muted, modifier = Modifier.size(26.dp))
        Spacer(Modifier.height(7.dp))
        Text(text, color = Muted, fontSize = 12.sp)
    }
}

private fun price(v: Double): String = when {
    v == 0.0 -> "—"
    abs(v) >= 1000 -> "$" + String.format("%,.2f", v)
    abs(v) >= 1 -> "$" + String.format("%.3f", v)
    else -> "$" + String.format("%.6f", v)
}

private fun compact(v: Double): String = when {
    v >= 1_000_000_000 -> String.format("%.1fB", v / 1_000_000_000)
    v >= 1_000_000 -> String.format("%.1fM", v / 1_000_000)
    v >= 1_000 -> String.format("%.1fK", v / 1_000)
    else -> String.format("%.0f", v)
}
