import { Link, useLocation } from "react-router-dom";
import { LayoutDashboard, Briefcase } from "lucide-react";

export function FreelancerSidebar() {
    const location = useLocation();

    const navigation = [
        { name: "Dashboard", href: "/freelancer/dashboard", icon: LayoutDashboard },
        { name: "My Projects", href: "/freelancer/projects", icon: Briefcase },
    ];

    return (
        <div className="flex h-full w-64 flex-col bg-white border-r">
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
                                    group flex items-center px-3 py-2.5 text-sm font-medium rounded-md transition-colors
                                    ${isActive
                                        ? "bg-primary/10 text-primary"
                                        : "text-gray-700 hover:bg-gray-50 hover:text-gray-900"
                                    }
                                `}
                            >
                                <item.icon
                                    className={`mr-3 h-5 w-5 flex-shrink-0 transition-colors ${isActive ? "text-primary" : "text-gray-400 group-hover:text-gray-500"}`}
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
