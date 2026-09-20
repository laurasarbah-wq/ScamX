package ae.scamx.app

data class OfficialChannel(
    val nameEn: String,
    val nameAr: String,
    val emirates: Set<String>,
    val summaryEn: String,
    val summaryAr: String,
    val url: String,
    val contact: String? = null,
)

data class CommunityPost(
    val id: String,
    val emirate: String,
    val scamType: String,
    val notes: String,
    val status: String = "verified",
    val date: String,
)

data class DemoExample(val titleEn: String, val titleAr: String, val text: String)

data class CommunitySafetyResult(val acceptable: Boolean, val reasonsEn: List<String>, val reasonsAr: List<String>)

val demoExamples = listOf(
    DemoExample("Bank OTP request", "طلب رمز بنك", "Emirates NBD security team: your account will be suspended today. Send the OTP immediately."),
    DemoExample("Delivery fee", "رسوم توصيل", "Your Emirates Post parcel is held. Pay AED 3 now at https://bit.ly/delivery-fee"),
    DemoExample("Traffic fine", "مخالفة مرورية", "Final RTA warning: pay your overdue traffic fine today or face legal action."),
    DemoExample("Task job", "وظيفة مهام", "Part-time task job: like products to earn AED 500 daily. Recharge your account to unlock commission."),
    DemoExample("Investment offer", "عرض استثماري", "Guaranteed crypto profit. Send USDT now for a 30% return today."),
    DemoExample("Legitimate reminder", "تذكير طبيعي", "Reminder: your clinic appointment is tomorrow at 10:00. Contact the clinic through its official number to reschedule."),
)

val emirates = listOf("All UAE", "Abu Dhabi", "Dubai", "Sharjah", "Ajman", "Umm Al Quwain", "Ras Al Khaimah", "Fujairah")

val scamTypes = listOf(
    "Bank impersonation", "Delivery / customs", "Fake shopping", "Investment / crypto",
    "Job / task scam", "Government impersonation", "Account takeover", "Romance / extortion", "Other",
)

private val arabicChoices = mapOf(
    "All UAE" to "كل الإمارات", "Abu Dhabi" to "أبوظبي", "Dubai" to "دبي", "Sharjah" to "الشارقة",
    "Ajman" to "عجمان", "Umm Al Quwain" to "أم القيوين", "Ras Al Khaimah" to "رأس الخيمة", "Fujairah" to "الفجيرة",
    "All types" to "كل الأنواع", "Bank impersonation" to "انتحال بنك", "Delivery / customs" to "توصيل / جمارك",
    "Fake shopping" to "تسوق وهمي", "Investment / crypto" to "استثمار / عملات رقمية", "Job / task scam" to "وظيفة / مهام وهمية",
    "Government impersonation" to "انتحال جهة حكومية", "Account takeover" to "الاستيلاء على حساب",
    "Romance / extortion" to "علاقة / ابتزاز", "Other" to "أخرى",
)

fun localizedChoice(value: String, arabic: Boolean): String = if (arabic) arabicChoices[value] ?: value else value

val officialChannels = listOf(
    OfficialChannel(
        "UAE Government cybercrime guide", "دليل حكومة الإمارات للجرائم الإلكترونية", setOf("All UAE"),
        "Start here to compare official reporting routes across the UAE.", "ابدأ هنا لمقارنة قنوات الإبلاغ الرسمية في دولة الإمارات.",
        "https://u.ae/en/information-and-services/justice-safety-and-the-law/cyber-safety-and-digital-security",
    ),
    OfficialChannel(
        "Dubai Police eCrime", "الجرائم الإلكترونية – شرطة دبي", setOf("Dubai"),
        "Submit a cybercrime complaint for an incident connected with Dubai. Keep screenshots, receipts and transaction references ready.",
        "قدّم شكوى عن جريمة إلكترونية مرتبطة بدبي. جهّز لقطات الشاشة والإيصالات وأرقام المعاملات.",
        "https://ecrime.dubaipolice.gov.ae/", "901 (non-emergency)",
    ),
    OfficialChannel(
        "Abu Dhabi Police Aman", "خدمة أمان – شرطة أبوظبي", setOf("Abu Dhabi"),
        "Share suspicious information confidentially. To open a formal police report, use the MOI UAE app.",
        "شارك المعلومات المشبوهة بسرية. لفتح بلاغ رسمي استخدم تطبيق وزارة الداخلية.",
        "https://srv.adpolice.gov.ae/en/aman/Pages/default.aspx", "800 2626 / SMS 2828",
    ),
    OfficialChannel(
        "Ministry of Interior UAE", "وزارة الداخلية الإماراتية", setOf("All UAE", "Sharjah", "Ajman", "Umm Al Quwain", "Ras Al Khaimah", "Fujairah"),
        "Use MOI digital services or the MOI UAE app for police and cybercrime reporting outside Dubai.",
        "استخدم خدمات وزارة الداخلية الرقمية أو تطبيق MOI UAE للبلاغات الشرطية والإلكترونية خارج دبي.",
        "https://moi.gov.ae/",
    ),
    OfficialChannel(
        "Your bank's official fraud team", "فريق الاحتيال الرسمي في بنكك", setOf("All UAE"),
        "If money or card details were shared, freeze the card and call the number printed on the card immediately.",
        "إذا شاركت مالاً أو بيانات بطاقة، أوقف البطاقة واتصل فوراً بالرقم المطبوع عليها.",
        "https://centralbank.ae/en/consumer/",
    ),
)

