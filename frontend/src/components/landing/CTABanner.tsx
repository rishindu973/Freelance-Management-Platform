import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";

export function CTABanner() {
  return (
    <section className="bg-background px-6 py-20 md:py-28">
      <div className="mx-auto max-w-2xl text-center">
        <h2 className="text-2xl font-semibold tracking-tight text-foreground sm:text-3xl">
          Start managing your team with clarity.
        </h2>
        <p className="mx-auto mt-3 max-w-md text-muted-foreground">
          Join managers who run calm, organized agencies — no complexity, no noise.
        </p>
        <div className="mt-8">
          <Button size="lg" asChild>
            <Link to="/signup">Get Started — It's Free</Link>
          </Button>
        </div>
      </div>
    </section>
  );
}
