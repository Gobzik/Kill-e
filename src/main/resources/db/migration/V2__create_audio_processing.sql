CREATE TABLE IF NOT EXISTS audio_processing (
    id UUID PRIMARY KEY,
    audio_s3_key VARCHAR(500) NOT NULL,
    timings_s3_key VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    duration_ms BIGINT,
    word_count INTEGER,
    error_message VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_audio_processing_status ON audio_processing(status);