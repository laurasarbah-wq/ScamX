package ae.scamx.app

enum class RiskLevel { LOW, SUSPICIOUS, HIGH }

data class RiskReason(
    val points: Int,
    val titleEn: String,
    val titleAr: String,
    val detailEn: String,
    val detailAr: String,
    val matchedText: String? = null,
)

data class RiskResult(
    val score: Int,
    val level: RiskLevel,
    val reasons: List<RiskReason>,
)

object RiskEngine {
    private data class Rule(
        val points: Int,
        val patterns: List<Regex>,
        val titleEn: String,
        val titleAr: String,
        val detailEn: String,
        val detailAr: String,
        val exclusions: List<Regex> = emptyList(),
    )

    private fun pattern(value: String) = Regex(value, setOf(RegexOption.IGNORE_CASE))

    private val rules = listOf(
        Rule(
            4,
            listOf(pattern("\\b(otp|one[ -]?time password|password|passcode|pin|cvv|card (number|details)|security code)\\b"), pattern("(رمز التحقق|كلمة المرور|الرقم السري|بيانات البطاقة|رقم البطاقة|رمز الأمان)")),
            "Requests sensitive information", "يطلب معلومات حساسة",
            "Mentions an OTP, password, PIN, or card details.", "يذكر رمز تحقق أو كلمة مرور أو رقماً سرياً أو بيانات بطاقة.",
            listOf(pattern("\\b(never|do not|don't)\\s+(share|send|give).{0,24}\\b(otp|password|pin|cvv)\\b"), pattern("(لا تشارك|لا ترسل|لا تعط).{0,24}(رمز التحقق|كلمة المرور|الرقم السري)")),
        ),
        Rule(
            3,
            listOf(pattern("\\b(bank|police|uae[ -]?pass|central bank|ministry of interior|emirates nbd|adcb|first abu dhabi bank|fab|mashreq|rta|emirates post|aramex|dubai police)\\b"), pattern("(بنك|المصرف|الشرطة|شرطة دبي|الهوية الرقمية|وزارة الداخلية|بريد الإمارات|هيئة الطرق|أرامكس)")),
            "Claims official authority", "يدّعي صفة رسمية",
            "Claims to represent a bank, police, or UAE Pass.", "يدّعي تمثيل بنك أو الشرطة أو الهوية الرقمية.",
        ),
        Rule(
            3,
            listOf(pattern("\\b(account (will be )?(closed|blocked|suspended)|arrest(ed)?|fine|legal action|warrant)\\b"), pattern("(إغلاق الحساب|حظر الحساب|تعليق الحساب|القبض|اعتقال|غرامة|إجراء قانوني)")),
            "Uses threats or fear", "يستخدم التهديد أو التخويف",
            "Threatens account closure, arrest, a fine, or legal action.", "يهدد بإغلاق حساب أو القبض أو غرامة أو إجراء قانوني.",
        ),
        Rule(
            2,
            listOf(pattern("\\b(urgent|immediately|right now|act now|within (an )?hour|last chance|final warning|today only)\\b"), pattern("(عاجل|فوراً|فورا|حالاً|حالا|الآن|تحرك الآن|فرصتك الأخيرة|التحذير الأخير|خلال ساعة)")),
            "Creates urgency", "يخلق شعوراً بالعجلة",
            "Pushes you to act before you have time to verify.", "يدفعك للتصرف قبل أن تتاح لك فرصة التحقق.",
        ),
        Rule(
            2,
            listOf(pattern("\\b(prize|winner|won|guaranteed (return|profit)|investment profit|unexpected refund|cash reward|lottery)\\b"), pattern("(جائزة|فائز|ربحت|ربح مضمون|عائد مضمون|استرداد غير متوقع|مكافأة نقدية|يانصيب)")),
            "Unexpected money or reward", "مال أو مكافأة غير متوقعة",
            "Promises a prize, guaranteed profit, or unexpected refund.", "يَعِد بجائزة أو ربح مضمون أو استرداد غير متوقع.",
        ),
        Rule(
            3,
            listOf(pattern("https?://(bit\\.ly|tinyurl\\.com|t\\.co|is\\.gd|cutt\\.ly|rb\\.gy|shorturl\\.at|tiny\\.one|rebrand\\.ly|dub\\.sh|short\\.io)"), pattern("https?://[^\\s/]*?(secure|verify|update|login|support|account|uaepass|bank)[^\\s/]*\\.(top|xyz|click|info|live|site|online|vip|shop)(/|\\s|$)")),
            "Suspicious or shortened link", "رابط مختصر أو مريب",
            "The link hides its destination or uses a commonly abused domain pattern.", "الرابط يخفي وجهته أو يستخدم نمط نطاق يُساء استخدامه كثيراً.",
        ),
        Rule(
            3,
            listOf(pattern("\\b(gift card|crypto(currency)?|bitcoin|usdt|wire transfer|western union|moneygram|cash deposit|recharge|top[ -]?up|prepaid voucher)\\b"), pattern("(بطاقة هدايا|عملة رقمية|بيتكوين|تحويل بنكي|ويسترن يونيون|إيداع نقدي|إعادة شحن|اشحن الرصيد|قسيمة مسبقة الدفع)")),
            "Unusual payment method", "طريقة دفع غير معتادة",
            "Requests payment by a method that can be hard to reverse.", "يطلب الدفع بطريقة قد يصعب استرجاعها.",
        ),
        Rule(
            1,
            listOf(pattern("\\b(dear customer|kindly do the needful|your a/c|bank department team|uae pass team)\\b"), pattern("(عميلنا العزيز.*عميل عزيزي|فريق قسم البنك|يرجى التكرم بسرعة)")),
            "Inconsistent wording", "صياغة غير متسقة",
            "Uses awkward wording or an inconsistent organisation name.", "يستخدم صياغة غريبة أو اسماً غير متسق للجهة.",
        ),
        Rule(
            3,
            listOf(pattern("\\b(remote access|screen share|anydesk|teamviewer|quicksupport|install (this|the) app|download (this|the) app)\\b"), pattern("(تحكم عن بعد|مشاركة الشاشة|اني ديسك|تيم فيور|نزّل التطبيق|حمل التطبيق)")),
            "Requests remote access", "يطلب الوصول عن بُعد",
            "Asks you to install remote-control software or share your screen.", "يطلب تثبيت برنامج للتحكم عن بُعد أو مشاركة شاشتك.",
        ),
        Rule(
            3,
            listOf(pattern("\\b(part[ -]?time task|simple tasks?|rate products?|like videos?|merchant task|recharge task|commission task|work from home.*(earn|aed)|telegram job)\\b"), pattern("(مهام بسيطة|قيّم المنتجات|اعجاب بالفيديو|عمولة يومية|وظيفة تيليجرام|اشحن رصيد المهمة|العمل من المنزل.*درهم)")),
            "Task or fake-job pattern", "نمط وظيفة أو مهام وهمية",
            "Matches common task-job scams that promise commission for simple online actions.", "يتطابق مع احتيال المهام الذي يَعِد بعمولة مقابل أعمال بسيطة عبر الإنترنت.",
        ),
        Rule(
            3,
            listOf(pattern("\\b(package|parcel|delivery|shipment|customs).{0,45}\\b(fee|aed|payment|pay|held|failed)\\b"), pattern("(طرد|شحنة|توصيل|الجمارك).{0,45}(رسوم|درهم|دفع|معلّقة|فشل)")),
            "Unexpected delivery fee", "رسوم توصيل غير متوقعة",
            "Claims a parcel is held or asks for a small delivery/customs payment.", "يدّعي تعليق شحنة أو يطلب رسوماً صغيرة للتوصيل أو الجمارك.",
        ),
        Rule(
            3,
            listOf(pattern("\\b(transfer|send|deposit|pay).{0,35}\\b(personal account|personal iban|individual account|wallet address)\\b"), pattern("(حوّل|أرسل|ادفع|أودع).{0,35}(حساب شخصي|آيبان شخصي|محفظة رقمية)")),
            "Payment to a personal account", "دفع إلى حساب شخصي",
            "Requests money to a personal account or wallet instead of an official payment channel.", "يطلب المال إلى حساب شخصي أو محفظة بدلاً من قناة دفع رسمية.",
        ),
        Rule(
            3,
            listOf(pattern("\\b(salik|rta|dubai traffic).{0,45}\\b(fine|penalty|overdue|pay)\\b"), pattern("(سالك|هيئة الطرق|مخالفة مرورية).{0,45}(غرامة|متأخرة|ادفع)")),
            "Traffic-fine impersonation", "انتحال مخالفة مرورية",
            "Claims an urgent Salik or traffic fine payment is due.", "يدّعي وجود دفعة عاجلة لمخالفة سالك أو مرورية.",
        ),
        Rule(
            3,
            listOf(pattern("\\b(visa|immigration|residency|golden visa).{0,55}\\b(guaranteed|fee|payment|approve|expired)\\b"), pattern("(تأشيرة|إقامة|الهجرة|الإقامة الذهبية).{0,55}(مضمون|رسوم|دفع|موافقة|منتهية)")),
            "Visa or immigration pressure", "ضغط متعلق بالتأشيرة أو الإقامة",
            "Uses a visa or residency claim to request money or urgent action.", "يستخدم ادعاءً متعلقاً بالتأشيرة أو الإقامة لطلب المال أو إجراء عاجل.",
        ),
        Rule(
            3,
            listOf(pattern("\\b(property|apartment|villa|room).{0,55}\\b(deposit|booking fee|reservation fee).{0,35}\\b(transfer|send|pay)\\b"), pattern("(عقار|شقة|فيلا|غرفة).{0,55}(عربون|تأمين|رسوم حجز).{0,35}(حوّل|أرسل|ادفع)")),
            "Property deposit pressure", "ضغط لدفع عربون عقاري",
            "Requests a property deposit before independent verification.", "يطلب عربوناً عقارياً قبل التحقق المستقل.",
        ),
    )

