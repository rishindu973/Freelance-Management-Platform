import { Outlet } from "react-router-dom";
import { FreelancerSidebar } from "./FreelancerSidebar";
import { FreelancerTopBar } from "./FreelancerTopBar";

export default function FreelancerLayout() {
    return (
        <div className="flex h-screen overflow-hidden bg-gray-50">
            {/* Desktop Sidebar */}
            <div className="hidden md:flex md:flex-shrink-0">
                <FreelancerSidebar />
            </div>

            {/* Main Content Area */}
            <div className="flex w-0 flex-1 flex-col overflow-hidden">
                <FreelancerTopBar />

                <main className="relative flex-1 overflow-y-auto focus:outline-none">
                    <div className="py-6">
                        <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
                            <Outlet />
                        </div>
                    </div>
                </main>
            </div>
        </div>
    );
}
