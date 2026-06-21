import {
  LayoutDashboard,
  Briefcase,
} from "lucide-react";
import { useLocation } from "react-router-dom";
import { NavLink } from "@/components/NavLink";
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  useSidebar,
} from "@/components/ui/sidebar";

const mainItems = [
  { title: "Dashboard", url: "/freelancer/dashboard", icon: LayoutDashboard },
  { title: "My Projects", url: "/freelancer/projects", icon: Briefcase },
];

export function FreelancerSidebar() {
  const { state } = useSidebar();
  const collapsed = state === "collapsed";
  const location = useLocation();

  const isActive = (url: string) =>
    location.pathname === url || location.pathname.startsWith(url + "/");

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
            <span className="text-base font-semibold tracking-tight text-foreground font-gebuk text-[#064e3b]">
              FreelanceFlow
            </span>
          )}
          {collapsed && (
            <span className="text-base font-semibold text-foreground font-gebuk text-[#10B981]">FF</span>
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
                    >
                      <item.icon className={`h-4 w-4 ${isActive(item.url) && !collapsed ? "text-[#0F172A]" : ""}`} />
                      <span>{item.title}</span>
                    </NavLink>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
    </Sidebar>
  );
}
