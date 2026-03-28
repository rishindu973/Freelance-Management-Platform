import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { FreelancerPortalService, FreelancerProfile } from "@/api/freelancerPortalService";
import { ProjectResponse } from "@/api/projectService";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
    AlertCircle, Clock, CheckCircle2, Circle, CheckSquare,
    User, Mail, Phone, ExternalLink
} from "lucide-react";
import { format, differenceInDays } from "date-fns";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";

export default function FreelancerDashboard() {
    const [projects, setProjects] = useState<ProjectResponse[]>([]);
    const [profile, setProfile] = useState<FreelancerProfile | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        Promise.all([
            FreelancerPortalService.getProfile(),
            FreelancerPortalService.getAssignments(),
        ])
            .then(([profileData, projectData]) => {
                setProfile(profileData);
                setProjects(projectData);
            })
            .catch(console.error)
            .finally(() => setIsLoading(false));
    }, []);

    const pending = projects.filter(p => p.status?.toLowerCase() === 'pending').length;
    const inProgress = projects.filter(p => p.status?.toLowerCase() === 'in progress').length;
    const completed = projects.filter(p => p.status?.toLowerCase() === 'completed').length;

    const getStatusIcon = (status: string) => {
        const s = status?.toLowerCase();
        if (s === 'completed') return <CheckCircle2 className="w-4 h-4 text-green-500" />;
        if (s === 'in progress') return <Circle className="w-4 h-4 text-blue-500 fill-blue-500/20" />;
        return <Circle className="w-4 h-4 text-gray-500" />;
    };

    const getStatusColor = (status: string) => {
        const s = status?.toLowerCase();
        if (s === 'completed') return "bg-green-100 text-green-800";
        if (s === 'in progress') return "bg-blue-100 text-blue-800";
        return "bg-gray-100 text-gray-800";
    };

    const getDeadlineWarning = (deadlineDate: string) => {
        if (!deadlineDate) return null;
        const daysLeft = differenceInDays(new Date(deadlineDate), new Date());
        if (daysLeft < 0) return { color: "text-red-600 bg-red-50", text: `Overdue by ${Math.abs(daysLeft)} days` };
        if (daysLeft <= 3) return { color: "text-red-600 bg-red-50", text: `${daysLeft} days left` };
        if (daysLeft <= 7) return { color: "text-orange-600 bg-orange-50", text: `${daysLeft} days left` };
        return { color: "text-gray-600", text: `${daysLeft} days left` };
    };

    if (isLoading) {
        return <div className="p-8 text-center text-gray-500">Loading dashboard...</div>;
    }

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold tracking-tight text-gray-900">My Dashboard</h1>
                <p className="text-gray-500">Track your assigned projects and deliverables.</p>
            </div>

            {/* ---- Profile Card ---- */}
            {profile && (
                <div className="bg-white rounded-xl border shadow-sm p-6 flex flex-col sm:flex-row items-start sm:items-center gap-6">
                    {/* Avatar */}
                    <div className="flex h-16 w-16 shrink-0 items-center justify-center rounded-full bg-primary text-2xl font-bold text-primary-foreground">
                        {profile.fullName?.split(" ").map(n => n[0]).join("").slice(0, 2).toUpperCase()}
                    </div>
                    {/* Info */}
                    <div className="flex-1 space-y-1">
                        <h2 className="text-xl font-semibold text-gray-900">{profile.fullName}</h2>
                        {profile.title && (
                            <Badge variant="outline" className="text-xs font-medium">{profile.title}</Badge>
                        )}
                        <div className="flex flex-wrap gap-x-4 gap-y-1 text-sm text-gray-500 pt-1">
                            {/* Email comes from user account, not directly in profile - show contact */}
                            {profile.contactNumber && (
                                <span className="flex items-center gap-1">
                                    <Phone className="w-3.5 h-3.5" />
                                    {profile.contactNumber}
                                </span>
                            )}
                            {profile.status && (
                                <span className="flex items-center gap-1">
                                    <User className="w-3.5 h-3.5" />
                                    Status: <span className="font-medium text-gray-700">{profile.status}</span>
                                </span>
                            )}
                            {profile.driveLink && (
                                <a
                                    href={profile.driveLink.startsWith("http") ? profile.driveLink : `https://${profile.driveLink}`}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="flex items-center gap-1 text-primary hover:underline"
                                >
                                    <ExternalLink className="w-3.5 h-3.5" />
                                    Portfolio / Drive
                                </a>
                            )}
                        </div>
                    </div>
                </div>
            )}

            {/* ---- KPI Cards ---- */}
            <div className="grid gap-4 md:grid-cols-3">
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium text-gray-500">Pending</CardTitle>
                        <Clock className="h-4 w-4 text-gray-400" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{pending}</div>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium text-gray-500">In Progress</CardTitle>
                        <AlertCircle className="h-4 w-4 text-blue-400" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{inProgress}</div>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium text-gray-500">Completed</CardTitle>
                        <CheckCircle2 className="h-4 w-4 text-green-400" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{completed}</div>
                    </CardContent>
                </Card>
            </div>

            {/* ---- Assignments List ---- */}
            <div className="bg-white rounded-lg shadow-sm border overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-100">
                    <h2 className="font-semibold text-gray-900">Current Assignments</h2>
                </div>

                {projects.length === 0 ? (
                    <div className="p-12 text-center text-gray-500 flex flex-col items-center">
                        <CheckSquare className="w-12 h-12 text-gray-200 mb-4" />
                        <p className="text-lg font-medium text-gray-900">No projects assigned yet</p>
                        <p className="text-sm">When a manager assigns you to a project, it will appear here.</p>
                    </div>
                ) : (
                    <div className="divide-y divide-gray-100">
                        {projects.map(project => {
                            const warning = getDeadlineWarning(project.deadline);
                            return (
                                <div key={project.id} className="p-6 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 hover:bg-gray-50 transition-colors">
                                    <div className="space-y-1">
                                        <Link to={`/freelancer/projects/${project.id}`} className="font-semibold text-primary hover:underline text-lg">
                                            {project.name}
                                        </Link>
                                        <div className="flex flex-wrap items-center gap-3 text-sm text-gray-500">
                                            <span className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(project.status)}`}>
                                                {getStatusIcon(project.status)}
                                                {project.status.toUpperCase()}
                                            </span>
                                            {project.startDate && (
                                                <>
                                                    <span>•</span>
                                                    <span>Started: {format(new Date(project.startDate), 'MMM d, yyyy')}</span>
                                                </>
                                            )}
                                            {project.type && (
                                                <>
                                                    <span>•</span>
                                                    <span className="italic text-gray-400">{project.type}</span>
                                                </>
                                            )}
                                        </div>
                                    </div>

                                    <div className="flex flex-col sm:items-end gap-2 w-full sm:w-auto">
                                        {warning && (
                                            <span className={`text-sm font-medium px-2.5 py-1 rounded-md ${warning.color}`}>
                                                {warning.text}
                                            </span>
                                        )}
                                        <Button asChild variant="outline" size="sm" className="w-full sm:w-auto">
                                            <Link to={`/freelancer/projects/${project.id}`}>
                                                View Project
                                            </Link>
                                        </Button>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                )}
            </div>
        </div>
    );
}
