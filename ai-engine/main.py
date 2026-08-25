"""Merchant Autopilot's explainable, approval-aware AI engine.

The engine is intentionally stateless for analysis and planning. ExecutorAgent
keeps only short-lived retry state; campaigns and payments remain persisted by
Spring Boot and PostgreSQL.
"""
from datetime import datetime, timezone
from enum import Enum
from statistics import mean
from typing import Any
from uuid import uuid4

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

app = FastAPI(title="Merchant Autopilot AI Engine", version="1.0.0")


class OpportunityType(str, Enum):
    UPSELL = "UPSELL"
    CROSS_SELL = "CROSS_SELL"
    ABANDONED_CART = "ABANDONED_CART"
    WIN_BACK = "WIN_BACK"


class Goal(BaseModel):
    revenueGoal: float = Field(gt=0)
    maxDiscount: float = Field(ge=0, le=100)
    minMargin: float = Field(ge=0, le=100)


class AnalyzeRequest(BaseModel):
    customers: list[dict[str, Any]] = Field(default_factory=list)
    products: list[dict[str, Any]] = Field(default_factory=list)
    orders: list[dict[str, Any]] = Field(default_factory=list)


class DigitalTwinRequest(BaseModel):
    customer: dict[str, Any]


class StrategyRequest(BaseModel):
    strategy: dict[str, Any]
    goal: Goal | None = None


class ExecuteRequest(BaseModel):
    campaignId: str
    approved: bool
    paymentLinkStatus: str | None = None


class ScoutAgent:
    """Finds opportunities from customer recency, frequency, and order signals."""

    def analyze(self, request: AnalyzeRequest) -> dict[str, Any]:
        opportunities: list[dict[str, Any]] = []
        for customer in request.customers:
            frequency = self._number(customer.get("purchase_frequency"), 0)
            average_value = self._number(customer.get("avg_order_value"), 0)
            segment = str(customer.get("segment", "")).lower()
            if segment in {"champions", "loyal"} or frequency >= 4:
                opportunities.append(self._opportunity(OpportunityType.UPSELL, customer, .94, "Frequent buyers show high intent for a complementary add-on."))
            elif segment in {"at risk", "dormant"}:
                opportunities.append(self._opportunity(OpportunityType.WIN_BACK, customer, .87, "Purchase recency and prior spend indicate recoverable demand."))
            if average_value >= 4000:
                opportunities.append(self._opportunity(OpportunityType.CROSS_SELL, customer, .86, "High basket value supports a relevant cross-sell recommendation."))

        if not opportunities:
            opportunities = [
                {"type": OpportunityType.UPSELL, "label": "Shoes + Socks", "confidence": .82, "customerIds": [], "reason": "Default opportunity while customer signals are unavailable."},
                {"type": OpportunityType.WIN_BACK, "label": "Lapsed customers", "confidence": .78, "customerIds": [], "reason": "Default win-back segment while customer signals are unavailable."},
            ]
        return {"agent": "Scout", "opportunities": opportunities, "analyzedCustomers": len(request.customers), "analyzedProducts": len(request.products), "analyzedOrders": len(request.orders)}

    def _opportunity(self, kind: OpportunityType, customer: dict[str, Any], confidence: float, reason: str) -> dict[str, Any]:
        labels = {OpportunityType.UPSELL: "Shoes + Socks", OpportunityType.CROSS_SELL: "Hydration cross-sell", OpportunityType.WIN_BACK: "Lapsed customers", OpportunityType.ABANDONED_CART: "Abandoned cart recovery"}
        return {"type": kind, "label": labels[kind], "confidence": confidence, "customerIds": [customer.get("id")] if customer.get("id") else [], "reason": reason}

    @staticmethod
    def _number(value: Any, fallback: float) -> float:
        try:
            return float(value)
        except (TypeError, ValueError):
            return fallback


