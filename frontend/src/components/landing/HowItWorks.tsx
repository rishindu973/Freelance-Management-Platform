import { Building2, UserPlus, FolderKanban, FileText, TrendingUp } from "lucide-react";

const steps = [
  { icon: Building2, title: "Create your organization", description: "Sign up and set up your workspace in seconds." },
  { icon: UserPlus, title: "Add freelancers", description: "Invite team members and generate their login credentials." },
  { icon: FolderKanban, title: "Assign projects", description: "Create projects, assign tasks, and set deadlines." },
  { icon: FileText, title: "Generate invoices", description: "Auto-create invoices with line items and send to clients." },
  { icon: TrendingUp, title: "Track payments", description: "Record payments, monitor profit, and stay on top of finances." },
];

export function HowItWorks() {
  return (
    <section className="bg-background px-6 py-20 md:py-28">
      <div className="mx-auto max-w-6xl">
        <div className="text-center">
          <h2 className="text-2xl font-semibold tracking-tight text-foreground sm:text-3xl">
            How it works
          </h2>
          <p className="mx-auto mt-3 max-w-xl text-muted-foreground">
            Get from zero to full project management in five simple steps.
          </p>
        </div>

        <div className="mt-14 flex flex-col gap-0">
          {steps.map((step, i) => (
            <div key={step.title} className="flex gap-6">
              {/* Vertical line + circle */}
              <div className="flex flex-col items-center">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full border-2 border-primary bg-background text-sm font-semibold text-foreground">
                  {i + 1}
                </div>
                {i < steps.length - 1 && <div className="w-px flex-1 bg-border" />}
              </div>

              {/* Content */}
              <div className="pb-10">
                <div className="flex items-center gap-2">
                  <step.icon className="h-4 w-4 text-muted-foreground" />
                  <h3 className="font-semibold text-foreground">{step.title}</h3>
                </div>
                <p className="mt-1 text-sm text-muted-foreground">{step.description}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
