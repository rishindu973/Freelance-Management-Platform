import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Check, Sparkles, ArrowRight } from "lucide-react";

const tiers = [
  {
    name: "Starter",
    price: "$19",
    period: "/month",
    description: "For solo managers with a small team.",
    features: [
      "Up to 5 freelancers",
      "Unlimited projects",
      "Basic invoicing",
      "Email support",
    ],
  },
  {
    name: "Professional",
    price: "$49",
    period: "/month",
    description: "For growing teams and agencies.",
    features: [
      "Up to 20 freelancers",
      "Client portal",
      "Advanced reports",
      "Deliverable review",
      "Priority support",
    ],
    popular: true,
  },
  {
    name: "Agency",
    price: "$99",
    period: "/month",
    description: "For established agencies at scale.",
    features: [
      "Unlimited freelancers",
      "Custom branding",
      "API access",
      "Dedicated account manager",
      "Advanced analytics",
      "SSO",
    ],
  },
];

export function Pricing() {
  return (
    <section id="pricing" className="relative px-6 py-24 md:py-32 bg-cream/50">
      {/* Background accent */}
      <div
        className="pointer-events-none absolute top-0 right-0 h-[400px] w-[400px] opacity-[0.04] rounded-full"
        style={{
          background: "radial-gradient(circle, hsl(152,40%,45%), transparent 70%)",
        }}
      />

      <div className="relative mx-auto max-w-6xl">
        {/* Header */}
        <div className="text-center">
          <div className="inline-flex items-center gap-2 rounded-full border bg-card px-4 py-1.5 text-xs font-medium text-muted-foreground mb-6">
            <Sparkles className="h-3.5 w-3.5 text-primary" />
            Pricing
          </div>
          <h2 className="text-3xl font-bold tracking-tight text-foreground sm:text-4xl">
            Simple, transparent{" "}
            <span className="text-primary">pricing</span>
          </h2>
          <p className="mx-auto mt-4 max-w-xl text-lg text-muted-foreground">
            Choose the plan that fits your team. No hidden fees, no surprises.
          </p>
        </div>

        {/* Pricing cards */}
        <div className="mt-16 grid gap-6 sm:grid-cols-2 lg:grid-cols-3 items-stretch">
          {tiers.map((tier) => (
            <div
              key={tier.name}
              className={`relative flex flex-col rounded-2xl border p-7 transition-all duration-300 hover:shadow-lg hover:-translate-y-1 ${tier.popular
                  ? "border-primary/30 bg-gradient-to-b from-primary/5 to-transparent shadow-lg shadow-primary/5 scale-[1.02] lg:scale-105"
                  : "bg-card"
                }`}
            >
              {/* Popular badge */}
              {tier.popular && (
                <div className="absolute -top-3 left-1/2 -translate-x-1/2">
                  <span className="inline-flex items-center gap-1.5 rounded-full bg-primary px-4 py-1 text-xs font-semibold text-primary-foreground shadow-md shadow-primary/25">
                    <Sparkles className="h-3 w-3" />
                    Most Popular
                  </span>
                </div>
              )}

              {/* Plan name + description */}
              <h3 className="text-lg font-bold text-foreground">{tier.name}</h3>
              <p className="mt-1 text-sm text-muted-foreground">
                {tier.description}
              </p>

              {/* Price */}
              <div className="mt-6 flex items-baseline gap-1">
                <span className="text-4xl font-bold text-foreground">{tier.price}</span>
                <span className="text-sm text-muted-foreground">{tier.period}</span>
              </div>

              {/* Divider */}
              <div className="my-6 h-px bg-border" />

              {/* Features */}
              <ul className="flex flex-col gap-3 flex-1">
                {tier.features.map((f) => (
                  <li
                    key={f}
                    className="flex items-start gap-2.5 text-sm text-muted-foreground"
                  >
                    <div className="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-success/15 mt-0.5">
                      <Check className="h-3 w-3 text-success" />
                    </div>
                    {f}
                  </li>
                ))}
              </ul>

              {/* CTA */}
              <div className="mt-8">
                <Button
                  variant={tier.popular ? "default" : "outline"}
                  className={`w-full h-11 ${tier.popular
                      ? "shadow-md shadow-primary/25"
                      : ""
                    }`}
                  asChild
                >
                  <Link to="/register" className="flex items-center gap-2">
                    Get Started
                    <ArrowRight className="h-4 w-4" />
                  </Link>
                </Button>
              </div>
            </div>
          ))}
        </div>

        {/* Bottom trust note */}
        <p className="mt-10 text-center text-sm text-muted-foreground">
          All plans include a 14-day free trial. No credit card required.
        </p>
      </div>
    </section>
  );
}
