import { ShieldCheck, Clock, PieChart } from "lucide-react";

const features = [
  {
    icon: ShieldCheck,
    title: "Organized team control",
    description:
      "Manage freelancer credentials, track availability, and keep your team organized without spreadsheets.",
  },
  {
    icon: Clock,
    title: "Deadline clarity",
    description:
      "See upcoming deadlines at a glance. Never miss a deliverable or payment date again.",
  },
  {
    icon: PieChart,
    title: "Clean financial overview",
    description:
      "Track income, expenses, and profit in a calm dashboard. Generate invoices and record payments effortlessly.",
  },
];

export function FeatureHighlights() {
  return (
    <section className="bg-cream px-6 py-20 md:py-28">
      <div className="mx-auto max-w-6xl">
        <div className="flex flex-col gap-16">
          {features.map((feature, i) => (
            <div
              key={feature.title}
              className={`flex flex-col items-center gap-8 md:flex-row ${
                i % 2 === 1 ? "md:flex-row-reverse" : ""
              }`}
            >
              {/* Icon block */}
              <div className="flex h-40 w-full max-w-xs items-center justify-center rounded-xl border bg-card shadow-sm">
                <feature.icon className="h-16 w-16 text-primary/40" />
              </div>

              {/* Text */}
              <div className="flex-1 text-center md:text-left">
                <h3 className="text-xl font-semibold text-foreground">{feature.title}</h3>
                <p className="mt-2 leading-relaxed text-muted-foreground">{feature.description}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
