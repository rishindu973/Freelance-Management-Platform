import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import { HelpCircle } from "lucide-react";

const faqs = [
  {
    q: "How do I add freelancers to my team?",
    a: "Once you create your organization, go to the Team section and click 'Add Member'. You can generate login credentials for them automatically.",
  },
  {
    q: "Can freelancers see each other's data?",
    a: "No. Freelancers can only see their own tasks, projects, and deliverables. All data is scoped to their account for complete privacy.",
  },
  {
    q: "How does invoicing work?",
    a: "You can create invoices per client with auto-numbered line items. Subtotals, tax, and totals are calculated automatically. Export as PDF anytime.",
  },
  {
    q: "Is there a free trial?",
    a: "Yes — every plan comes with a 14-day free trial. No credit card required to start. Cancel anytime with no questions asked.",
  },
  {
    q: "Can I track partial payments?",
    a: "Absolutely. When recording a payment, you can enter a partial amount. The remaining balance is tracked automatically on your dashboard.",
  },
  {
    q: "What happens when I cancel?",
    a: "Your data remains accessible in read-only mode for 30 days after cancellation. You can export everything at any time before that.",
  },
];

export function FAQ() {
  return (
    <section id="faq" className="relative px-6 py-24 md:py-32">
      <div className="mx-auto max-w-2xl">
        {/* Header */}
        <div className="text-center">
          <div className="inline-flex items-center gap-2 rounded-full border bg-card px-4 py-1.5 text-xs font-medium text-muted-foreground mb-6">
            <HelpCircle className="h-3.5 w-3.5 text-muted-foreground" />
            FAQ
          </div>
          <h2 className="text-3xl font-bold tracking-tight text-foreground sm:text-4xl">
            Frequently asked{" "}
            <span className="text-primary">questions</span>
          </h2>
          <p className="mt-4 text-lg text-muted-foreground">
            Everything you need to know about FreelanceFlow.
          </p>
        </div>

        {/* Accordion */}
        <Accordion type="single" collapsible className="mt-12">
          {faqs.map((faq, i) => (
            <AccordionItem
              key={i}
              value={`faq-${i}`}
              className="border rounded-xl mb-3 px-5 data-[state=open]:bg-cream/60 data-[state=open]:shadow-sm transition-all"
            >
              <AccordionTrigger className="text-left text-sm font-semibold text-foreground py-5 hover:no-underline hover:text-primary transition-colors">
                {faq.q}
              </AccordionTrigger>
              <AccordionContent className="text-sm leading-relaxed text-muted-foreground pb-5">
                {faq.a}
              </AccordionContent>
            </AccordionItem>
          ))}
        </Accordion>
      </div>
    </section>
  );
}
