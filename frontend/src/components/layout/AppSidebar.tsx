import {
  LayoutDashboard,
  Users,
  Building2,
  FolderKanban,
  ListChecks,
  Upload,
  FileText,
  CreditCard,
  BarChart3,
} from "lucide-react";
import { useLocation } from "react-router-dom";
import { NavLink } from "@/components/NavLink";
import { useEffect, useState } from "react";
import { ManagerService, ManagerProfile } from "@/api/managerService";
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarFooter,
  useSidebar,
} from "@/components/ui/sidebar";

const mainItems = [
  { title: "Dashboard", url: "/dashboard", icon: LayoutDashboard },
  { title: "Team", url: "/freelancers", icon: Users },
  { title: "Clients", url: "/clients", icon: Building2 },
  { title: "Projects", url: "/projects", icon: FolderKanban },
  { title: "Invoices", url: "/invoices", icon: FileText },
  { title: "Payments", url: "/payments", icon: CreditCard },
  { title: "Reports", url: "/reports", icon: BarChart3 },
];

export function AppSidebar() {
  const { state } = useSidebar();
  const collapsed = state === "collapsed";
  const location = useLocation();
  const [manager, setManager] = useState<ManagerProfile | null>(null);

  useEffect(() => {
    ManagerService.getManagerProfile()
      .then(setManager)
      .catch(() => { /* silently fail */ });
  }, []);

  const isActive = (url: string) =>
    location.pathname === url || location.pathname.startsWith(url + "/");

  const initials = manager?.fullName
    ? manager.fullName.split(" ").map((n) => n[0]).join("").toUpperCase().slice(0, 2)
    : "M";

  return (
    <Sidebar 
      collapsible="icon" 
      className={`border-r transition-all duration-300 ${
        collapsed 
          ? "bg-[#0A1F1A] border-transparent shadow-antigravity" 
          : "bg-[#FBF9F4] border-slate-200/50"
      }`}
    >
      <SidebarContent className={`pt-4 ${collapsed ? "text-slate-200" : "text-[#0F172A]"}`}>
        {/* Brand */}
        <div className="flex h-12 items-center px-4">
          {!collapsed && (
            <span className="text-base font-semibold tracking-tight text-foreground">
              FreelanceFlow
            </span>
          )}
          {collapsed && (
            <span className="text-base font-semibold text-foreground">FF</span>
          )}
        </div>

        {/* Main nav */}
        <SidebarGroup>
          <SidebarGroupLabel className={`text-xs uppercase tracking-wider ${collapsed ? "text-slate-400" : "text-slate-500"}`}>
            Main
          </SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {mainItems.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton
                    asChild
                    isActive={isActive(item.url)}
                    tooltip={item.title}
                  >
                    <NavLink
                      to={item.url}
                      className={`flex items-center gap-2 p-2 rounded-md transition-all duration-200 ${
                        collapsed 
                          ? "text-slate-400 hover:bg-[#12362D] hover:text-[#10B981]" 
                          : "text-slate-600 hover:text-[#0F172A] hover:bg-slate-100"
                      }`}
                      activeClassName={
                        collapsed
                          ? "text-[#10B981] drop-shadow-[0_0_8px_rgba(16,185,129,0.8)] font-medium"
                          : "bg-[#10B981]/15 text-[#0F172A] font-medium"
                      }
                    >
                      <item.icon className="h-5 w-5 stroke-[1.5]" />
                      {!collapsed && <span>{item.title}</span>}
                    </NavLink>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>


      </SidebarContent>

      <SidebarFooter className="border-t p-3">
        <div className="flex items-center gap-3">
          <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-primary text-xs font-medium text-primary-foreground">
            {initials}
          </div>
          {!collapsed && (
            <div className="min-w-0">
              <p className={`truncate text-sm font-medium ${collapsed ? "text-slate-200" : "text-[#0F172A]"}`}>
                {manager?.fullName ?? "Loading..."}
              </p>
              <p className={`truncate text-xs ${collapsed ? "text-slate-400" : "text-slate-500"}`}>
                {manager?.companyName ?? ""}
              </p>
            </div>
          )}
        </div>
      </SidebarFooter>
    </Sidebar>
  );
}
