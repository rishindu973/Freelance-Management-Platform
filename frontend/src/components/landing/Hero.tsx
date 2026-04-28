import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import {
  ArrowRight,
  LayoutDashboard,
  Users,
  FileText,
  CreditCard,
  TrendingUp,
  Star,
} from "lucide-react";

/* Animated floating stat pill */
function FloatingPill({
  icon: Icon,
  label,
  value,
  className = "",
  delay = "0s",
}: {
  icon: any;
  label: string;
  value: string;
  className?: string;
  delay?: string;
}) {
  return (
    <div
      className={`absolute flex items-center gap-2.5 rounded-2xl border bg-card/90 backdrop-blur-sm px-4 py-2.5 shadow-lg ${className}`}
      style={{
        animation: `float 6s ease-in-out infinite`,
        animationDelay: delay,
      }}
    >
      <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary/10">
        <Icon className="h-4 w-4 text-primary" />
      </div>
      <div>
        <p className="text-xs text-muted-foreground leading-none">{label}</p>
        <p className="text-sm font-semibold text-foreground mt-0.5">{value}</p>
      </div>
    </div>
  );
}

export function Hero() {
  return (
    <section className="relative overflow-hidden px-6 pb-20 pt-28 md:pb-32 md:pt-36">
      {/* Background gradient orbs */}
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <div
          className="absolute -top-24 left-1/4 h-[500px] w-[500px] rounded-full opacity-[0.07]"
          style={{
            background: "radial-gradient(circle, hsl(152,50%,45%) 0%, transparent 70%)",
          }}
        />
        <div
          className="absolute top-32 right-[10%] h-[400px] w-[400px] rounded-full opacity-[0.05]"
          style={{
            background: "radial-gradient(circle, hsl(40,80%,55%) 0%, transparent 70%)",
          }}
        />
        <div
          className="absolute -bottom-20 left-[15%] h-[350px] w-[350px] rounded-full opacity-[0.04]"
          style={{
            background: "radial-gradient(circle, hsl(152,40%,50%) 0%, transparent 70%)",
          }}
        />
      </div>

      <div className="relative mx-auto max-w-6xl">
        <div className="flex flex-col items-center text-center">
          {/* Trust badge */}
          <div className="mb-8 inline-flex items-center gap-2 rounded-full border bg-card/80 backdrop-blur-sm px-4 py-1.5 shadow-sm">
            <div className="flex -space-x-1">
              {[...Array(5)].map((_, i) => (
                <Star
                  key={i}
                  className="h-3.5 w-3.5 fill-warning text-warning"
                />
              ))}
            </div>
            <span className="text-xs font-medium text-muted-foreground">
              Trusted by 2,000+ teams worldwide
            </span>
          </div>

          {/* Main headline */}
          <h1 className="max-w-3xl text-4xl font-bold leading-[1.1] tracking-tight text-foreground sm:text-5xl md:text-6xl">
            Your Team. Your Projects.{" "}
            <span
              className="bg-clip-text text-transparent"
              style={{
                backgroundImage:
                  "linear-gradient(135deg, hsl(152,45%,40%) 0%, hsl(152,35%,55%) 50%, hsl(40,70%,52%) 100%)",
              }}
            >
              Effortlessly in Sync.
            </span>
          </h1>

          {/* Sub-headline */}
          <p className="mt-6 max-w-xl text-lg leading-relaxed text-muted-foreground md:text-xl">
            The calm workspace where freelancer management, project tracking,
            and payments come together — without the chaos.
          </p>

          {/* CTA buttons */}
          <div className="mt-10 flex flex-col gap-3.5 sm:flex-row">
            <Button
              size="lg"
              className="h-12 px-8 text-base shadow-lg shadow-primary/25 transition-all hover:shadow-xl hover:shadow-primary/30"
              asChild
            >
              <Link to="/register">
                Get Started Free
                <ArrowRight className="ml-1 h-4 w-4" />
              </Link>
            </Button>
            <Button
              variant="outline"
              size="lg"
              className="h-12 px-8 text-base"
              asChild
            >
              <Link to="/login">View Live Demo</Link>
            </Button>
          </div>

          {/* Social proof line */}
          <p className="mt-6 text-sm text-muted-foreground">
            Free 14-day trial · No credit card required
          </p>
        </div>

        {/* Floating dashboard preview with stats */}
        <div className="relative mx-auto mt-16 max-w-3xl md:mt-20">
          {/* Floating pills */}
          <FloatingPill
            icon={TrendingUp}
            label="Revenue"
            value="$48,200"
            className="hidden md:flex -left-16 top-8 lg:-left-24"
            delay="0s"
          />
          <FloatingPill
            icon={Users}
            label="Active Team"
            value="12 members"
            className="hidden md:flex -right-12 top-16 lg:-right-20"
            delay="2s"
          />
          <FloatingPill
            icon={FileText}
            label="Invoices Sent"
            value="38 this month"
            className="hidden md:flex -left-8 bottom-12 lg:-left-16"
            delay="4s"
          />

          {/* Main dashboard card */}
          <div
            className="rounded-2xl border bg-card/80 backdrop-blur-sm p-6 shadow-2xl shadow-primary/5"
            style={{
              background:
                "linear-gradient(145deg, hsl(48,80%,99%) 0%, hsl(45,60%,96%) 100%)",
            }}
          >
            {/* Top bar mock */}
            <div className="flex items-center justify-between border-b pb-4 mb-5">
              <div className="flex items-center gap-3">
                <div className="h-3 w-3 rounded-full bg-destructive/40" />
                <div className="h-3 w-3 rounded-full bg-warning/40" />
                <div className="h-3 w-3 rounded-full bg-success/40" />
              </div>
              <div className="flex items-center gap-2 rounded-lg bg-muted px-3 py-1.5">
                <div className="h-2 w-2 rounded-full bg-success" />
                <span className="text-xs text-muted-foreground font-medium">
                  Dashboard — Live
                </span>
              </div>
              <div className="w-16" />
            </div>

            {/* KPI row */}
            <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
              {[
                {
                  label: "Income",
                  value: "$12,400",
                  icon: CreditCard,
                  change: "+12%",
                },
                {
                  label: "Projects",
                  value: "8 active",
                  icon: LayoutDashboard,
                  change: "+3",
                },
                {
                  label: "Team",
                  value: "6 members",
                  icon: Users,
                  change: "All active",
                },
                {
                  label: "Invoices",
                  value: "3 pending",
                  icon: FileText,
                  change: "$4,800",
                },
              ].map((kpi) => (
                <div
                  key={kpi.label}
                  className="rounded-xl border bg-card p-3.5 transition-all hover:shadow-md hover:-translate-y-0.5"
                >
                  <div className="flex items-center gap-2">
                    <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-primary/10">
                      <kpi.icon className="h-3.5 w-3.5 text-primary" />
                    </div>
                    <span className="text-xs text-muted-foreground">{kpi.label}</span>
                  </div>
                  <p className="mt-2 text-sm font-bold text-foreground">{kpi.value}</p>
                  <p className="mt-0.5 text-xs text-success font-medium">{kpi.change}</p>
                </div>
              ))}
            </div>

            {/* Chart bars */}
            <div className="mt-5 flex h-28 items-end gap-1.5 rounded-xl border bg-card px-4 pb-4 pt-8">
              {[35, 50, 40, 65, 55, 70, 60, 75, 68, 80, 72, 85].map(
                (h, i) => (
                  <div
                    key={i}
                    className="flex-1 rounded-md transition-all duration-500 hover:opacity-80"
                    style={{
                      height: `${h}%`,
                      background: `linear-gradient(to top, hsl(152,35%,42%), hsl(152,40%,55%))`,
                      opacity: 0.15 + (h / 100) * 0.85,
                      animationDelay: `${i * 80}ms`,
                    }}
                  />
                )
              )}
            </div>
          </div>

          {/* Glow effect behind card */}
          <div
            className="absolute inset-0 -z-10 blur-3xl opacity-[0.08]"
            style={{
              background:
                "radial-gradient(ellipse at center, hsl(152,40%,45%), transparent 70%)",
            }}
          />
        </div>
      </div>

      {/* CSS animation */}
      <style>{`
        @keyframes float {
          0%, 100% { transform: translateY(0px); }
          50% { transform: translateY(-12px); }
        }
      `}</style>
    </section>
  );
}
