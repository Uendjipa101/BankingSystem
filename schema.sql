CREATE TABLE IF NOT EXISTS users (
                                     user_id             VARCHAR(20)  PRIMARY KEY,
    username            VARCHAR(50)  NOT NULL UNIQUE,
    password_hash       VARCHAR(64)  NOT NULL,
    user_role           VARCHAR(15)  NOT NULL CHECK (user_role IN ('CUSTOMER','TELLER','ADMINISTRATOR')),
    customer_kind       VARCHAR(10)  CHECK (customer_kind IN ('INDIVIDUAL','COMPANY')),
    active              BOOLEAN      NOT NULL DEFAULT TRUE,
    first_name          VARCHAR(50)  NOT NULL,
    surname             VARCHAR(50)  NOT NULL,
    address             VARCHAR(150) NOT NULL,
    phone_number        VARCHAR(20)  NOT NULL,
    id_number           VARCHAR(20),
    company_name        VARCHAR(100),
    registration_number VARCHAR(30),
    staff_number        VARCHAR(20)
    );

CREATE TABLE IF NOT EXISTS accounts (
                                        account_number      VARCHAR(20)   PRIMARY KEY,
    customer_id         VARCHAR(20)   NOT NULL,
    account_type        VARCHAR(12)   NOT NULL CHECK (account_type IN ('SAVINGS','INVESTMENT','CHEQUE')),
    branch              VARCHAR(50)   NOT NULL,
    date_opened         DATE          NOT NULL,
    balance             DECIMAL(15,2) NOT NULL DEFAULT 0 CHECK (balance >= 0),
    last_interest_month VARCHAR(7),
    FOREIGN KEY (customer_id) REFERENCES users(user_id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS employment_details (
                                                  account_number  VARCHAR(20)  PRIMARY KEY,
    company_name    VARCHAR(100) NOT NULL,
    company_address VARCHAR(150) NOT NULL,
    FOREIGN KEY (account_number) REFERENCES accounts(account_number) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS transactions (
                                            transaction_id  VARCHAR(30)   PRIMARY KEY,
    account_number  VARCHAR(20)   NOT NULL,
    txn_type        VARCHAR(12)   NOT NULL CHECK (txn_type IN ('DEPOSIT','WITHDRAWAL','INTEREST')),
    amount          DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    txn_time        TIMESTAMP     NOT NULL,
    balance_after   DECIMAL(15,2) NOT NULL,
    description     VARCHAR(150),
    FOREIGN KEY (account_number) REFERENCES accounts(account_number) ON DELETE CASCADE
    );