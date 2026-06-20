import { Link, useLocation } from "react-router-dom";
import { LayoutDashboard, Briefcase } from "lucide-react";

export function FreelancerSidebar() {
    const location = useLocation();

    const navigation = [
        { name: "Dashboard", href: "/freelancer/dashboard", icon: LayoutDashboard },
        { name: "My Projects", href: "/freelancer/projects", icon: Briefcase },
    ];

    return (
        <div className="flex h-full w-64 flex-col bg-[#FBF9F4] border-r border-slate-200/50">
            <div className="flex h-16 shrink-0 items-center px-6">
                <span className="text-xl font-bold bg-gradient-to-r from-primary to-blue-600 outline-none text-transparent bg-clip-text">
                    FreelanceFlow
                </span>
            </div>
            <div className="flex flex-1 flex-col overflow-y-auto">
                <nav className="flex-1 space-y-1 px-4 py-4">
                    {navigation.map((item) => {
                        const isActive =
                            location.pathname === item.href ||
                            location.pathname.startsWith(item.href + "/");
                        return (
                            <Link
                                key={item.name}
                                to={item.href}
                                className={`
                                    group flex items-center px-3 py-2.5 text-sm font-medium rounded-md transition-all duration-200
                                    ${isActive
                                        ? "bg-[#10B981]/15 text-[#0F172A]"
                                        : "text-slate-600 hover:text-[#0F172A] hover:bg-slate-100"
                                    }
                                `}
                            >
                                <item.icon
                                    className={`mr-3 h-5 w-5 flex-shrink-0 transition-colors stroke-[1.5] ${isActive ? "text-[#0F172A]" : "text-slate-400 group-hover:text-slate-500"}`}
                                    aria-hidden="true"
                                />
                                {item.name}
                            </Link>
                        );
                    })}
                </nav>
            </div>
        </div>
    );
}
