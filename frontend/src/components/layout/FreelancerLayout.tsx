import { Outlet } from "react-router-dom";
import { SidebarProvider } from "@/components/ui/sidebar";
import { FreelancerSidebar } from "./FreelancerSidebar";
import { FreelancerTopBar } from "./FreelancerTopBar";

export function FreelancerLayout() {
  return (
    <SidebarProvider>
      <div className="flex min-h-screen w-full">
        <FreelancerSidebar />
        <div className="flex flex-1 flex-col">
          <FreelancerTopBar />
          <main className="flex-1 overflow-auto bg-cream p-6">
            <Outlet />
          </main>
        </div>
      </div>
    </SidebarProvider>
  );
}
