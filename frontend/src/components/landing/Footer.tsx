import { Link } from "react-router-dom";

export function Footer() {
  return (
    <footer className="border-t bg-cream px-6 py-10">
      <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-6 sm:flex-row">
        <Link to="/" className="text-sm font-semibold text-foreground">
          FreelanceFlow
        </Link>

        <div className="flex gap-6 text-sm text-muted-foreground">
          <Link to="/privacy" className="transition-colors hover:text-foreground">Privacy</Link>
          <Link to="/terms" className="transition-colors hover:text-foreground">Terms</Link>
          <Link to="/contact" className="transition-colors hover:text-foreground">Contact</Link>
        </div>

        <p className="text-xs text-muted-foreground">
          © {new Date().getFullYear()} FreelanceFlow
        </p>
      </div>
    </footer>
  );
}
