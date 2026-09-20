ALTER TABLE community_reports ADD COLUMN ai_status TEXT NOT NULL DEFAULT 'unavailable';
ALTER TABLE community_reports ADD COLUMN moderation_reasons TEXT NOT NULL DEFAULT '[]';
ALTER TABLE community_reports ADD COLUMN moderation_categories TEXT NOT NULL DEFAULT '[]';
ALTER TABLE community_reports ADD COLUMN moderator_note TEXT;
ALTER TABLE moderation_events ADD COLUMN note TEXT;
CREATE INDEX IF NOT EXISTS idx_reports_ai_status ON community_reports(ai_status, created_at DESC);
