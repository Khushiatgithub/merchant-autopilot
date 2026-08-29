CREATE TABLE IF NOT EXISTS merchants (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS customers (
    id UUID PRIMARY KEY,
    merchant_id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL,
    segment VARCHAR(40) NOT NULL,
    avg_order_value NUMERIC(12,2) NOT NULL,
    favorite_category VARCHAR(80) NOT NULL,
    purchase_frequency NUMERIC(8,2) NOT NULL,
    last_purchase TIMESTAMP WITH TIME ZONE,
    preferred_hour SMALLINT NOT NULL,
    CONSTRAINT fk_customers_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id) ON DELETE CASCADE,
    CONSTRAINT uk_customers_merchant_email UNIQUE (merchant_id, email)
);

CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY,
    merchant_id UUID NOT NULL,
    name VARCHAR(160) NOT NULL,
    category VARCHAR(80) NOT NULL,
    price NUMERIC(12,2) NOT NULL,
    margin_percent NUMERIC(5,2) NOT NULL,
    stock_count INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_products_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    merchant_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    product_id UUID NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    payment_method VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id) ON DELETE CASCADE,
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_orders_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE IF NOT EXISTS campaigns (
    id UUID PRIMARY KEY,
    merchant_id UUID NOT NULL,
    name VARCHAR(160) NOT NULL,
    type VARCHAR(40) NOT NULL,
    expected_revenue NUMERIC(12,2) NOT NULL DEFAULT 0,
    actual_revenue NUMERIC(12,2) NOT NULL DEFAULT 0,
    discount_percent NUMERIC(5,2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_APPROVAL',
    rationale TEXT,
    confidence NUMERIC(5,2),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_campaigns_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS campaign_targets (
    campaign_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    PRIMARY KEY (campaign_id, customer_id),
    CONSTRAINT fk_campaign_targets_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE,
    CONSTRAINT fk_campaign_targets_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS payment_links (
    id UUID PRIMARY KEY,
    campaign_id UUID,
    order_id UUID,
    razorpay_link_id VARCHAR(120) UNIQUE,
    short_url TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    retry_count INT NOT NULL DEFAULT 0,
    verified_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_payment_links_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE SET NULL,
    CONSTRAINT fk_payment_links_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY,
    merchant_id UUID NOT NULL,
    actor VARCHAR(80) NOT NULL,
    action VARCHAR(120) NOT NULL,
    reason TEXT,
    confidence NUMERIC(5,2),
    approval VARCHAR(30),
    result TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_customers_merchant_segment ON customers(merchant_id, segment);
CREATE INDEX IF NOT EXISTS idx_customers_last_purchase ON customers(last_purchase);
CREATE INDEX IF NOT EXISTS idx_products_merchant_category ON products(merchant_id, category);
CREATE INDEX IF NOT EXISTS idx_orders_merchant_created ON orders(merchant_id, created_at);
CREATE INDEX IF NOT EXISTS idx_orders_customer_created ON orders(customer_id, created_at);
CREATE INDEX IF NOT EXISTS idx_orders_product ON orders(product_id);
CREATE INDEX IF NOT EXISTS idx_campaigns_merchant_status ON campaigns(merchant_id, status);
CREATE INDEX IF NOT EXISTS idx_payment_links_status ON payment_links(status);
CREATE INDEX IF NOT EXISTS idx_audit_merchant_created ON audit_logs(merchant_id, created_at);
