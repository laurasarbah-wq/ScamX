package ae.scamx.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.foundation.Canvas
import org.jetbrains.compose.resources.Font
import ae.scamx.app.resources.Res
import ae.scamx.app.resources.noto_sans_arabic
import kotlinx.coroutines.launch

private val Red = Color(0xFFD62828)
private val DarkRed = Color(0xFF9B1C1C)
private val Ink = Color(0xFF171A21)
private val Muted = Color(0xFF666B76)
private val Page = Color(0xFFF7F7F5)
private val Border = Color(0xFFE4E2DE)
private val Safe = Color(0xFF16805C)
private val Amber = Color(0xFFC47700)

enum class InputKind { MESSAGE, LINK, PHONE }
enum class AppScreen { HOME, REPORT, COMMUNITY }

@Composable
fun ScamXApp() {
    var arabic by remember { mutableStateOf(false) }
    var screen by remember { mutableStateOf(AppScreen.HOME) }
    val appFont = FontFamily(Font(Res.font.noto_sans_arabic))
    CompositionLocalProvider(LocalLayoutDirection provides if (arabic) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = Red,
                onPrimary = Color.White,
                surface = Color.White,
                background = Page,
                onSurface = Ink,
            ),
            typography = scamXTypography(appFont),
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFFBFA), Page, Color(0xFFF2F5F7))))) {
                Column(Modifier.fillMaxSize()) {
                    Header(arabic, onLanguageChange = { arabic = !arabic })
                    ScamNavigation(screen, arabic) { screen = it }
                    key(screen) {
                        BoxWithConstraints(
                            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            val horizontal = if (maxWidth < 600.dp) 20.dp else 40.dp
                            Column(
                                modifier = Modifier.widthIn(max = 1120.dp).fillMaxWidth().padding(horizontal, 36.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                when (screen) {
                                    AppScreen.HOME -> HomePage(arabic) { screen = AppScreen.REPORT }
                                    AppScreen.REPORT -> ReportPage(arabic)
                                    AppScreen.COMMUNITY -> CommunityPage(arabic)
                                }
                                Spacer(Modifier.height(36.dp))
                                Footer(arabic)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScamNavigation(selected: AppScreen, arabic: Boolean, onSelect: (AppScreen) -> Unit) {
    Surface(color = Color.White, tonalElevation = 1.dp) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Row(Modifier.fillMaxWidth().widthIn(max = 620.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NavItem(AppScreen.HOME, selected, "⌂", if (arabic) "الرئيسية" else "Home", Modifier.weight(1f), onSelect)
                NavItem(AppScreen.REPORT, selected, "⚑", if (arabic) "الإبلاغ" else "Report", Modifier.weight(1f), onSelect)
                NavItem(AppScreen.COMMUNITY, selected, "●●", if (arabic) "المجتمع" else "Community", Modifier.weight(1f), onSelect)
            }
        }
    }
}

@Composable
private fun NavItem(screen: AppScreen, selected: AppScreen, icon: String, label: String, modifier: Modifier, onSelect: (AppScreen) -> Unit) {
    val active = screen == selected
    TextButton(
        onClick = { onSelect(screen) },
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(containerColor = if (active) Color(0xFFFFE9E7) else Color.Transparent),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text("$icon  $label", color = if (active) DarkRed else Muted, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun HomePage(arabic: Boolean, onReport: () -> Unit) {
    Hero(arabic)
    Spacer(Modifier.height(32.dp))
    Checker(arabic, onReport)
    Spacer(Modifier.height(28.dp))
    ResearchStrip(arabic)
    Spacer(Modifier.height(18.dp))
    PrivacyNote(arabic)
}

private fun scamXTypography(font: FontFamily): Typography {
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = font),
        displayMedium = base.displayMedium.copy(fontFamily = font),
        displaySmall = base.displaySmall.copy(fontFamily = font),
        headlineLarge = base.headlineLarge.copy(fontFamily = font, fontWeight = FontWeight.Bold),
        headlineMedium = base.headlineMedium.copy(fontFamily = font),
        headlineSmall = base.headlineSmall.copy(fontFamily = font, fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.copy(fontFamily = font),
        titleMedium = base.titleMedium.copy(fontFamily = font, fontWeight = FontWeight.SemiBold),
        titleSmall = base.titleSmall.copy(fontFamily = font),
        bodyLarge = base.bodyLarge.copy(fontFamily = font, lineHeight = 25.sp),
        bodyMedium = base.bodyMedium.copy(fontFamily = font),
        bodySmall = base.bodySmall.copy(fontFamily = font),
        labelLarge = base.labelLarge.copy(fontFamily = font),
        labelMedium = base.labelMedium.copy(fontFamily = font),
        labelSmall = base.labelSmall.copy(fontFamily = font),
    )
}

@Composable
private fun Header(arabic: Boolean, onLanguageChange: () -> Unit) {
    Surface(color = Color.White, shadowElevation = 1.dp) {
        Row(
            Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ScamXLogo(40.dp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("ScamX", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                    Text("UAE RULES • v1.2", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Safe)
                }
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onLanguageChange) {
                Text(if (arabic) "English" else "العربية", color = DarkRed, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun Hero(arabic: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(color = Color(0xFFFFE9E7), shape = CircleShape) {
            Text(
                if (arabic) "خاص وآمن • يعمل على جهازك" else "PRIVATE & SAFE • CHECKED ON YOUR DEVICE",
                color = DarkRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
        Spacer(Modifier.height(22.dp))
        Text(
            if (arabic) "توقّف. تحقّق. احمِ نفسك." else "Pause. Check. Protect.",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            color = Ink,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            if (arabic) "افحص رسالة أو رابطاً أو رقم هاتف بحثاً عن علامات الخطر الشائعة — من دون تحميل أي شيء." else "Check a message, link, or phone number for common warning signs — without uploading anything.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Muted,
            modifier = Modifier.widthIn(max = 680.dp),
        )
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("1  ${if (arabic) "اختر مثالاً" else "Choose"}", "2  ${if (arabic) "افحص" else "Check"}", "3  ${if (arabic) "تصرّف بأمان" else "Act safely"}").forEach {
                Surface(color = Color.White, shape = CircleShape, border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
                    Text(it, color = Muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun Checker(arabic: Boolean, onReport: () -> Unit) {
    var value by remember { mutableStateOf(TextFieldValue("")) }
    var kind by remember { mutableStateOf(InputKind.MESSAGE) }
    var result by remember { mutableStateOf<RiskResult?>(null) }
    var showError by remember { mutableStateOf(false) }
    var onlineConsent by remember { mutableStateOf(false) }
    var onlineState by remember { mutableStateOf<String?>(null) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var showAbout by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    if (showAbout) AlertDialog(
        onDismissRequest = { showAbout = false },
        confirmButton = { TextButton(onClick = { showAbout = false }) { Text(if (arabic) "حسناً" else "Got it") } },
        title = { Text(if (arabic) "كيف تعمل النتيجة؟" else "How the score works") },
        text = { Text(if (arabic) "تجمع ScamX نقاط مؤشرات الخطر الشائعة. النتيجة ليست إثباتاً، وقد تخطئ الأداة. تحقق دائماً عبر قناة رسمية." else "ScamX adds points for common risk indicators. A score is not proof, and the checker can be wrong. Always verify through an official channel.") },
    )

    Card(
        modifier = Modifier.fillMaxWidth().widthIn(max = 820.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(Modifier.padding(24.dp)) {
            Text(if (arabic) "ماذا تريد أن تفحص؟" else "What would you like to check?", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(if (arabic) "جرّب مثالاً سريعاً" else "Try a quick example", color = Muted, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                demoExamples.forEach { example ->
                    AssistChip(
                        onClick = { value = TextFieldValue(example.text); kind = InputKind.MESSAGE; result = null; feedback = null },
                        label = { Text(if (arabic) example.titleAr else example.titleEn, fontSize = 11.sp) },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TypeChip(InputKind.MESSAGE, kind, if (arabic) "رسالة" else "Message", Modifier.weight(1f)) { kind = it; result = null }
                TypeChip(InputKind.LINK, kind, if (arabic) "رابط" else "Link", Modifier.weight(1f)) { kind = it; result = null }
                TypeChip(InputKind.PHONE, kind, if (arabic) "رقم" else "Phone", Modifier.weight(1f)) { kind = it; result = null }
            }
            Spacer(Modifier.height(18.dp))
            OutlinedTextField(
                value = value,
                onValueChange = { value = it; showError = false; result = null },
                modifier = Modifier.fillMaxWidth().heightIn(min = if (kind == InputKind.MESSAGE) 160.dp else 92.dp),
                label = { Text(if (arabic) "ألصق المحتوى هنا" else "Paste content here") },
                placeholder = {
                    Text(
                        when (kind) {
                            InputKind.MESSAGE -> if (arabic) "ألصق الرسالة المشبوهة…" else "Paste the suspicious message…"
                            InputKind.LINK -> if (arabic) "https://example.com" else "https://example.com"
                            InputKind.PHONE -> if (arabic) "+971 50 000 0000" else "+971 50 000 0000"
                        }
                    )
                },
                isError = showError,
                supportingText = if (showError) { { Text(if (arabic) "ألصق رسالة أو رابطاً أو رقم هاتف أولاً." else "Paste a message, link, or phone number first.") } } else null,
                shape = RoundedCornerShape(14.dp),
                singleLine = kind != InputKind.MESSAGE,
            )
            Row(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = onlineConsent, onCheckedChange = { onlineConsent = it; onlineState = null })
                Column {
                    Text(if (arabic) "فحص سمعة الرابط عبر الإنترنت" else "Check link reputation online", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(if (arabic) "بموافقتك، يُرسل الرابط المستخرج فقط — وليس الرسالة." else "With consent, only an extracted URL is sent—not the message.", color = Muted, fontSize = 11.sp)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { value = TextFieldValue(""); result = null; onlineConsent = false; onlineState = null; showError = false; feedback = null },
                modifier = Modifier.height(54.dp),
                shape = RoundedCornerShape(14.dp),
            ) { Text(if (arabic) "مسح" else "Clear") }
            Button(
                onClick = {
                    if (value.text.isBlank()) {
                        showError = true
                    } else {
                        result = RiskEngine.check(value.text)
                        onlineState = if (onlineConsent) "checking" else null
                        if (onlineConsent) scope.launch { onlineState = ScamXApi.check(value.text).state }
                    }
                },
                modifier = Modifier.weight(1f).height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red),
            ) {
                Text(if (arabic) "افحص الآن" else "Check now", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
            }
            Text(
                if (onlineConsent) {
                    if (arabic) "يبقى نص الرسالة على جهازك؛ قد يُفحص الرابط المستخرج بعد موافقتك." else "Message text stays on your device; an extracted URL may be checked with your consent."
                } else {
                    if (arabic) "لا يغادر النص جهازك أبداً." else "Your text never leaves this device."
                },
                color = Muted,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth().padding(top = 9.dp),
                textAlign = TextAlign.Center,
            )
            result?.let {
                Spacer(Modifier.height(24.dp))
                ResultCard(it, arabic)
                onlineState?.let { state ->
                    Spacer(Modifier.height(12.dp))
                OnlineStatusCard(state, arabic)
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        val advice = if (arabic) "تم رصد مؤشرات خطر. لا تضغط على الرابط أو تدفع أو تشارك رمز التحقق. تحقق عبر التطبيق أو الموقع الرسمي." else "Risk indicators were detected. Do not click, pay, or share an OTP. Verify through the organisation's official app or website."
                        clipboard.setText(AnnotatedString(advice)); feedback = if (arabic) "تم نسخ نصيحة السلامة." else "Safety advice copied."
                    }, modifier = Modifier.weight(1f)) { Text(if (arabic) "نسخ النصيحة" else "Copy advice") }
                    if (it.level != RiskLevel.LOW) OutlinedButton(onClick = onReport, modifier = Modifier.weight(1f)) { Text(if (arabic) "الإبلاغ بأمان" else "Report safely") }
                }
                TextButton(onClick = { showAbout = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text(if (arabic) "حول النتيجة" else "About this score") }
                Text(if (arabic) "هل كانت النتيجة مفيدة؟" else "Was this result helpful?", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Helpful" to "مفيدة", "Missed a warning" to "فاتها تحذير", "Too severe" to "شديدة جداً").forEach { option ->
                        FilterChip(selected = feedback == option.first, onClick = { feedback = option.first }, label = { Text(if (arabic) option.second else option.first, fontSize = 10.sp) })
                    }
                }
                feedback?.let { if (it !in listOf("Helpful", "Missed a warning", "Too severe")) Text(it, color = Safe, fontSize = 12.sp) }
            }
        }
    }
}

@Composable
private fun OnlineStatusCard(state: String, arabic: Boolean) {
    val matched = state == "matched"
    val color = if (matched) Red else if (state == "clear") Safe else Muted
    val text = when (state) {
        "checking" -> if (arabic) "جارٍ فحص قاعدة السمعة عبر الإنترنت…" else "Checking the online reputation service…"
        "matched" -> if (arabic) "وجدت خدمة السمعة تطابقاً معروفاً. لا تفتح الرابط." else "The reputation service found a known threat match. Do not open the link."
        "clear" -> if (arabic) "لم تجد خدمة السمعة تطابقاً معروفاً. هذا لا يضمن أن الرابط آمن." else "No known match was found online. This does not guarantee that the link is safe."
        "no_url" -> if (arabic) "لم يتم العثور على رابط لفحصه عبر الإنترنت. بقي نص الرسالة على جهازك." else "No link was found for an online check. The pasted message stayed on your device."
        "not_configured" -> if (arabic) "خدمة الإنترنت جاهزة في المشروع لكنها تحتاج عنوان API المنشور." else "The online service is implemented but needs the deployed API URL in the project configuration."
        else -> if (arabic) "تعذر الوصول إلى خدمة السمعة. اعتمد على مؤشرات الجهاز وتحقق مستقلاً." else "The reputation service could not be reached. Use the on-device indicators and verify independently."
    }
    Surface(color = color.copy(alpha = .09f), shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = .25f))) {
        Text(text, color = color, modifier = Modifier.padding(14.dp), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TypeChip(kind: InputKind, selected: InputKind, label: String, modifier: Modifier, onClick: (InputKind) -> Unit) {
    val active = kind == selected
    Surface(
        onClick = { onClick(kind) },
        modifier = modifier,
        color = if (active) Color(0xFFFFE9E7) else Color.White,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (active) Red else Border),
    ) {
        Text(label, color = if (active) DarkRed else Muted, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 12.dp))
    }
}

@Composable
private fun ResultCard(result: RiskResult, arabic: Boolean) {
    val color = when (result.level) { RiskLevel.LOW -> Safe; RiskLevel.SUSPICIOUS -> Amber; RiskLevel.HIGH -> Red }
    val background = when (result.level) { RiskLevel.LOW -> Color(0xFFEAF7F1); RiskLevel.SUSPICIOUS -> Color(0xFFFFF4DA); RiskLevel.HIGH -> Color(0xFFFFE9E7) }
    val label = when (result.level) {
        RiskLevel.LOW -> if (arabic) "خطر منخفض" else "Low risk"
        RiskLevel.SUSPICIOUS -> if (arabic) "مريب" else "Suspicious"
        RiskLevel.HIGH -> if (arabic) "خطر مرتفع" else "High risk"
    }
    Surface(color = background, shape = RoundedCornerShape(18.dp), border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = .25f))) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).clip(CircleShape).background(color))
                Spacer(Modifier.width(10.dp))
                Text(label, color = color, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.weight(1f))
                Surface(color = Color.White.copy(alpha = .75f), shape = CircleShape) {
                    Text(if (arabic) "${result.score} نقاط" else "${result.score} points", color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { (result.score.coerceAtMost(10) / 10f) },
                modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                color = color,
                trackColor = Color.White.copy(alpha = .75f),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                when (result.level) {
                    RiskLevel.LOW -> if (arabic) "لم يتم العثور على علامات خطر واضحة. هذا لا يضمن أن المحتوى آمن." else "No obvious warning signs were found. This does not guarantee that the message is safe."
                    RiskLevel.SUSPICIOUS -> if (arabic) "تم رصد مؤشرات خطر. كن حذراً وتحقق بشكل مستقل." else "Risk indicators were detected. Be careful and verify independently."
                    RiskLevel.HIGH -> if (arabic) "تم رصد مؤشرات خطر قوية. لا تضغط أو تدفع أو تشارك معلومات." else "Strong risk indicators were detected. Do not click, pay, or share information."
                },
                color = Ink,
            )
            if (result.reasons.isNotEmpty()) {
                HorizontalDivider(Modifier.padding(vertical = 16.dp), color = color.copy(alpha = .2f))
                Text(if (arabic) "لماذا ظهرت هذه النتيجة" else "Why this result", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                result.reasons.forEach { reason ->
                    Row(Modifier.padding(vertical = 5.dp)) {
                        Text("+${reason.points}", color = color, fontWeight = FontWeight.ExtraBold, modifier = Modifier.width(34.dp))
                        Column {
                            Text(if (arabic) reason.titleAr else reason.titleEn, fontWeight = FontWeight.SemiBold)
                            Text(if (arabic) reason.detailAr else reason.detailEn, color = Muted, fontSize = 13.sp)
                            reason.matchedText?.let { phrase ->
                                Surface(color = color.copy(alpha = .12f), shape = RoundedCornerShape(6.dp), modifier = Modifier.padding(top = 5.dp)) {
                                    Text("“$phrase”", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
                                }
                            }
                        }
                    }
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 16.dp), color = color.copy(alpha = .2f))
            Text(if (arabic) "الخطوات الآمنة التالية" else "Safe next steps", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            val tips = if (arabic) listOf("لا تشارك أبداً رمز التحقق أو كلمة المرور.", "لا تفتح الرابط. استخدم التطبيق أو الموقع الرسمي للجهة.", "اتصل بالجهة عبر رقم موثوق وجدته بنفسك.") else listOf("Never share an OTP or password.", "Do not open the link. Use the organisation’s official app or website.", "Contact the organisation using a trusted number you found yourself.")
            tips.forEach { Text("•  $it", color = Ink, modifier = Modifier.padding(vertical = 3.dp)) }
        }
    }
}

@Composable
private fun ResearchStrip(arabic: Boolean) {
    Surface(
        color = Ink,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().widthIn(max = 820.dp),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(if (arabic) "لماذا نتحقق؟" else "Why checking matters", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Stat("56%", if (arabic) "يتلقون محاولة شهرياً" else "receive a monthly attempt", Modifier.weight(1f))
                Stat("49%", if (arabic) "تكتمل خلال 24 ساعة" else "finish within 24 hours", Modifier.weight(1f))
                Stat("30%", if (arabic) "فقط أبلغوا الشرطة" else "reported to police", Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Text(if (arabic) "المصدر: دراسة حالة الاحتيال في الإمارات 2024. الأرقام تصف عينة الاستطلاع وليست عدداً رسمياً لكل الجرائم." else "Source: 2024 State of Scams in the UAE survey. Figures describe the survey sample, not an official count of every crime.", color = Color(0xFFBABDC5), fontSize = 11.sp)
        }
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier) {
    Column(modifier.padding(horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 23.sp)
        Text(label, color = Color(0xFFD8D9DE), fontSize = 11.sp, textAlign = TextAlign.Center, lineHeight = 15.sp)
    }
}

@Composable
private fun ReportPage(arabic: Boolean) {
    var selectedEmirate by remember { mutableStateOf("All UAE") }
    val uriHandler = LocalUriHandler.current
    Column(Modifier.fillMaxWidth().widthIn(max = 880.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        PageHeading(
            if (arabic) "أبلغ أو احصل على مساعدة" else "Report or get help",
            if (arabic) "اختر إمارتك للوصول إلى القناة الرسمية المناسبة. ScamX لا يستقبل بلاغاً شرطياً نيابةً عنك." else "Choose your emirate to find the right official channel. ScamX does not file a police report for you.",
        )
        Spacer(Modifier.height(22.dp))
        Surface(color = Color(0xFFFFE9E7), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text(if (arabic) "إذا شاركت مالاً أو بيانات" else "If money or information was shared", color = DarkRed, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(if (arabic) "أوقف التواصل ← اتصل بالبنك ← أمّن حساباتك ← احفظ الأدلة ← أبلغ رسمياً" else "Stop contact → Call your bank → Secure accounts → Save evidence → Report officially", color = Ink, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(if (arabic) "في حالة خطر فوري اتصل بـ 999." else "For immediate danger, call 999.", color = Red, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(20.dp))
        DropdownField(
            if (arabic) "الإمارة" else "Emirate",
            selectedEmirate,
            emirates,
            arabic,
            Modifier.fillMaxWidth(),
        ) { selectedEmirate = it }
        Spacer(Modifier.height(18.dp))
        officialChannels.filter { "All UAE" in it.emirates || selectedEmirate == "All UAE" || selectedEmirate in it.emirates }.forEach { channel ->
            Card(
                Modifier.fillMaxWidth().padding(vertical = 7.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Border),
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("✓", color = Safe, fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.width(9.dp))
                        Text(if (arabic) channel.nameAr else channel.nameEn, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(if (arabic) channel.summaryAr else channel.summaryEn, color = Muted)
                    channel.contact?.let { Text(it, color = Ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp)) }
                    Spacer(Modifier.height(13.dp))
                    OutlinedButton(onClick = { uriHandler.openUri(channel.url) }, shape = RoundedCornerShape(12.dp)) {
                        Text(if (arabic) "افتح الموقع الرسمي" else "Open official website", color = DarkRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityPage(arabic: Boolean) {
    var selectedEmirate by remember { mutableStateOf("Dubai") }
    var selectedType by remember { mutableStateOf(scamTypes.first()) }
    var otherType by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var posts by remember { mutableStateOf(communitySeed) }
    var notice by remember { mutableStateOf<String?>(null) }
    var moderationConsent by remember { mutableStateOf(false) }
    var filterEmirate by remember { mutableStateOf("All UAE") }
    var filterType by remember { mutableStateOf("All types") }
    val scope = rememberCoroutineScope()
    val safety = remember(notes) { checkCommunitySafety(notes) }

    Column(Modifier.fillMaxWidth().widthIn(max = 880.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        PageHeading(
            if (arabic) "مجتمع ScamX" else "ScamX community",
            if (arabic) "تجارب مجهولة تساعد الآخرين على التعرف على الأنماط. المشاركات ليست دليلاً على أن شخصاً بعينه محتال." else "Anonymous experiences help others recognize patterns. Posts are not proof that a particular person is a scammer.",
        )
        Spacer(Modifier.height(18.dp))
        Surface(color = Color(0xFFFFF4DA), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Text(if (arabic) "تُنشر المشاركات بعد مراجعة بشرية فقط. الروابط والأرقام واللغة المسيئة والتهديدات والاتهامات بالأسماء تُعلّم أو تُحذف." else "Posts are published only after human review. Links, numbers, abusive language, threats, and accusations naming people are flagged or removed.", color = Color(0xFF704700), modifier = Modifier.padding(16.dp), fontSize = 13.sp)
        }
        Spacer(Modifier.height(18.dp))
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(20.dp)) {
                Text(if (arabic) "شارك تجربة مجهولة" else "Share an anonymous experience", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(16.dp))
                DropdownField(if (arabic) "الإمارة" else "Emirate", selectedEmirate, emirates.drop(1), arabic, Modifier.fillMaxWidth()) { selectedEmirate = it }
                Spacer(Modifier.height(12.dp))
                DropdownField(if (arabic) "نوع الاحتيال" else "Scam type", selectedType, scamTypes, arabic, Modifier.fillMaxWidth()) { selectedType = it }
                if (selectedType == "Other") {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = otherType, onValueChange = { otherType = it.take(60) }, modifier = Modifier.fillMaxWidth(), label = { Text(if (arabic) "اكتب النوع" else "Describe the type") }, singleLine = true, shape = RoundedCornerShape(12.dp))
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it.take(600); notice = null },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 130.dp),
                    label = { Text(if (arabic) "ملاحظات بدون بيانات شخصية" else "Notes — no personal information") },
                    placeholder = { Text(if (arabic) "اشرح الأسلوب المستخدم وما الذي نبهك…" else "Describe the approach and what raised concern…") },
                    shape = RoundedCornerShape(12.dp),
                )
                if (notes.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Surface(color = if (safety.acceptable) Color(0xFFEAF7F1) else Color(0xFFFFE9E7), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(if (safety.acceptable) (if (arabic) "● أخضر — لم يرصد الفحص المحلي مخالفة واضحة" else "● Green — no obvious local policy issue detected") else (if (arabic) "⚑ سيُعلّم للمشرف" else "⚑ Will be flagged for a moderator"), color = if (safety.acceptable) Safe else Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            (if (arabic) safety.reasonsAr else safety.reasonsEn).forEach { Text("• $it", color = Ink, fontSize = 11.sp) }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = moderationConsent, onCheckedChange = { moderationConsent = it })
                    Text(if (arabic) "أوافق على إرسال النص المنقح إلى خدمة OpenAI للمساعدة في المراجعة. القرار النهائي للمشرف." else "I agree that the redacted text may be sent to OpenAI to assist moderation. A human makes the final decision.", color = Muted, fontSize = 11.sp)
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {
                        if (!moderationConsent) {
                            notice = if (arabic) "وافق على فحص المراجعة قبل الإرسال." else "Please consent to the moderation check before submitting."
                        } else if (notes.trim().length < 20 || (selectedType == "Other" && otherType.trim().length < 3)) {
                            notice = if (arabic) "أضف وصفاً أوضح قبل الإرسال." else "Add a clearer description before submitting."
                        } else {
                            val safeNotes = redactCommunityText(notes)
                            val finalType = if (selectedType == "Other") otherType.trim() else selectedType
                            val pendingPost = CommunityPost("local-${posts.size}", selectedEmirate, finalType, safeNotes, if (safety.acceptable) "pending" else "flagged", if (arabic) "بانتظار المراجعة" else "Awaiting moderation")
                            posts = listOf(pendingPost) + posts
                            notes = ""
                            otherType = ""
                            moderationConsent = false
                            notice = if (ScamXApi.configured) {
                                if (arabic) "جارٍ إرسال المشاركة الآمنة إلى قائمة المراجعة…" else "Sending the redacted submission to moderation…"
                            } else {
                                if (arabic) "حُفظت محلياً. انشر خدمة API لمشاركة البلاغ مع المجتمع." else "Saved locally. Deploy the API to submit it to the shared moderation queue."
                            }
                            if (ScamXApi.configured) scope.launch {
                                notice = when (ScamXApi.submit(pendingPost)) {
                                    "sent_green" -> if (arabic) "وصلت للمشرف: أخضر مبدئياً، والقرار النهائي بشري." else "Sent to moderators: provisionally green; a human makes the final decision."
                                    "sent_flagged" -> if (arabic) "وصلت للمشرف ومعلّمة للمراجعة. لن تُنشر تلقائياً." else "Sent and flagged for moderator review. It will not be published automatically."
                                    "sent_unavailable" -> if (arabic) "وصلت للمشرف؛ فحص AI غير متاح والقرار للمشرف." else "Sent to moderators; AI was unavailable and a human will review it."
                                    else -> if (arabic) "تعذر الإرسال؛ بقيت النسخة المحلية محفوظة في هذه الجلسة." else "Could not send; the local copy remains in this session."
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Red),
                    shape = RoundedCornerShape(12.dp),
                ) { Text(if (arabic) "إرسال للمراجعة" else "Submit for moderation", fontWeight = FontWeight.Bold) }
                notice?.let { Text(it, color = if (notes.isBlank()) Safe else Red, modifier = Modifier.padding(top = 10.dp), fontSize = 13.sp) }
            }
        }
        Spacer(Modifier.height(26.dp))
        Text(if (arabic) "تنبيهات المجتمع" else "Community alerts", fontWeight = FontWeight.Bold, fontSize = 22.sp, modifier = Modifier.fillMaxWidth())
        Text(if (arabic) "المشاركات المنشورة راجعها مشرف، لكنها تظل تجارب مجتمعية غير موثقة رسمياً." else "Published posts were moderator-reviewed, but remain unverified community experiences.", color = Muted, fontSize = 12.sp, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownField(if (arabic) "تصفية بالإمارة" else "Filter emirate", filterEmirate, emirates, arabic, Modifier.weight(1f)) { filterEmirate = it }
            DropdownField(if (arabic) "تصفية بالنوع" else "Filter type", filterType, listOf("All types") + scamTypes, arabic, Modifier.weight(1f)) { filterType = it }
        }
        Spacer(Modifier.height(8.dp))
        posts.filter { (filterEmirate == "All UAE" || it.emirate == filterEmirate) && (filterType == "All types" || it.scamType == filterType) }
            .forEach { post -> CommunityCard(post, arabic) }
    }
}

@Composable
private fun CommunityCard(post: CommunityPost, arabic: Boolean) {
    val pending = post.status == "pending"
    val flagged = post.status == "flagged"
    Card(
        Modifier.fillMaxWidth().padding(vertical = 7.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = if (flagged) Color(0xFFFFE9E7) else if (pending) Color(0xFFFFF4DA) else Color(0xFFEAF7F1), shape = CircleShape) {
                    Text(if (flagged) (if (arabic) "معلّم للمراجعة" else "Flagged") else if (pending) (if (arabic) "قيد المراجعة" else "Pending") else (if (arabic) "تمت المراجعة" else "Moderated"), color = if (flagged) Red else if (pending) Amber else Safe, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                }
                Spacer(Modifier.weight(1f))
                Text(post.date, color = Muted, fontSize = 11.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text("${post.emirate} • ${post.scamType}", color = DarkRed, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(post.notes, color = Ink)
        }
    }
}

@Composable
private fun PageHeading(title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center, color = Ink)
        Spacer(Modifier.height(10.dp))
        Text(subtitle, color = Muted, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 720.dp))
    }
}

@Composable
private fun DropdownField(label: String, selected: String, values: List<String>, arabic: Boolean, modifier: Modifier = Modifier, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(label, color = Muted, fontSize = 10.sp)
                Text(localizedChoice(selected, arabic), color = Ink, fontWeight = FontWeight.SemiBold)
            }
            Text("⌄", color = DarkRed, fontSize = 20.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            values.forEach { value ->
                DropdownMenuItem(text = { Text(localizedChoice(value, arabic)) }, onClick = { onSelect(value); expanded = false })
            }
        }
    }
}

@Composable
private fun PrivacyNote(arabic: Boolean) {
    Row(
        Modifier.fillMaxWidth().widthIn(max = 820.dp).border(1.dp, Border, RoundedCornerShape(16.dp)).padding(18.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text("✓", color = Safe, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(if (arabic) "مصمم للخصوصية" else "Designed for privacy", fontWeight = FontWeight.Bold)
            Text(if (arabic) "يُحلل نص الرسالة محلياً. الفحص عبر الإنترنت اختياري ولا يرسل إلا الرابط المستخرج، ولا يفتح الروابط." else "Message analysis runs locally. The optional online check sends only an extracted link, and suspicious links are never opened.", color = Muted, fontSize = 14.sp)
        }
    }
}

@Composable
private fun Footer(arabic: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(if (arabic) "ScamX يرصد مؤشرات الخطر ولا يتهم أي شخص بالاحتيال." else "ScamX detects risk indicators. It does not accuse anyone of being a scammer.", color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(if (arabic) "للبلاغات الرسمية في الإمارات: eCrime أو أقرب مركز شرطة." else "For official UAE reporting: use eCrime or contact your local police.", color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(if (arabic) "قواعد الكشف: UAE v1.2 • آخر مراجعة: سبتمبر 2026" else "Detection rules: UAE v1.2 • Last reviewed: September 2026", color = Safe, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ScamXLogo(size: androidx.compose.ui.unit.Dp) {
    Box(Modifier.size(size).clip(RoundedCornerShape(size * .28f)).background(Red), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size * .62f)) {
            val w = this.size.width
            val h = this.size.height
            val shield = Path().apply {
                moveTo(w * .5f, h * .05f)
                lineTo(w * .88f, h * .2f)
                lineTo(w * .83f, h * .62f)
                quadraticTo(w * .72f, h * .87f, w * .5f, h * .96f)
                quadraticTo(w * .28f, h * .87f, w * .17f, h * .62f)
                lineTo(w * .12f, h * .2f)
                close()
            }
            drawPath(shield, Color.White, style = Stroke(width = w * .10f))
            drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(w * .32f, h * .34f), end = androidx.compose.ui.geometry.Offset(w * .68f, h * .70f), strokeWidth = w * .10f)
            drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(w * .68f, h * .34f), end = androidx.compose.ui.geometry.Offset(w * .32f, h * .70f), strokeWidth = w * .10f)
        }
    }
}
