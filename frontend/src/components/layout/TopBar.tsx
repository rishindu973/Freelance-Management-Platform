import { useLocation } from "react-router-dom";
import { Bell } from "lucide-react";
import { SidebarTrigger } from "@/components/ui/sidebar";
import { Button } from "@/components/ui/button";

const routeTitles: Record<string, string> = {
  "/dashboard": "Dashboard",
  "/team": "Team",
  "/clients": "Clients",
  "/projects": "Projects",
  "/tasks": "Tasks",
  "/deliverables": "Deliverables",
  "/invoices": "Invoices",
  "/payments": "Payments",
  "/reports": "Reports",
  "/notifications": "Notifications",
  "/settings": "Settings",
};

export function TopBar() {
  const location = useLocation();

  const getTitle = () => {
    for (const [path, title] of Object.entries(routeTitles)) {
      if (location.pathname === path || location.pathname.startsWith(path + "/")) {
        return title;
      }
    }
    return "Dashboard";
  };

  return (
    <header className="flex h-14 shrink-0 items-center gap-3 border-b bg-background px-4">
      <SidebarTrigger className="-ml-1" />

      <div className="h-5 w-px bg-border" />

      <h1 className="text-sm font-medium text-foreground">{getTitle()}</h1>

      <div className="ml-auto flex items-center gap-2">
        <Button variant="ghost" size="icon" className="relative" asChild>
          <a href="/notifications">
            <Bell className="h-4 w-4 text-muted-foreground" />
            <span className="absolute right-1.5 top-1.5 h-2 w-2 rounded-full bg-destructive" />
          </a>
        </Button>
        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary text-xs font-medium text-primary-foreground">
          JD
        </div>
      </div>
    </header>
  );
}
