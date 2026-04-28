import {
  Building2,
  UserPlus,
  FolderKanban,
  FileText,
  TrendingUp,
} from "lucide-react";

const steps = [
  {
    icon: Building2,
    title: "Create your workspace",
    description:
      "Sign up and set up your organization in seconds. No complex onboarding.",
    accent: "bg-primary",
  },
  {
    icon: UserPlus,
    title: "Invite your team",
    description:
      "Add freelancers and generate their login credentials instantly.",
    accent: "bg-warning",
  },
  {
    icon: FolderKanban,
    title: "Assign projects & tasks",
    description:
      "Create projects, set deadlines, assign tasks, and track progress.",
    accent: "bg-success",
  },
  {
    icon: FileText,
    title: "Generate invoices",
    description:
      "Auto-create professional invoices with line items and send to clients.",
    accent: "bg-primary",
  },
  {
    icon: TrendingUp,
    title: "Track & grow",
    description:
      "Monitor payments, analyze profit, and scale your operations with confidence.",
    accent: "bg-warning",
  },
];

export function HowItWorks() {
  return (
    <section id="how-it-works" className="relative px-6 py-24 md:py-32 bg-cream/50">
      {/* Background decorative gradient */}
      <div
        className="pointer-events-none absolute top-0 left-0 h-full w-full opacity-[0.03]"
        style={{
          background:
            "linear-gradient(180deg, transparent 0%, hsl(152,35%,42%) 50%, transparent 100%)",
        }}
      />

      <div className="relative mx-auto max-w-6xl">
        {/* Header */}
        <div className="text-center">
          <div className="inline-flex items-center gap-2 rounded-full border bg-card px-4 py-1.5 text-xs font-medium text-muted-foreground mb-6">
            <span className="h-1.5 w-1.5 rounded-full bg-success animate-pulse" />
            Simple Setup
          </div>
          <h2 className="text-3xl font-bold tracking-tight text-foreground sm:text-4xl">
            Up and running in{" "}
            <span className="text-primary">5 minutes</span>
          </h2>
          <p className="mx-auto mt-4 max-w-xl text-lg text-muted-foreground">
            Get from zero to full project management in five simple steps.
          </p>
        </div>

        {/* Steps — horizontal on desktop, vertical on mobile */}
        <div className="mt-16">
          {/* Desktop: horizontal layout */}
          <div className="hidden lg:flex items-start gap-0">
            {steps.map((step, i) => (
              <div key={step.title} className="flex-1 flex flex-col items-center text-center group">
                {/* Number circle + connector */}
                <div className="relative flex items-center w-full justify-center">
                  {/* Left connector */}
                  {i > 0 && (
                    <div className="absolute right-1/2 top-1/2 -translate-y-1/2 w-full h-0.5 bg-border" />
                  )}
                  {/* Right connector */}
                  {i < steps.length - 1 && (
                    <div className="absolute left-1/2 top-1/2 -translate-y-1/2 w-full h-0.5 bg-border" />
                  )}

                  <div
                    className={`relative z-10 flex h-14 w-14 items-center justify-center rounded-2xl ${step.accent} text-white text-lg font-bold shadow-lg transition-transform group-hover:scale-110`}
                    style={{
                      boxShadow: `0 8px 24px -4px hsla(152,35%,42%,0.2)`,
                    }}
                  >
                    {i + 1}
                  </div>
                </div>

                {/* Icon + text */}
                <div className="mt-6 px-3">
                  <step.icon className="mx-auto h-5 w-5 text-muted-foreground mb-3" />
                  <h3 className="font-semibold text-foreground text-sm">{step.title}</h3>
                  <p className="mt-2 text-xs leading-relaxed text-muted-foreground">
                    {step.description}
                  </p>
                </div>
              </div>
            ))}
          </div>

          {/* Mobile: vertical timeline */}
          <div className="flex flex-col gap-0 lg:hidden">
            {steps.map((step, i) => (
              <div key={step.title} className="flex gap-5">
                {/* Timeline */}
                <div className="flex flex-col items-center">
                  <div
                    className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-xl ${step.accent} text-white font-bold shadow-md`}
                  >
                    {i + 1}
                  </div>
                  {i < steps.length - 1 && (
                    <div className="w-0.5 flex-1 bg-border my-1" />
                  )}
                </div>

                {/* Content */}
                <div className="pb-10 pt-1">
                  <div className="flex items-center gap-2">
                    <step.icon className="h-4 w-4 text-muted-foreground" />
                    <h3 className="font-semibold text-foreground">{step.title}</h3>
                  </div>
                  <p className="mt-1.5 text-sm leading-relaxed text-muted-foreground">
                    {step.description}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
