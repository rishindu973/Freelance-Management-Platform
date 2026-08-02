import { useState, useEffect } from "react";
import { User, LogOut } from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { Button } from "@/components/ui/button";
import { SidebarTrigger } from "@/components/ui/sidebar";
import { FreelancerPortalService, FreelancerProfile } from "@/api/freelancerPortalService";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

export function FreelancerTopBar() {
    const { logout } = useAuth();
    const [profile, setProfile] = useState<FreelancerProfile | null>(null);

    useEffect(() => {
        FreelancerPortalService.getProfile()
            .then(setProfile)
            .catch(console.error);
    }, []);

    const initials = profile?.fullName
        ? profile.fullName.split(" ").map((n) => n[0]).join("").toUpperCase().slice(0, 2)
        : null;

    return (
        <div className="sticky top-0 z-10 flex h-16 flex-shrink-0 bg-white shadow-sm border-b px-4 items-center justify-between">
            <div className="flex items-center gap-4">
                <SidebarTrigger className="-ml-1" />
            </div>

            <div className="flex items-center gap-4">
                <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                        <Button variant="ghost" className="relative flex items-center rounded-full bg-white text-sm focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2">
                            <span className="sr-only">Open user menu</span>
                            <div className="h-8 w-8 rounded-full bg-primary/10 flex items-center justify-center text-primary font-medium">
                                {initials ? initials : <User className="h-4 w-4" />}
                            </div>
                            <span className="ml-2 font-medium text-sm text-gray-700">
                                {profile?.fullName || "Freelancer"}
                            </span>
                        </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end" className="w-56">
                        <DropdownMenuLabel>My Account</DropdownMenuLabel>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem className="cursor-pointer text-gray-500">
                            Profile Settings
                        </DropdownMenuItem>
                        <DropdownMenuItem onClick={logout} className="cursor-pointer text-red-600 focus:text-red-600">
                            <LogOut className="mr-2 h-4 w-4" />
                            <span>Sign out</span>
                        </DropdownMenuItem>
                    </DropdownMenuContent>
                </DropdownMenu>
            </div>
        </div>
    );
}
