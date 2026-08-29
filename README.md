# Merchant Autopilot
### AI Revenue Manager for Razorpay Merchants

Merchant Autopilot turns a revenue goal into explainable, approval-gated growth campaigns. It uses Scout to find opportunities, Strategist to plan offers, and Executor to create Razorpay Test Mode links only after merchant approval. It never charges automatically.

<img width="1897" height="882" alt="Screenshot 2026-08-25 182051" src="https://github.com/user-attachments/assets/3983a490-c13a-4323-9eda-6999f5493205" />

<img width="1591" height="778" alt="Screenshot 2026-08-29 193616" src="https://github.com/user-attachments/assets/b39dd1a8-4026-4d35-baab-16ed139bdae3" />
<img width="1917" height="873" alt="Screenshot 2026-08-29 193601" src="https://github.com/user-attachments/assets/e1f0c030-4e63-46ad-94dd-7b73e5f5b074" />
<img width="1917" height="902" alt="Screenshot 2026-08-29 193627" src="https://github.com/user-attachments/assets/8f70f5c0-8e83-446f-acff-8bec0ffe90df" />
<img width="1917" height="896" alt="Screenshot 2026-08-29 193642" src="https://github.com/user-attachments/assets/96be3c61-7fb8-45ca-974a-b7aa11947c23" />
<img width="1512" height="531" alt="Screenshot 2026-08-29 193654" src="https://github.com/user-attachments/assets/6cdb45ba-b1b8-4f33-a922-c17de362401c" />
<img width="1917" height="807" alt="Screenshot 2026-08-29 193703" src="https://github.com/user-attachments/assets/74170947-4af2-42f3-b742-672e25d80e80" />
<img width="1917" height="887" alt="Screenshot 2026-08-29 193715" src="https://github.com/user-attachments/assets/0ee68813-bcd8-45d8-a695-b05594026044" />
<img width="1917" height="866" alt="Screenshot 2026-08-29 193727" src="https://github.com/user-attachments/assets/2c19519f-35c7-4719-a4e9-5644535a626e" />
<img width="1917" height="890" alt="Screenshot 2026-08-29 193738" src="https://github.com/user-attachments/assets/254ef817-dc67-499a-a7f0-276aa0fa4bcf" />



## Architecture

```mermaid
flowchart LR
  UI[React dashboard] --> API[Spring Boot API]
  API --> DB[(PostgreSQL)]
  API --> AI[FastAPI AI engine]
  API --> RZ[Razorpay Test Mode]
  AI --> S[Scout]
  AI --> T[Strategist]
  API --> E[Executor + audit log]
```

## Run locally

1. Create PostgreSQL database and run `database/schema.sql`, then generate and run the seed data with `python database/seed.py` followed by `psql "$DATABASE_URL" -f database/seed.sql`.
2. Start AI: `cd ai-engine && pip install -r requirements.txt && uvicorn main:app --reload --port 8000`.
3. Start API: `cd backend && mvn spring-boot:run`.
4. Start UI: `cd frontend && npm install && npm run dev`.

The UI includes a realistic demo mode and works without credentials. Add credentials to connect real services.

The generated seed contains exactly 500 customers, 100 products, and 2,000 transactions. JPA entities are under `backend/src/main/java/com/merchantautopilot/domain` and mirror every schema table, including the composite `campaign_targets` key.

After updating an existing database, run `psql -U postgres -d merchant_autopilot -f database/migration-001-auth.sql` once before restarting the backend. Fresh databases should run `schema.sql` followed by `seed.sql`.

Backend authentication endpoints are `POST /api/auth/register` and `POST /api/auth/login`. Send the returned JWT as `Authorization: Bearer <token>` to merchant endpoints. Razorpay links are created only for approved campaigns and use Test Mode credentials from `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET`.

## Environment

`DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD`, `AI_ENGINE_URL`, `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`, and `OPENAI_API_KEY`.

## API surface

`POST /api/goals`, `GET /api/plan`, `POST /api/campaigns`, `POST /api/campaigns/{id}/approve`, `POST /api/payments/create-link`, `POST /api/payments/verify`, `GET /api/customers`, `GET /api/dashboard`, `GET /api/audit`.

AI endpoints: `/analyze`, `/plan`, `/recommend`, `/predict-discount`, `/digital-twin`, `/explain`.

## Demo script

Set a goal of INR 2,00,000 with max discount 15%, review confidence and margin impact, approve one strategy, open the generated Test Mode payment link, then inspect attribution and the audit timeline. Expired links retry once and stop after the second failure.

## Screenshots

Add `docs/screenshots/dashboard.png`, `strategy.png`, and `audit.png` for submission packaging.