class StrategistAgent:
    """Builds a constrained plan and explains why each strategy was selected."""

    STRATEGIES = [("Upsell Shoes + Socks", "UPSELL", .31, 5, .94, "Affinity and recent purchase intent support an add-on."), ("Cross Sell Bottle", "CROSS_SELL", .155, 5, .86, "High-value customers are likely to add a relevant product."), ("Win Back Campaign", "WIN_BACK", .29, 10, .87, "Lapsed customers have proven historical demand."), ("Bundles", "BUNDLE", .245, 5, .82, "A bundle grows basket size while protecting blended margin.")]

    def plan(self, goal: Goal) -> dict[str, Any]:
        strict_constraints = goal.maxDiscount <= 5 and goal.minMargin >= 35
        weights = (.24, .11, .15, .20) if strict_constraints else (.31, .155, .245, .29)
        order = [(0, "Upsell Shoes + Socks", "UPSELL", 5, .94, "Affinity and recent purchase intent support an add-on."), (1, "Cross Sell Bottle", "CROSS_SELL", 5, .86, "High-value customers are likely to add a relevant product."), (2, "Bundles", "BUNDLE", 5, .82, "A bundle grows basket size while protecting blended margin."), (3, "Win Back Campaign", "WIN_BACK", 10, .87, "Lapsed customers have proven historical demand.")]
        strategies = []
        for index, name, kind, preferred_discount, confidence, reason in order:
            discount = self._safe_discount(preferred_discount, goal.maxDiscount, goal.minMargin)
            strategies.append({"name": name, "type": kind, "expectedRevenue": round(goal.revenueGoal * weights[index]), "discount": discount, "confidence": confidence, "conversion": round(.12 + confidence * .1 + discount * .003, 3), "marginImpact": "Protected" if goal.minMargin <= 38 else "Requires margin review", "reason": reason, "whySelected": "Highest expected revenue under the merchant guardrails."})
        return {"agent": "Strategist", "goal": goal.revenueGoal, "strategies": strategies, "totalExpectedRevenue": sum(item["expectedRevenue"] for item in strategies), "guardrails": {"maxDiscount": goal.maxDiscount, "minMargin": goal.minMargin}}

    def discount(self, goal: Goal) -> dict[str, Any]:
        selected = 0 if goal.minMargin > 38 else min((0, 5, 10, 15), key=lambda offer: abs(offer - min(goal.maxDiscount, 10)))
        selected = min(selected, goal.maxDiscount)
        margin_safe = goal.minMargin <= 38 or selected == 0
        return {"agent": "Strategist", "discount": selected, "conversionLift": round(.08 + selected * .012, 3), "marginSafe": margin_safe, "reason": "Selected from the allowed 0%, 5%, 10%, and 15% offer ladder."}

    @staticmethod
    def _safe_discount(preferred: float, maximum: float, minimum_margin: float) -> float:
        if minimum_margin > 38:
            return 0
        return min(preferred, maximum)


class ExecutorAgent:
    """Executes only approved campaigns and permits exactly one retry."""

    def __init__(self) -> None:
        self.retry_counts: dict[str, int] = {}

    def execute(self, request: ExecuteRequest) -> dict[str, Any]:
        if not request.approved:
            return {"agent": "Executor", "campaignId": request.campaignId, "status": "BLOCKED", "reason": "Merchant approval is required before any money action."}
        status = (request.paymentLinkStatus or "CREATED").upper()
        if status in {"PAID", "VERIFIED"}:
            return {"agent": "Executor", "campaignId": request.campaignId, "status": "COMPLETED", "retryCount": self.retry_counts.get(request.campaignId, 0)}
        if status == "EXPIRED":
            retries = self.retry_counts.get(request.campaignId, 0)
            if retries >= 1:
                return {"agent": "Executor", "campaignId": request.campaignId, "status": "STOPPED", "retryCount": retries, "reason": "Payment link expired twice; execution stopped and must be reviewed."}
            self.retry_counts[request.campaignId] = retries + 1
            return {"agent": "Executor", "campaignId": request.campaignId, "status": "RETRY_REQUIRED", "retryCount": retries + 1, "reason": "Payment link expired; one retry is allowed."}
        return {"agent": "Executor", "campaignId": request.campaignId, "status": "READY", "retryCount": self.retry_counts.get(request.campaignId, 0), "reason": "Campaign is approved and ready for the payment service."}


