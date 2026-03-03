import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";

const faqs = [
  {
    q: "How do I add freelancers to my team?",
    a: "Once you create your organization, go to the Team section and click 'Add Member'. You can generate login credentials for them automatically.",
  },
  {
    q: "Can freelancers see each other's data?",
    a: "No. Freelancers can only see their own tasks, projects, and deliverables. All data is scoped to their account.",
  },
  {
    q: "How does invoicing work?",
    a: "You can create invoices per client with auto-numbered line items. Subtotals, tax, and totals are calculated automatically.",
  },
  {
    q: "Is there a free trial?",
    a: "Yes — every plan comes with a 14-day free trial. No credit card required to start.",
  },
  {
    q: "Can I track partial payments?",
    a: "Absolutely. When recording a payment, you can enter a partial amount. The remaining balance is tracked automatically.",
  },
  {
    q: "What happens when I cancel?",
    a: "Your data remains accessible in read-only mode for 30 days after cancellation. You can export everything at any time.",
  },
];

export function FAQ() {
  return (
    <section className="bg-cream px-6 py-20 md:py-28">
      <div className="mx-auto max-w-2xl">
        <div className="text-center">
          <h2 className="text-2xl font-semibold tracking-tight text-foreground sm:text-3xl">
            Frequently asked questions
          </h2>
          <p className="mt-3 text-muted-foreground">
            Everything you need to know about FreelanceFlow.
          </p>
        </div>

        <Accordion type="single" collapsible className="mt-10">
          {faqs.map((faq, i) => (
            <AccordionItem key={i} value={`faq-${i}`}>
              <AccordionTrigger className="text-left text-sm font-medium text-foreground">
                {faq.q}
              </AccordionTrigger>
              <AccordionContent className="text-sm leading-relaxed text-muted-foreground">
                {faq.a}
              </AccordionContent>
            </AccordionItem>
          ))}
        </Accordion>
      </div>
    </section>
  );
}
