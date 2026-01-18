-- Mock Server Schema (SQLite)

-- Auditing table
CREATE TABLE IF NOT EXISTS transition_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    uetr TEXT NOT NULL,
    from_state TEXT NOT NULL,
    to_state TEXT NOT NULL,
    timestamp TEXT NOT NULL,
    api_data TEXT -- JSON representation of the transaction state
);

-- Tracks which file index (.1, .2, etc) should be served next for a UETR
CREATE TABLE IF NOT EXISTS transaction_pointer (
    uetr TEXT PRIMARY KEY,
    next_index INTEGER DEFAULT 1
);
