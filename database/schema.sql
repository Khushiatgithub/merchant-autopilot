CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE merchants (
	id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
	name VARCHAR(120) NOT NULL,
	email VARCHAR(180) NOT NULL UNIQUE,
	password_hash VARCHAR(255) NOT NULL,
	created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE customers (
	id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
	merchant_id UUID NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
	name VARCHAR(120) NOT NULL,
	email VARCHAR(180) NOT NULL,
	segment VARCHAR(40) NOT NULL,
	avg_order_value NUMERIC(12, 2) NOT NULL CHECK (avg_order_value >= 0),
	favorite_category VARCHAR(80) NOT NULL,
	purchase_frequency NUMERIC(8, 2) NOT NULL CHECK (purchase_frequency >= 0),
	last_purchase TIMESTAMPTZ,
	preferred_hour SMALLINT NOT NULL CHECK (preferred_hour BETWEEN 0 AND 23),
	UNIQUE (merchant_id, email)
);

CREATE TABLE products (
	id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
	merchant_id UUID NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
	name VARCHAR(160) NOT NULL,
	category VARCHAR(80) NOT NULL,
	price NUMERIC(12, 2) NOT NULL CHECK (price > 0),
	margin_percent NUMERIC(5, 2) NOT NULL CHECK (margin_percent BETWEEN 0 AND 100),
	stock_count INT NOT NULL DEFAULT 0 CHECK (stock_count >= 0)
);

CREATE TABLE orders (
	id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
	merchant_id UUID NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
	customer_id UUID NOT NULL REFERENCES customers(id),
	product_id UUID NOT NULL REFERENCES products(id),
	amount NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
	status VARCHAR(30) NOT NULL CHECK (status IN ('PAID', 'FAILED', 'REFUNDED', 'PENDING')),
	payment_method VARCHAR(40) NOT NULL,
	created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE campaigns (
	id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
	merchant_id UUID NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
	name VARCHAR(160) NOT NULL,
	type VARCHAR(40) NOT NULL,
	expected_revenue NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (expected_revenue >= 0),
	actual_revenue NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (actual_revenue >= 0),
	discount_percent NUMERIC(5, 2) NOT NULL DEFAULT 0 CHECK (discount_percent BETWEEN 0 AND 100),
	status VARCHAR(30) NOT NULL DEFAULT 'PENDING_APPROVAL',
	rationale TEXT,
	confidence NUMERIC(5, 2) CHECK (confidence BETWEEN 0 AND 100),
	created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE campaign_targets (
	campaign_id UUID NOT NULL REFERENCES campaigns(id) ON DELETE CASCADE,
	customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
	PRIMARY KEY (campaign_id, customer_id)
);

CREATE TABLE payment_links (
	id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
	campaign_id UUID REFERENCES campaigns(id) ON DELETE SET NULL,
	order_id UUID REFERENCES orders(id) ON DELETE SET NULL,
	razorpay_link_id VARCHAR(120) UNIQUE,
	short_url TEXT,
	status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
	retry_count INT NOT NULL DEFAULT 0 CHECK (retry_count BETWEEN 0 AND 2),
	verified_at TIMESTAMPTZ
);

CREATE TABLE audit_logs (
	id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
	merchant_id UUID NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
	actor VARCHAR(80) NOT NULL,
	action VARCHAR(120) NOT NULL,
	reason TEXT,
	confidence NUMERIC(5, 2) CHECK (confidence BETWEEN 0 AND 100),
	approval VARCHAR(30),
	result TEXT,
	created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_customers_merchant_segment ON customers(merchant_id, segment);
CREATE INDEX idx_customers_last_purchase ON customers(last_purchase);
CREATE INDEX idx_products_merchant_category ON products(merchant_id, category);
CREATE INDEX idx_orders_merchant_created ON orders(merchant_id, created_at);
CREATE INDEX idx_orders_customer_created ON orders(customer_id, created_at);
CREATE INDEX idx_orders_product ON orders(product_id);
CREATE INDEX idx_campaigns_merchant_status ON campaigns(merchant_id, status);
CREATE INDEX idx_payment_links_status ON payment_links(status);
CREATE INDEX idx_audit_merchant_created ON audit_logs(merchant_id, created_at);
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE TABLE merchants (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), name VARCHAR(120) NOT NULL, email VARCHAR(180) UNIQUE NOT NULL, created_at TIMESTAMPTZ DEFAULT now());
CREATE TABLE customers (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), merchant_id UUID REFERENCES merchants(id) ON DELETE CASCADE, name VARCHAR(120) NOT NULL, email VARCHAR(180) NOT NULL, segment VARCHAR(40), avg_order_value NUMERIC(12,2), favorite_category VARCHAR(80), purchase_frequency NUMERIC(8,2), last_purchase TIMESTAMPTZ, preferred_hour SMALLINT, UNIQUE(merchant_id,email));
CREATE TABLE products (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), merchant_id UUID REFERENCES merchants(id) ON DELETE CASCADE, name VARCHAR(160) NOT NULL, category VARCHAR(80), price NUMERIC(12,2) NOT NULL, margin_percent NUMERIC(5,2) NOT NULL, stock_count INT DEFAULT 0);
CREATE TABLE orders (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), merchant_id UUID REFERENCES merchants(id) ON DELETE CASCADE, customer_id UUID REFERENCES customers(id), product_id UUID REFERENCES products(id), product VARCHAR(80), amount NUMERIC(12,2) NOT NULL, status VARCHAR(30) NOT NULL, payment_method VARCHAR(40), created_at TIMESTAMPTZ DEFAULT now());
CREATE TABLE campaigns (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), merchant_id UUID REFERENCES merchants(id) ON DELETE CASCADE, name VARCHAR(160) NOT NULL, type VARCHAR(40) NOT NULL, expected_revenue NUMERIC(12,2), actual_revenue NUMERIC(12,2) DEFAULT 0, discount_percent NUMERIC(5,2), status VARCHAR(30) DEFAULT 'PENDING_APPROVAL', rationale TEXT, confidence NUMERIC(5,2), created_at TIMESTAMPTZ DEFAULT now());
CREATE TABLE campaign_targets (campaign_id UUID REFERENCES campaigns(id) ON DELETE CASCADE, customer_id UUID REFERENCES customers(id) ON DELETE CASCADE, PRIMARY KEY(campaign_id, customer_id));
CREATE TABLE payment_links (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), campaign_id UUID REFERENCES campaigns(id), order_id UUID REFERENCES orders(id), razorpay_link_id VARCHAR(120), short_url TEXT, status VARCHAR(30) DEFAULT 'CREATED', retry_count INT DEFAULT 0, verified_at TIMESTAMPTZ);
CREATE TABLE audit_logs (id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), merchant_id UUID REFERENCES merchants(id), actor VARCHAR(80) NOT NULL, action VARCHAR(120) NOT NULL, reason TEXT, confidence NUMERIC(5,2), approval VARCHAR(30), result TEXT, created_at TIMESTAMPTZ DEFAULT now());
CREATE INDEX idx_customers_segment ON customers(segment); CREATE INDEX idx_orders_created ON orders(created_at); CREATE INDEX idx_campaigns_status ON campaigns(status); CREATE INDEX idx_audit_created ON audit_logs(created_at);
