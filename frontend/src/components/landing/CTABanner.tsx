import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { ArrowRight, Sparkles } from "lucide-react";

export function CTABanner() {
  return (
    <section className="px-6 py-24 md:py-32">
      <div className="mx-auto max-w-4xl">
        <div
          className="relative overflow-hidden rounded-3xl px-8 py-16 text-center md:px-16 md:py-20"
          style={{
            background:
              "linear-gradient(135deg, hsl(152,35%,38%) 0%, hsl(152,30%,30%) 40%, hsl(200,25%,22%) 100%)",
          }}
        >
          {/* Decorative orbs */}
          <div
            className="pointer-events-none absolute -top-20 -right-20 h-64 w-64 rounded-full opacity-20"
            style={{
              background: "radial-gradient(circle, hsl(40,70%,52%), transparent 70%)",
            }}
          />
          <div
            className="pointer-events-none absolute -bottom-16 -left-16 h-48 w-48 rounded-full opacity-15"
            style={{
              background: "radial-gradient(circle, hsl(152,50%,55%), transparent 70%)",
            }}
          />

          {/* Content */}
          <div className="relative">
            <div className="inline-flex items-center gap-2 rounded-full bg-white/10 backdrop-blur-sm px-4 py-1.5 text-xs font-medium text-white/80 mb-6 border border-white/10">
              <Sparkles className="h-3.5 w-3.5" />
              Start your free trial today
            </div>
            <h2 className="text-3xl font-bold tracking-tight text-white sm:text-4xl md:text-5xl">
              Ready to bring clarity
              <br />
              to your team?
            </h2>
            <p className="mx-auto mt-5 max-w-md text-base text-white/70 md:text-lg">
              Join 2,000+ managers who run calm, organized agencies — no
              complexity, no noise, just results.
            </p>
            <div className="mt-10 flex flex-col gap-3 sm:flex-row sm:justify-center">
              <Button
                size="lg"
                className="h-12 px-8 text-base bg-white text-foreground hover:bg-white/90 shadow-lg"
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
                className="h-12 px-8 text-base border-white/20 text-white hover:bg-white/10 bg-transparent"
                asChild
              >
                <Link to="/login">View Live Demo</Link>
              </Button>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
