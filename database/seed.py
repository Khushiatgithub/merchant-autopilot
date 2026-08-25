"""Generate deterministic PostgreSQL seed SQL for the demo merchant."""

import random
from datetime import datetime, timedelta, timezone
from pathlib import Path

random.seed(42)
ROOT = Path(__file__).resolve().parent
OUT = ROOT / "seed.sql"
MERCHANT_ID = "00000000-0000-0000-0000-000000000001"
BASE_DATE = datetime(2026, 8, 24, tzinfo=timezone.utc)
segments = ["Champions", "At risk", "New", "Loyal", "Dormant"]
categories = ["Footwear", "Activewear", "Hydration", "Accessories"]
methods = ["UPI", "CARD", "NETBANKING"]


def sql(value: str) -> str:
    return "'" + value.replace("'", "''") + "'"


def timestamp(value: datetime) -> str:
    return sql(value.isoformat())


with OUT.open("w", encoding="utf-8") as out:
    out.write("BEGIN;\n")
    out.write("TRUNCATE payment_links, campaign_targets, audit_logs, campaigns, orders, products, customers, merchants CASCADE;\n")
    out.write(
        "INSERT INTO merchants (id, name, email, password_hash) VALUES "
        f"('{MERCHANT_ID}', 'Northstar Goods', 'neha@northstar.example', "
        "'$2a$10$7EqJtq98hPqEX7fNZaFWoO4s9R3G3X4wBq6oXG1Pq5s6fZf2XvYqK');\n"
    )

    product_ids = []
    for index in range(1, 101):
        product_id = f"10000000-0000-0000-0000-{index:012d}"
        product_ids.append(product_id)
        category = categories[(index - 1) % len(categories)]
        price = random.randint(900, 9000)
        margin = random.randint(28, 68)
        stock = random.randint(20, 500)
        out.write(
            "INSERT INTO products (id, merchant_id, name, category, price, margin_percent, stock_count) "
            f"VALUES ('{product_id}', '{MERCHANT_ID}', 'Product {index:03d}', {sql(category)}, "
            f"{price:.2f}, {margin:.2f}, {stock});\n"
        )

    customer_ids = []
    for index in range(1, 501):
        customer_id = f"20000000-0000-0000-0000-{index:012d}"
        customer_ids.append(customer_id)
        segment = random.choice(segments)
        category = random.choice(categories)
        average_value = random.uniform(900, 7000)
        frequency = random.uniform(0.3, 8)
        last_purchase = BASE_DATE - timedelta(days=random.randint(1, 180))
        out.write(
            "INSERT INTO customers (id, merchant_id, name, email, segment, avg_order_value, "
            "favorite_category, purchase_frequency, last_purchase, preferred_hour) VALUES "
            f"('{customer_id}', '{MERCHANT_ID}', 'Customer {index:04d}', 'customer{index:04d}@example.com', "
            f"{sql(segment)}, {average_value:.2f}, {sql(category)}, {frequency:.2f}, "
            f"{timestamp(last_purchase)}, {random.randint(8, 22)});\n"
        )

    statuses = ["PAID", "FAILED", "REFUNDED", "PENDING"]
    weights = [82, 10, 4, 4]
    for index in range(1, 2001):
        order_id = f"30000000-0000-0000-0000-{index:012d}"
        customer_id = random.choice(customer_ids)
        product_id = random.choice(product_ids)
        product_price = random.randint(900, 9000)
        created_at = BASE_DATE - timedelta(days=random.randint(1, 365), hours=random.randint(0, 23))
        out.write(
            "INSERT INTO orders (id, merchant_id, customer_id, product_id, amount, status, "
            "payment_method, created_at) VALUES "
            f"('{order_id}', '{MERCHANT_ID}', '{customer_id}', '{product_id}', {product_price:.2f}, "
            f"{sql(random.choices(statuses, weights)[0])}, {sql(random.choice(methods))}, "
            f"{timestamp(created_at)});\n"
        )
    out.write("COMMIT;\n")

print(f"Generated {len(customer_ids)} customers, {len(product_ids)} products, and 2000 transactions in {OUT}")