val communitySeed = listOf(
    CommunityPost("seed-1", "Dubai", "Delivery / customs", "Small delivery fee requested through a shortened link. No parcel had been ordered.", "verified", "18 Sep 2026"),
    CommunityPost("seed-2", "Abu Dhabi", "Job / task scam", "Unexpected messaging-app job offered commission for liking products, then requested a recharge payment.", "verified", "16 Sep 2026"),
    CommunityPost("seed-3", "All UAE", "Bank impersonation", "Caller claimed an account would be blocked and requested an OTP. The bank confirmed it never asks for OTPs.", "verified", "14 Sep 2026"),
)

fun redactCommunityText(value: String): String {
    var output = value
    output = output.replace(Regex("\\b(?:\\+?971|0)?5\\d(?:[ -]?\\d){7}\\b"), "[phone removed]")
    output = output.replace(Regex("\\b\\d{4}[ -]?\\d{4}[ -]?\\d{4}[ -]?\\d{4}\\b"), "[card removed]")
    output = output.replace(Regex("\\b784[- ]?\\d{4}[- ]?\\d{7}[- ]?\\d\\b"), "[ID removed]")
    output = output.replace(Regex("https?://\\S+", RegexOption.IGNORE_CASE), "[link removed]")
    output = output.replace(Regex("\\b[A-Z]{2}\\d{2}[A-Z0-9]{11,30}\\b", RegexOption.IGNORE_CASE), "[account removed]")
    return output.trim().take(600)
}

fun checkCommunitySafety(value: String): CommunitySafetyResult {
    val en = mutableListOf<String>()
    val ar = mutableListOf<String>()
    if (Regex("https?://|www\\.", RegexOption.IGNORE_CASE).containsMatchIn(value)) {
        en += "Links are not accepted"; ar += "الروابط غير مقبولة"
    }
    if (Regex("\\b(?:\\+?971|0)?5\\d(?:[ -]?\\d){7}\\b|\\b\\d{7,16}\\b").containsMatchIn(value)) {
        en += "Phone or account numbers are not accepted"; ar += "أرقام الهاتف أو الحساب غير مقبولة"
    }
    if (Regex("\\b(idiot|stupid|bastard|kill|attack|hate)\\b", RegexOption.IGNORE_CASE).containsMatchIn(value) ||
        Regex("(غبي|أحمق|سأقتلك|اقتله|أكره)").containsMatchIn(value)) {
        en += "Aggressive or abusive language needs moderator review"; ar += "اللغة العدوانية أو المسيئة تحتاج مراجعة"
    }
    if (Regex("\\b(Mr|Mrs|Ms|Dr)\\.?\\s+[A-Z][a-z]{2,}(?:\\s+[A-Z][a-z]{2,})?\\b").containsMatchIn(value) ||
        Regex("\\b[A-Z][a-z]{2,}\\s+[A-Z][a-z]{2,}\\s+(is|was)\\s+(a\\s+)?scammer\\b").containsMatchIn(value)) {
        en += "A person may be identified or accused by name"; ar += "قد يكون هناك تعريف بشخص أو اتهامه بالاسم"
    }
    return CommunitySafetyResult(en.isEmpty(), en, ar)
}
