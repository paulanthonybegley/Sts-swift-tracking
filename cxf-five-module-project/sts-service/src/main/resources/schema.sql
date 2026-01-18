-- Client Service Schema (SQLite)

CREATE TABLE IF NOT EXISTS tracking (
    ID INTEGER PRIMARY KEY, -- Manual ID management: MAX(ID) + 1
    PAYMENT_ID TEXT NOT NULL, -- UETR
    UPDATE_DATE TEXT NOT NULL,
    MODIFIED_DATE TEXT NOT NULL,
    STATUS INTEGER NOT NULL, -- 2=Active (Last Hop), 3=Completed, 4=Rejected
    TO_NODE TEXT NOT NULL -- 'TO' column renamed to TO_NODE to avoid SQL keyword conflicts
);
