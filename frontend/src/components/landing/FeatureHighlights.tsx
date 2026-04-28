import { ShieldCheck, Clock, PieChart, Sparkles } from "lucide-react";

const features = [
  {
    icon: ShieldCheck,
    title: "Organized team control",
    description:
      "Manage freelancer credentials, track availability, and keep your team organized — no spreadsheets, no chaos.",
    stats: { label: "Time saved weekly", value: "6+ hours" },
  },
  {
    icon: Clock,
    title: "Deadline clarity",
    description:
      "See upcoming deadlines at a glance with smart notifications. Never miss a deliverable or payment date again.",
    stats: { label: "On-time delivery rate", value: "98%" },
  },
  {
    icon: PieChart,
    title: "Clean financial overview",
    description:
      "Track income, expenses, and profit in a calm dashboard. Generate invoices and record payments effortlessly.",
    stats: { label: "Revenue visibility", value: "Real-time" },
  },
];

export function FeatureHighlights() {
  return (
    <section className="relative px-6 py-24 md:py-32 overflow-hidden">
      {/* Decorative gradient blob */}
      <div
        className="pointer-events-none absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 h-[600px] w-[600px] opacity-[0.04] rounded-full"
        style={{
          background: "radial-gradient(circle, hsl(40,70%,52%), transparent 70%)",
        }}
      />

      <div className="relative mx-auto max-w-6xl">
        {/* Section header */}
        <div className="text-center mb-20">
          <div className="inline-flex items-center gap-2 rounded-full border bg-card px-4 py-1.5 text-xs font-medium text-muted-foreground mb-6">
            <Sparkles className="h-3.5 w-3.5 text-warning" />
            Why Teams Love Us
          </div>
          <h2 className="text-3xl font-bold tracking-tight text-foreground sm:text-4xl">
            Built for teams that value{" "}
            <span className="text-primary">calm productivity</span>
          </h2>
        </div>

        {/* Feature rows — alternating layout */}
        <div className="flex flex-col gap-24">
          {features.map((feature, i) => (
            <div
              key={feature.title}
              className={`flex flex-col items-center gap-10 md:flex-row md:gap-16 ${
                i % 2 === 1 ? "md:flex-row-reverse" : ""
              }`}
            >
              {/* Visual block */}
              <div className="w-full max-w-sm flex-shrink-0">
                <div
                  className="relative rounded-2xl border bg-gradient-to-br from-card to-cream p-8 shadow-sm"
                >
                  {/* Large icon */}
                  <div className="flex h-20 w-20 items-center justify-center rounded-2xl bg-primary/10 mx-auto">
                    <feature.icon className="h-10 w-10 text-primary" />
                  </div>

                  {/* Stats pill */}
                  <div className="mt-6 flex items-center justify-center gap-3 rounded-xl bg-card border px-5 py-3">
                    <div className="text-center">
                      <p className="text-xs text-muted-foreground">
                        {feature.stats.label}
                      </p>
                      <p className="text-lg font-bold text-primary mt-0.5">
                        {feature.stats.value}
                      </p>
                    </div>
                  </div>

                  {/* Decorative dots */}
                  <div
                    className="absolute -top-3 -right-3 h-24 w-24 opacity-[0.04] rounded-full"
                    style={{
                      backgroundImage:
                        "radial-gradient(circle at 2px 2px, hsl(152,35%,42%) 1.5px, transparent 0)",
                      backgroundSize: "12px 12px",
                    }}
                  />
                </div>
              </div>

              {/* Text block */}
              <div className="flex-1 text-center md:text-left">
                <h3 className="text-2xl font-bold text-foreground">
                  {feature.title}
                </h3>
                <p className="mt-4 text-lg leading-relaxed text-muted-foreground">
                  {feature.description}
                </p>
                <div className="mt-6 h-1 w-12 rounded-full bg-primary/30 mx-auto md:mx-0" />
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
