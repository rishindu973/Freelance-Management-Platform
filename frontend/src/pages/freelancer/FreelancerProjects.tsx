import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { FreelancerPortalService } from "@/api/freelancerPortalService";
import { ProjectResponse } from "@/api/projectService";
import {
    Briefcase, Calendar, Clock, CheckCircle2, Circle,
    ArrowRight, Search, Filter
} from "lucide-react";
import { format, differenceInDays } from "date-fns";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";

export default function FreelancerProjects() {
    const [projects, setProjects] = useState<ProjectResponse[]>([]);
    const [filteredProjects, setFilteredProjects] = useState<ProjectResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState("");
    const [statusFilter, setStatusFilter] = useState("all");

    useEffect(() => {
        FreelancerPortalService.getAssignments()
            .then(data => {
                setProjects(data);
                setFilteredProjects(data);
            })
            .catch(console.error)
            .finally(() => setIsLoading(false));
    }, []);

    useEffect(() => {
        let result = projects;

        if (searchTerm) {
            result = result.filter(p =>
                p.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                p.description?.toLowerCase().includes(searchTerm.toLowerCase())
            );
        }

        if (statusFilter !== "all") {
            result = result.filter(p => p.status.toLowerCase() === statusFilter.toLowerCase());
        }

        setFilteredProjects(result);
    }, [searchTerm, statusFilter, projects]);

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

    const getDeadlineInfo = (deadlineDate: string) => {
        if (!deadlineDate) return null;
        const daysLeft = differenceInDays(new Date(deadlineDate), new Date());
        if (daysLeft < 0) return { color: "text-red-600", text: `Overdue by ${Math.abs(daysLeft)} days` };
        return { color: "text-gray-600", text: `${daysLeft} days remaining` };
    };

    if (isLoading) {
        return <div className="p-8 text-center text-gray-500">Loading projects...</div>;
    }

    return (
        <div className="space-y-6">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-gray-900">My Projects</h1>
                    <p className="text-gray-500">List of all projects you are assigned to.</p>
                </div>
            </div>

            {/* Filters */}
            <div className="flex flex-col sm:flex-row gap-4 bg-white p-4 rounded-lg border shadow-sm">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                    <Input
                        placeholder="Search projects..."
                        className="pl-9"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                <div className="flex items-center gap-2">
                    <Filter className="h-4 w-4 text-gray-400" />
                    <select
                        className="h-10 rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2"
                        value={statusFilter}
                        onChange={(e) => setStatusFilter(e.target.value)}
                    >
                        <option value="all">All Status</option>
                        <option value="pending">Pending</option>
                        <option value="in progress">In Progress</option>
                        <option value="completed">Completed</option>
                    </select>
                </div>
            </div>

            {/* Projects Grid */}
            {filteredProjects.length === 0 ? (
                <div className="p-12 text-center text-gray-500 bg-white rounded-lg border flex flex-col items-center">
                    <Briefcase className="w-12 h-12 text-gray-200 mb-4" />
                    <p className="text-lg font-medium text-gray-900">No projects found</p>
                    <p className="text-sm">Try adjusting your search or filters.</p>
                </div>
            ) : (
                <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                    {filteredProjects.map(project => {
                        const deadline = getDeadlineInfo(project.deadline);
                        return (
                            <Link
                                key={project.id}
                                to={`/freelancer/projects/${project.id}`}
                                className="group bg-white rounded-xl border shadow-sm hover:shadow-md transition-all p-5 flex flex-col h-full"
                            >
                                <div className="flex justify-between items-start mb-3">
                                    <Badge className={getStatusColor(project.status)}>
                                        <div className="flex items-center gap-1.5">
                                            {getStatusIcon(project.status)}
                                            {project.status.toUpperCase()}
                                        </div>
                                    </Badge>
                                    <span className="text-xs text-gray-400 font-mono">#{project.id}</span>
                                </div>

                                <h3 className="text-lg font-bold text-gray-900 group-hover:text-primary transition-colors mb-2">
                                    {project.name}
                                </h3>

                                <p className="text-sm text-gray-500 line-clamp-2 mb-4 flex-1">
                                    {project.description || "No description provided."}
                                </p>

                                <div className="space-y-2 pt-4 border-t border-gray-50">
                                    <div className="flex items-center justify-between text-sm">
                                        <div className="flex items-center gap-2 text-gray-500">
                                            <Calendar className="w-4 h-4" />
                                            <span>Deadline:</span>
                                        </div>
                                        <span className={`font-medium ${deadline?.color}`}>
                                            {project.deadline ? format(new Date(project.deadline), 'MMM d, yyyy') : "N/A"}
                                        </span>
                                    </div>

                                    <div className="flex items-center justify-between text-xs text-gray-400">
                                        <div className="flex items-center gap-2">
                                            <Clock className="w-4 h-4" />
                                            <span>{deadline?.text || ""}</span>
                                        </div>
                                        <div className="flex items-center gap-1 text-primary font-medium">
                                            Details <ArrowRight className="w-3 h-3" />
                                        </div>
                                    </div>
                                </div>
                            </Link>
                        );
                    })}
                </div>
            )}
        </div>
    );
}
