import { Users, ListChecks, Upload, FileText, CreditCard, BarChart3 } from "lucide-react";

const cards = [
  {
    icon: Users,
    title: "Team Management",
    description: "Add freelancers, generate credentials, and manage your team in one view.",
  },
  {
    icon: ListChecks,
    title: "Task Assignment",
    description: "Assign tasks with deadlines and priorities. Everyone stays on track.",
  },
  {
    icon: Upload,
    title: "Deliverable Review",
    description: "Freelancers upload work, you review, approve, or request changes.",
  },
  {
    icon: FileText,
    title: "Invoice Automation",
    description: "Generate professional invoices with auto-numbering and line items.",
  },
  {
    icon: CreditCard,
    title: "Payment Tracking",
    description: "Record payments, track overdue invoices, and support partial payments.",
  },
  {
    icon: BarChart3,
    title: "Financial Insights",
    description: "See income vs expenses, profit summaries, and project reports at a glance.",
  },
];

export function ValueProposition() {
  return (
    <section className="bg-cream px-6 py-20 md:py-28">
      <div className="mx-auto max-w-6xl">
        <div className="text-center">
          <h2 className="text-2xl font-semibold tracking-tight text-foreground sm:text-3xl">
            Everything you need to run your team
          </h2>
          <p className="mx-auto mt-3 max-w-xl text-muted-foreground">
            From onboarding freelancers to tracking payments — all in a calm, organized workspace.
          </p>
        </div>

        <div className="mt-14 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {cards.map((card) => (
            <div
              key={card.title}
              className="rounded-xl border bg-card p-6 shadow-sm transition-shadow hover:shadow-md"
            >
              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-cream">
                <card.icon className="h-5 w-5 text-foreground" />
              </div>
              <h3 className="mt-4 font-semibold text-foreground">{card.title}</h3>
              <p className="mt-2 text-sm leading-relaxed text-muted-foreground">{card.description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
