import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Check } from "lucide-react";

const tiers = [
  {
    name: "Starter",
    price: "$19",
    period: "/month",
    description: "For solo managers with a small team.",
    features: ["Up to 5 freelancers", "Unlimited projects", "Basic invoicing", "Email support"],
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
    <section className="bg-background px-6 py-20 md:py-28">
      <div className="mx-auto max-w-6xl">
        <div className="text-center">
          <h2 className="text-2xl font-semibold tracking-tight text-foreground sm:text-3xl">
            Simple, transparent pricing
          </h2>
          <p className="mx-auto mt-3 max-w-xl text-muted-foreground">
            Choose the plan that fits your team. No hidden fees.
          </p>
        </div>

        <div className="mt-14 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {tiers.map((tier) => (
            <div
              key={tier.name}
              className={`flex flex-col rounded-xl border p-7 shadow-sm ${
                tier.popular ? "border-primary/30 ring-1 ring-primary/10" : ""
              }`}
            >
              {tier.popular && (
                <span className="mb-4 w-fit rounded-full bg-cream px-3 py-1 text-xs font-medium text-foreground">
                  Most popular
                </span>
              )}
              <h3 className="text-lg font-semibold text-foreground">{tier.name}</h3>
              <p className="mt-1 text-sm text-muted-foreground">{tier.description}</p>
              <div className="mt-5">
                <span className="text-3xl font-semibold text-foreground">{tier.price}</span>
                <span className="text-sm text-muted-foreground">{tier.period}</span>
              </div>

              <ul className="mt-6 flex flex-col gap-2.5">
                {tier.features.map((f) => (
                  <li key={f} className="flex items-start gap-2 text-sm text-muted-foreground">
                    <Check className="mt-0.5 h-4 w-4 shrink-0 text-success" />
                    {f}
                  </li>
                ))}
              </ul>

              <div className="mt-auto pt-7">
                <Button
                  variant={tier.popular ? "default" : "outline"}
                  className="w-full"
                  asChild
                >
                  <Link to="/signup">Get Started</Link>
                </Button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