class DigitalTwinAgent:
    def predict(self, customer: dict[str, Any]) -> dict[str, Any]:
        frequency = self._number(customer.get("purchase_frequency"), 2)
        average_value = self._number(customer.get("avg_order_value"), 3840)
        likelihood = min(.98, max(.05, .45 + frequency * .1))
        return {"agent": "Digital Twin", "customerId": customer.get("id"), "likelihoodToPurchase": round(likelihood, 3), "nextPredictedProduct": customer.get("next_product", "Performance Socks"), "expectedSpend": round(average_value * (1.05 + likelihood * .13)), "preferredHour": customer.get("preferred_hour", 19), "signals": ["purchase_frequency", "average_order_value", "segment"]}

    @staticmethod
    def _number(value: Any, fallback: float) -> float:
        try:
            return float(value)
        except (TypeError, ValueError):
            return fallback


class ExplainabilityAgent:
    def explain(self, strategy: dict[str, Any], goal: Goal | None = None) -> dict[str, Any]:
        confidence = float(strategy.get("confidence", .8))
        discount = float(strategy.get("discount", 0))
        max_discount = goal.maxDiscount if goal else 15
        return {"agent": "Explainability", "reason": strategy.get("reason", "Customer and transaction signals indicate a revenue opportunity."), "confidence": confidence, "expectedRevenue": strategy.get("expectedRevenue", 0), "marginImpact": strategy.get("marginImpact", "Protected by merchant guardrails"), "whySelected": strategy.get("whySelected", "Best expected revenue within constraints."), "guardrailCheck": {"discountWithinLimit": discount <= max_discount, "discount": discount, "maxDiscount": max_discount}, "generatedAt": datetime.now(timezone.utc).isoformat()}


scout = ScoutAgent()
strategist = StrategistAgent()
executor = ExecutorAgent()
digital_twin_agent = DigitalTwinAgent()
explainability = ExplainabilityAgent()


@app.get("/health")
def health() -> dict[str, Any]:
    return {"status": "ok", "agents": ["scout", "strategist", "executor", "digital-twin", "explainability"], "timestamp": datetime.now(timezone.utc).isoformat()}


@app.post("/analyze")
def analyze(request: AnalyzeRequest) -> dict[str, Any]:
    return scout.analyze(request)


@app.post("/plan")
def plan(goal: Goal) -> dict[str, Any]:
    return strategist.plan(goal)


@app.post("/recommend")
def recommend(request: AnalyzeRequest) -> dict[str, Any]:
    scout_result = scout.analyze(request)
    return {**scout_result, "nextAgent": "Strategist", "recommendations": scout_result["opportunities"]}


@app.post("/predict-discount")
def predict_discount(goal: Goal) -> dict[str, Any]:
    return strategist.discount(goal)


@app.post("/digital-twin")
def digital_twin(request: DigitalTwinRequest) -> dict[str, Any]:
    return digital_twin_agent.predict(request.customer)


@app.post("/explain")
def explain(request: StrategyRequest) -> dict[str, Any]:
    return explainability.explain(request.strategy, request.goal)


@app.post("/execute")
def execute(request: ExecuteRequest) -> dict[str, Any]:
    return executor.execute(request)


@app.post("/revenue-plan")
def revenue_plan(goal: Goal, request: AnalyzeRequest | None = None) -> dict[str, Any]:
    analysis = scout.analyze(request or AnalyzeRequest())
    strategy = strategist.plan(goal)
    return {"scout": analysis, "strategist": strategy, "approvalRequired": True, "generatedAt": datetime.now(timezone.utc).isoformat()}
