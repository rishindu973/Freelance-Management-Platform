import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Menu, X, Zap } from "lucide-react";

const navLinks = [
  { label: "Features", href: "#features" },
  { label: "How It Works", href: "#how-it-works" },
  { label: "Pricing", href: "#pricing" },
  { label: "FAQ", href: "#faq" },
];

export function Navbar() {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 20);
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  const scrollTo = (href: string) => {
    setMobileOpen(false);
    const el = document.querySelector(href);
    if (el) el.scrollIntoView({ behavior: "smooth" });
  };

  return (
    <nav
      className={`fixed top-0 z-50 w-full transition-all duration-300 ${
        scrolled
          ? "border-b bg-background/90 backdrop-blur-xl shadow-sm"
          : "bg-transparent"
      }`}
    >
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-6">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-2 group">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary transition-transform group-hover:scale-110">
            <Zap className="h-4 w-4 text-primary-foreground" />
          </div>
          <span className="text-lg font-bold tracking-tight text-foreground">
            FreelanceFlow
          </span>
        </Link>

        {/* Desktop links */}
        <div className="hidden items-center gap-8 md:flex">
          {navLinks.map((link) => (
            <button
              key={link.href}
              onClick={() => scrollTo(link.href)}
              className="text-sm text-muted-foreground transition-colors hover:text-foreground cursor-pointer"
            >
              {link.label}
            </button>
          ))}
        </div>

        {/* Desktop actions */}
        <div className="hidden items-center gap-3 md:flex">
          <Button variant="ghost" size="sm" asChild>
            <Link to="/login">Log In</Link>
          </Button>
          <Button size="sm" className="shadow-md shadow-primary/25" asChild>
            <Link to="/register">Start Free Trial</Link>
          </Button>
        </div>

        {/* Mobile toggle */}
        <button
          className="md:hidden relative z-50"
          onClick={() => setMobileOpen(!mobileOpen)}
          aria-label="Toggle menu"
        >
          {mobileOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
        </button>
      </div>

      {/* Mobile menu — full overlay */}
      {mobileOpen && (
        <div className="fixed inset-0 top-16 bg-background/98 backdrop-blur-xl px-6 pt-8 pb-6 md:hidden animate-in fade-in slide-in-from-top-2 duration-200">
          <div className="flex flex-col gap-6">
            {navLinks.map((link) => (
              <button
                key={link.href}
                onClick={() => scrollTo(link.href)}
                className="text-lg font-medium text-foreground transition-colors hover:text-primary text-left"
              >
                {link.label}
              </button>
            ))}
            <div className="mt-4 flex flex-col gap-3 border-t pt-6">
              <Button variant="outline" size="lg" asChild>
                <Link to="/login">Log In</Link>
              </Button>
              <Button size="lg" className="shadow-md shadow-primary/25" asChild>
                <Link to="/register">Start Free Trial</Link>
              </Button>
            </div>
          </div>
        </div>
      )}
    </nav>
  );
}
