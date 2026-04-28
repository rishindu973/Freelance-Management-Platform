import {
  Users,
  ListChecks,
  Upload,
  FileText,
  CreditCard,
  BarChart3,
  ArrowUpRight,
} from "lucide-react";

const cards = [
  {
    icon: Users,
    title: "Team Management",
    description:
      "Add freelancers, generate credentials, and manage your entire team from a single, clean dashboard.",
    gradient: "from-primary/10 to-primary/5",
    iconBg: "bg-primary/15",
    iconColor: "text-primary",
  },
  {
    icon: ListChecks,
    title: "Task Assignment",
    description:
      "Assign tasks with deadlines and priorities. Keep everyone aligned and on track effortlessly.",
    gradient: "from-warning/10 to-warning/5",
    iconBg: "bg-warning/15",
    iconColor: "text-warning",
  },
  {
    icon: Upload,
    title: "Deliverable Review",
    description:
      "Freelancers upload their work. You review, approve, or request changes — all in one place.",
    gradient: "from-success/10 to-success/5",
    iconBg: "bg-success/15",
    iconColor: "text-success",
  },
  {
    icon: FileText,
    title: "Invoice Automation",
    description:
      "Generate professional invoices with auto-numbering, line items, and tax calculations.",
    gradient: "from-primary/10 to-primary/5",
    iconBg: "bg-primary/15",
    iconColor: "text-primary",
  },
  {
    icon: CreditCard,
    title: "Payment Tracking",
    description:
      "Record payments, track overdue invoices, and support partial payments — stress-free.",
    gradient: "from-warning/10 to-warning/5",
    iconBg: "bg-warning/15",
    iconColor: "text-warning",
  },
  {
    icon: BarChart3,
    title: "Financial Insights",
    description:
      "See income vs expenses, profit summaries, and project reports at a glance on your dashboard.",
    gradient: "from-success/10 to-success/5",
    iconBg: "bg-success/15",
    iconColor: "text-success",
  },
];

export function ValueProposition() {
  return (
    <section id="features" className="relative px-6 py-24 md:py-32 overflow-hidden">
      {/* Subtle background pattern */}
      <div className="pointer-events-none absolute inset-0 opacity-[0.02]"
        style={{
          backgroundImage: `radial-gradient(circle at 1px 1px, hsl(220,20%,20%) 1px, transparent 0)`,
          backgroundSize: "40px 40px",
        }}
      />

      <div className="relative mx-auto max-w-6xl">
        {/* Section header */}
        <div className="text-center">
          <div className="inline-flex items-center gap-2 rounded-full border bg-card px-4 py-1.5 text-xs font-medium text-muted-foreground mb-6">
            <span className="h-1.5 w-1.5 rounded-full bg-primary animate-pulse" />
            Features
          </div>
          <h2 className="text-3xl font-bold tracking-tight text-foreground sm:text-4xl">
            Everything you need to run{" "}
            <span className="text-primary">your team</span>
          </h2>
          <p className="mx-auto mt-4 max-w-xl text-lg text-muted-foreground">
            From onboarding freelancers to tracking payments — all in a calm,
            organized workspace.
          </p>
        </div>

        {/* Feature cards */}
        <div className="mt-16 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {cards.map((card, i) => (
            <div
              key={card.title}
              className={`group relative flex flex-col rounded-2xl border bg-gradient-to-br ${card.gradient} p-6 transition-all duration-300 hover:shadow-lg hover:-translate-y-1`}
            >
              {/* Icon */}
              <div
                className={`flex h-12 w-12 items-center justify-center rounded-xl ${card.iconBg} transition-transform group-hover:scale-110`}
              >
                <card.icon className={`h-5 w-5 ${card.iconColor}`} />
              </div>

              {/* Text */}
              <h3 className="mt-5 text-lg font-semibold text-foreground flex items-center gap-2">
                {card.title}
                <ArrowUpRight className="h-4 w-4 text-muted-foreground opacity-0 transition-all group-hover:opacity-100 group-hover:translate-x-0.5 group-hover:-translate-y-0.5" />
              </h3>
              <p className="mt-2 text-sm leading-relaxed text-muted-foreground">
                {card.description}
              </p>

              {/* Hover border accent */}
              <div className="absolute inset-0 rounded-2xl border-2 border-transparent transition-colors group-hover:border-primary/15 pointer-events-none" />
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
