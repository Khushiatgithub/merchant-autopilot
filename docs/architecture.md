# Architecture Notes

Scout reads customer, product, and order features and returns opportunity candidates. Strategist scores candidates against revenue, discount, and margin constraints. The Spring Boot Executor persists the campaign as `PENDING_APPROVAL`; approval is a separate endpoint and only then may Razorpay Test Mode create a link. Webhooks verify signatures and update orders. Every state transition writes `audit_logs`.
