CREATE TABLE IF NOT EXISTS community_reports (
    id TEXT PRIMARY KEY,
    emirate TEXT NOT NULL,
    scam_type TEXT NOT NULL,
    notes_redacted TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'approved', 'rejected')),
    ai_status TEXT NOT NULL DEFAULT 'unavailable' CHECK (ai_status IN ('green', 'flagged', 'unavailable')),
    moderation_reasons TEXT NOT NULL DEFAULT '[]',
    moderation_categories TEXT NOT NULL DEFAULT '[]',
    moderator_note TEXT,
    created_at TEXT NOT NULL,
    moderated_at TEXT
);

CREATE INDEX IF NOT EXISTS idx_reports_status_created ON community_reports(status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_reports_ai_status ON community_reports(ai_status, created_at DESC);

CREATE TABLE IF NOT EXISTS moderation_events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    report_id TEXT NOT NULL,
    action TEXT NOT NULL CHECK (action IN ('approved', 'rejected')),
    note TEXT,
    created_at TEXT NOT NULL,
    FOREIGN KEY (report_id) REFERENCES community_reports(id)
);
