PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS accounts (
                                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                                        account_key TEXT NOT NULL UNIQUE,
                                        email TEXT,
                                        service_status TEXT NOT NULL,
                                        created_at TEXT NOT NULL,
                                        updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS social_accounts (
                                               id INTEGER PRIMARY KEY AUTOINCREMENT,
                                               account_id INTEGER NOT NULL,
                                               provider TEXT NOT NULL,
                                               status TEXT NOT NULL,
                                               linked_at TEXT NOT NULL,
                                               updated_at TEXT NOT NULL,
                                               UNIQUE(account_id, provider),
    FOREIGN KEY(account_id) REFERENCES accounts(id)
    );

CREATE TABLE IF NOT EXISTS inbox_events (
                                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                                            event_id TEXT NOT NULL UNIQUE,
                                            event_type TEXT NOT NULL,
                                            account_key TEXT NOT NULL,
                                            payload TEXT NOT NULL,
                                            status TEXT NOT NULL,
                                            error_message TEXT,
                                            received_at TEXT NOT NULL,
                                            processed_at TEXT
);