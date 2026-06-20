import { useLocation } from "react-router-dom";
import { Bell, ChevronDown } from "lucide-react";
import { SidebarTrigger } from "@/components/ui/sidebar";
import { Button } from "@/components/ui/button";
import { useEffect, useState, useRef } from "react";
import { ManagerService, ManagerProfile } from "@/api/managerService";
import { ActivityService, ActivityResponse } from "@/api/activityService";
import { useAuth } from "@/context/AuthContext";
import { LogOut } from "lucide-react";

const routeTitles: Record<string, string> = {
  "/dashboard": "Dashboard",
  "/team": "Team",
  "/clients": "Clients",
  "/freelancers": "Freelancers",
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
  const { logout } = useAuth();
  const [manager, setManager] = useState<ManagerProfile | null>(null);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const [notifications, setNotifications] = useState<ActivityResponse[]>([]);
  const notificationsRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const profile = await ManagerService.getManagerProfile();
        setManager(profile);
      } catch (err) {
        console.error("Failed to load manager profile", err);
      }
    };
    const fetchNotifications = async () => {
      try {
        const res = await ActivityService.getActivities({ size: 5 });
        setNotifications(res.content);
      } catch (err) {
        console.error("Failed to load activities", err);
      }
    };
    fetchProfile();
    fetchNotifications();
  }, []);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setDropdownOpen(false);
      }
      if (notificationsRef.current && !notificationsRef.current.contains(event.target as Node)) {
        setNotificationsOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

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
        <div className="relative" ref={notificationsRef}>
          <Button variant="ghost" size="icon" className="relative" onClick={() => setNotificationsOpen(!notificationsOpen)}>
            <Bell className="h-4 w-4 text-muted-foreground" />
            {notifications.length > 0 && (
              <span className="absolute right-1.5 top-1.5 h-2 w-2 rounded-full bg-destructive" />
            )}
          </Button>

          {notificationsOpen && (
            <div className="absolute right-0 top-full mt-2 w-80 rounded-xl border bg-card p-4 shadow-lg ring-1 ring-black/5 z-50 animate-in fade-in slide-in-from-top-2">
              <div className="mb-3 border-b pb-3">
                <p className="font-semibold text-foreground text-sm">Notifications</p>
              </div>
              <div className="space-y-3 max-h-64 overflow-y-auto">
                {notifications.length === 0 ? (
                  <p className="text-sm text-muted-foreground">No new notifications.</p>
                ) : (
                  notifications.map((notif) => (
                    <div key={notif.id} className="text-sm">
                      <p className="text-foreground">{notif.description}</p>
                      <p className="text-xs text-muted-foreground mt-1">
                        {new Date(notif.timestamp).toLocaleString("en-US", { month: "short", day: "numeric", hour: "numeric", minute: "2-digit" })}
                      </p>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}
        </div>
        <div className="relative ml-2 border-l pl-4" ref={dropdownRef}>
          <button
            onClick={() => setDropdownOpen(!dropdownOpen)}
            className="flex items-center gap-2 rounded-full border bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 p-1 pr-3 transition-colors"
          >
            <div className="flex h-7 w-7 items-center justify-center rounded-full bg-primary text-xs font-semibold text-primary-foreground">
              {manager?.fullName ? manager.fullName.charAt(0).toUpperCase() : "M"}
            </div>
            <div className="hidden text-left sm:block">
              <p className="text-sm font-medium leading-none text-foreground">{manager?.fullName || "Manager"}</p>
            </div>
            <ChevronDown className="h-4 w-4 text-muted-foreground" />
          </button>

          {dropdownOpen && (
            <div className="absolute right-0 top-full mt-2 w-64 rounded-xl border bg-card p-4 shadow-lg ring-1 ring-black/5 z-50 animate-in fade-in slide-in-from-top-2">
              <div className="mb-3 border-b pb-3">
                <p className="font-semibold text-foreground text-sm">{manager?.fullName || "N/A"}</p>
                <p className="text-xs text-muted-foreground">{manager?.email || "N/A"}</p>
              </div>
              <div className="space-y-2 text-sm text-muted-foreground mb-4">
                <div className="flex justify-between">
                  <span className="font-medium">Company:</span>
                  <span className="text-foreground">{manager?.companyName || "N/A"}</span>
                </div>
                <div className="flex justify-between">
                  <span className="font-medium">Contact:</span>
                  <span className="text-foreground">{manager?.contactNumber || "N/A"}</span>
                </div>
              </div>
              <Button
                variant="destructive"
                size="sm"
                className="w-full flex items-center justify-center gap-2"
                onClick={logout}
              >
                <LogOut className="h-4 w-4" /> Sign out
              </Button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