    fun check(input: String): RiskResult {
        val normalized = input.trim()
        val matched = rules.mapNotNull { rule ->
            val match = rule.patterns.firstNotNullOfOrNull { it.find(normalized) }
            val excluded = rule.exclusions.any { it.containsMatchIn(normalized) }
            if (match != null && !excluded) RiskReason(rule.points, rule.titleEn, rule.titleAr, rule.detailEn, rule.detailAr, match.value.take(90)) else null
        }
        val structural = buildList {
            if (Regex("https?://(\\d{1,3}\\.){3}\\d{1,3}", RegexOption.IGNORE_CASE).containsMatchIn(normalized) ||
                Regex("https?://[^\\s]+@", RegexOption.IGNORE_CASE).containsMatchIn(normalized) ||
                Regex("https?://xn--", RegexOption.IGNORE_CASE).containsMatchIn(normalized)
            ) {
                add(RiskReason(3, "Deceptive link structure", "بنية رابط خادعة", "The link uses a raw IP address, embedded username, or internationalized look-alike domain.", "يستخدم الرابط عنوان IP مباشراً أو اسم مستخدم مضمّناً أو نطاقاً دولياً مشابهاً."))
            }
            if (Regex("http://", RegexOption.IGNORE_CASE).containsMatchIn(normalized)) {
                add(RiskReason(1, "Unencrypted link", "رابط غير مشفّر", "Uses HTTP instead of HTTPS. This alone does not prove fraud.", "يستخدم HTTP بدلاً من HTTPS. هذا وحده لا يثبت الاحتيال."))
            }
            if (Regex("\\b(uae.?pass|dubai.?police|emirates.?nbd|adcb|rta)[-_]?(verify|secure|login|update)?\\.(top|xyz|click|info|live|site|online)", RegexOption.IGNORE_CASE).containsMatchIn(normalized)) {
                add(RiskReason(4, "Possible brand look-alike", "احتمال انتحال علامة تجارية", "The domain combines a trusted UAE name with a high-risk domain ending.", "يجمع النطاق اسماً موثوقاً في الإمارات مع نهاية نطاق عالية الخطورة."))
            }
        }
        val reasons = (matched + structural).distinctBy { it.titleEn }
        val score = reasons.sumOf { it.points }
        val level = when {
            score >= 6 -> RiskLevel.HIGH
            score >= 3 -> RiskLevel.SUSPICIOUS
            else -> RiskLevel.LOW
        }
        return RiskResult(score, level, reasons)
    }
}
