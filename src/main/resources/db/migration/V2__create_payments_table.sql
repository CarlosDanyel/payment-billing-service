CREATE TABLE IF NOT EXISTS payments
(
    id                VARCHAR(36)    NOT NULL,
    service_order_id  VARCHAR(36)    NOT NULL,
    external_id       VARCHAR(255),
    amount            DECIMAL(10, 2) NOT NULL,
    status            VARCHAR(50)    NOT NULL,
    payment_method    VARCHAR(50),
    qr_code           TEXT,
    qr_code_base64    TEXT,
    ticket_url        TEXT,
    created_at        DATETIME(6)    NOT NULL,
    updated_at        DATETIME(6)    NOT NULL,
    CONSTRAINT pk_payments PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_payment_service_order ON payments (service_order_id);
CREATE INDEX idx_payment_external_id   ON payments (external_id);
CREATE INDEX idx_payment_status        ON payments (status);
