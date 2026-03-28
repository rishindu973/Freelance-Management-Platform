import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { LayoutDashboard, Users, FileText, CreditCard } from "lucide-react";

export function Hero() {
  return (
    <section className="bg-background px-6 pb-20 pt-16 md:pb-28 md:pt-24">
      <div className="mx-auto max-w-6xl">
        <div className="flex flex-col items-center gap-16 lg:flex-row lg:gap-12">
          {/* Copy */}
          <div className="flex-1 text-center lg:text-left">
            <h1 className="text-3xl font-semibold leading-tight tracking-tight text-foreground sm:text-4xl md:text-5xl">
              Manage Freelancers, Projects &amp; Payments Calmly — In One Place.
            </h1>
            <p className="mt-5 text-lg leading-relaxed text-muted-foreground md:text-xl">
              A clean workspace for team leads who want clarity, not complexity.
            </p>
            <div className="mt-8 flex flex-col gap-3 sm:flex-row sm:justify-center lg:justify-start">
              <Button size="lg" asChild>
                <Link to="/signup">Get Started</Link>
              </Button>
              <Button variant="outline" size="lg" asChild>
                <Link to="/pricing">View Pricing</Link>
              </Button>
            </div>
          </div>

          {/* Dashboard preview mock */}
          <div className="w-full max-w-md flex-1 lg:max-w-lg">
            <div className="rounded-xl border bg-card p-5 shadow-sm">
              {/* Mini KPI row */}
              <div className="grid grid-cols-2 gap-3">
                {[
                  { label: "Income", value: "$12,400", icon: CreditCard },
                  { label: "Projects", value: "8 active", icon: LayoutDashboard },
                  { label: "Team", value: "6 members", icon: Users },
                  { label: "Invoices", value: "3 pending", icon: FileText },
                ].map((kpi) => (
                  <div key={kpi.label} className="rounded-lg border bg-cream p-3">
                    <div className="flex items-center gap-2">
                      <kpi.icon className="h-4 w-4 text-muted-foreground" />
                      <span className="text-xs text-muted-foreground">{kpi.label}</span>
                    </div>
                    <p className="mt-1 text-sm font-semibold text-foreground">{kpi.value}</p>
                  </div>
                ))}
              </div>
              {/* Fake chart placeholder */}
              <div className="mt-4 flex h-24 items-end gap-1.5 rounded-lg border bg-cream px-4 pb-3 pt-6">
                {[35, 50, 40, 65, 55, 70, 60, 75, 68, 80, 72, 85].map((h, i) => (
                  <div
                    key={i}
                    className="flex-1 rounded-sm bg-primary/20"
                    style={{ height: `${h}%` }}
                  />
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
